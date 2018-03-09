package com.salcedo.rapbot.websocket

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelFuture, ChannelFutureListener}
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.ServerHandshakeStateEvent
import io.netty.handler.logging.{LogLevel, LoggingHandler}

import scala.concurrent.{Future, Promise}
import scala.util.Try


/**
  * A HTTP server which serves Web Socket requests at:
  *
  * http://localhost:8080/websocket
  *
  * Open your browser at http://localhost:8080/, then the demo page will be loaded and a Web Socket connection will be
  * made automatically.
  *
  * This server illustrates support for the different web socket specification versions and will work with:
  *
  * <ul>
  * <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
  * <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
  * <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
  * <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
  * <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
  * <li>Firefox 11+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
  * </ul>
  */
object NettyWSServer {
  val PORT: Int = System.getProperty("port", "8080").toInt

  @throws[Exception]
  def run(): Future[Unit] = {
    val bossGroup = new NioEventLoopGroup(1)
    val workerGroup = new NioEventLoopGroup
    val channel = new ServerBootstrap()
      .group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new WebSocketServerInitializer())
      .bind(PORT)
      .sync
      .channel

    System.out.println("Open your web browser and navigate to http://127.0.0.1:" + PORT + '/')

    val promise: Promise[Unit] = Promise()
    val listener: ChannelFutureListener = new ChannelFutureListener() {
      override def operationComplete(future: ChannelFuture): Unit = promise.complete(Try())
    }

    channel.closeFuture.addListener(listener)

    promise.future
  }
}

final class NettyWSServer {}

import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.http.HttpObjectAggregator

object WebSocketServerInitializer {
  private val WEBSOCKET_PATH = "/"
}

class WebSocketServerInitializer() extends ChannelInitializer[SocketChannel] {
  @throws[Exception]
  override def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline
    pipeline.addLast(new HttpServerCodec())
    pipeline.addLast(new HttpObjectAggregator(2048))
    pipeline.addLast(new WebSocketServerProtocolHandler(WebSocketServerInitializer.WEBSOCKET_PATH))
    pipeline.addLast(new WebSocketFrameHandlerNotActor)
  }
}

import java.util.Locale

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.{TextWebSocketFrame, WebSocketFrame}


/**
  * Echoes uppercase content of text frames.
  */
class WebSocketFrameHandlerNotActor extends SimpleChannelInboundHandler[WebSocketFrame] {
  override def userEventTriggered(ctx: ChannelHandlerContext, evt: scala.Any): Unit = {
    evt match {
      case ServerHandshakeStateEvent.HANDSHAKE_COMPLETE =>
        ctx.channel.writeAndFlush(new TextWebSocketFrame("{\"backLeft\":{\"speed\":0,\"command\":{\"value\":1}},\"backRight\":{\"speed\":0,\"command\":{\"value\":1}}}"))
    }
  }

  @throws[Exception]
  override protected def channelRead0(ctx: ChannelHandlerContext, frame: WebSocketFrame): Unit = { // ping and pong frames already handled
    frame match {
      case textFrame: TextWebSocketFrame => // Send the uppercase string back.
        val request = textFrame.text
        System.out.printf("%s received %s\n", ctx.channel, request)
        ctx.channel.writeAndFlush(new TextWebSocketFrame(request.toUpperCase(Locale.US)))
      case _ =>
        val message = "unsupported frame type: " + frame.getClass.getName
        throw new UnsupportedOperationException(message)
    }
  }
}