package com.salcedo.rapbot.websocket

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.io.Tcp.{PeerClosed, Received, Write}
import akka.routing.{BroadcastRoutingLogic, Router}
import akka.util.ByteString
import com.google.gson.Gson
import com.salcedo.rapbot.snapshot.SnapshotActor.Snapshot
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled.wrappedBuffer
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE
import io.netty.handler.codec.http.websocketx.{TextWebSocketFrame, WebSocketFrame, WebSocketServerProtocolHandler}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.util.ReferenceCounted

import scala.concurrent.Promise
import scala.util.Try

object WebSocketConnectionActor {
  def props(connection: ActorRef): Props = Props(new WebSocketConnectionActor(connection))
}

class WebSocketConnectionActor(connection: ActorRef) extends Actor with ActorLogging {
  val json = new Gson
  val channel = new EmbeddedChannel(
    new HttpServerCodec,
    new HttpObjectAggregator(2048),
    new WebSocketServerProtocolHandler("/"),
    new WebSocketFrameHandler(self)
  )
  val handshake: Promise[Unit] = Promise()

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[Snapshot])
  }

  override def receive: Receive = {
    case HANDSHAKE_COMPLETE => handshake.complete(Try())
    case frame: TextWebSocketFrame => log.debug(s"Parsed text frame from client data. Frame: ${frame.text}")
    case Received(data) => this.receiveData(data)
    case PeerClosed => context.stop(self)
    case Failure(e) => log.error("An exception occurred handling the WebSocket frame. {}", e)
    case snapshot: Snapshot => this.push(snapshot)
  }

  def receiveData(data: ByteString): Unit = {
    log.info(s"Received data from client. Data: ${data.utf8String}")

    channel.writeInbound(wrappedBuffer(data.asByteBuffer))
  }

  def push(snapshot: Snapshot): Unit = {
    if (!handshake.isCompleted) return

    channel.writeOutbound(new TextWebSocketFrame(json.toJson(snapshot)))

    val byteBuf = channel.readOutbound().asInstanceOf[ByteBuf]
    val someValue: Option[ByteBuf] = Some(byteBuf)
    val builder = ByteString.newBuilder

    someValue
      .map(value => value.readBytes(builder.asOutputStream, value.readableBytes()))
      .map(_.release())
      .foreach(_ => connection ! Write(builder.result()))
  }
}

/**
  * Echoes uppercase content of text frames.
  */
class WebSocketFrameHandler(actor: ActorRef) extends SimpleChannelInboundHandler[WebSocketFrame] {
  override def userEventTriggered(ctx: ChannelHandlerContext, event: scala.Any): Unit = {
    event match {
      case HANDSHAKE_COMPLETE => actor ! event
    }
  }

  @throws[Exception]
  override protected def channelRead0(ctx: ChannelHandlerContext, frame: WebSocketFrame): Unit = {
    frame match {
      case _: TextWebSocketFrame => actor ! frame.retain()
      case _ => actor ! Failure(throw new UnsupportedOperationException("Unsupported frame type: " + frame.getClass.getName))
    }
  }
}