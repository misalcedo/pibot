package com.salcedo.rapbot.websocket

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.io.Tcp.{PeerClosed, Received, Write}
import akka.util.ByteString
import com.salcedo.rapbot.driver.KeyBoardDriverActor.Key
import com.salcedo.rapbot.hub.Hub.SystemState
import com.salcedo.rapbot.serialization.JSON.{read, write}
import com.salcedo.rapbot.snapshot.SnapshotTakerActor.TakeSnapshot
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
  val channel = new EmbeddedChannel(
    new HttpServerCodec,
    new HttpObjectAggregator(2048),
    new WebSocketServerProtocolHandler("/"),
    new WebSocketFrameHandler(self)
  )
  val handshake: Promise[Unit] = Promise()

  override def preStart(): Unit = {
    context.watch(connection)
  }

  override def receive: Receive = {
    case HANDSHAKE_COMPLETE => this.complete()
    case frame: TextWebSocketFrame => this.readFrame(frame)
    case Received(data) => this.receiveData(data)
    case PeerClosed => this.close()
    case Failure(e) => log.error("An exception occurred handling the WebSocket frame. {}", e)
    case Terminated(_) => context.stop(self)
    case state: SystemState => this.push(state)
  }

  def complete(): Unit = {
    handshake.complete(Try(Unit))
    context.system.eventStream.publish(TakeSnapshot)
  }

  def readFrame(frame: TextWebSocketFrame): Unit = {
    log.debug("Parsed text frame from client data. Frame: {}", frame.text)

    context.system.eventStream.publish(read(frame.text, classOf[Key]))

    frame.release()
  }

  def receiveData(data: ByteString): Unit = {
    if (!channel.isOpen) return

    log.debug("Received data from client. Data: {}", data.utf8String)

    channel.writeInbound(wrappedBuffer(data.asByteBuffer))
    readChannel()
  }

  def close(): Unit = {
    log.debug("Peer closed the connection.")

    channel.close()
    context.stop(self)
  }

  def push(state: SystemState): Unit = {
    if (!handshake.isCompleted || !channel.isOpen) return

    channel.writeOutbound(new TextWebSocketFrame(write(state)))

    readChannel()
  }

  private def readChannel(): Unit = {
    val byteBuf = channel.readOutbound().asInstanceOf[ByteBuf]
    val builder = ByteString.newBuilder

    Option(byteBuf)
      .map(value => value.readBytes(builder.asOutputStream, value.readableBytes()))
      .map(_.release())
      .foreach(_ => connection ! Write(builder.result()))
  }
}