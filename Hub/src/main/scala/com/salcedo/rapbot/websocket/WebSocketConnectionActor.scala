package com.salcedo.rapbot.websocket

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.{PeerClosed, Received, Write}
import akka.util.ByteString
import com.google.gson.Gson
import com.salcedo.rapbot.hub.Hub.SystemState
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled.wrappedBuffer
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE
import io.netty.handler.codec.http.websocketx.{TextWebSocketFrame, WebSocketServerProtocolHandler}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}

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

  override def receive: Receive = {
    case HANDSHAKE_COMPLETE => handshake.complete(Try(Unit))
    case frame: TextWebSocketFrame => log.debug(s"Parsed text frame from client data. Frame: ${frame.text}")
    case Received(data) => this.receiveData(data)
    case PeerClosed => context.stop(self)
    case Failure(e) => log.error("An exception occurred handling the WebSocket frame. {}", e)
    case state: SystemState => this.push(state)
  }

  def receiveData(data: ByteString): Unit = {
    log.debug(s"Received data from client. Data: ${data.utf8String}")

    channel.writeInbound(wrappedBuffer(data.asByteBuffer))
  }

  def push(state: SystemState): Unit = {
    if (!handshake.isCompleted) return

    channel.writeOutbound(new TextWebSocketFrame(json.toJson(state)))

    val byteBuf = channel.readOutbound().asInstanceOf[ByteBuf]
    val someValue: Option[ByteBuf] = Some(byteBuf)
    val builder = ByteString.newBuilder

    someValue
      .map(value => value.readBytes(builder.asOutputStream, value.readableBytes()))
      .map(_.release())
      .foreach(_ => connection ! Write(builder.result()))
  }
}