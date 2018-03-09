package com.salcedo.rapbot.websocket

import akka.actor.ActorRef
import akka.actor.Status.Failure
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.{TextWebSocketFrame, WebSocketFrame}
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE

class WebSocketFrameHandler(actor: ActorRef) extends SimpleChannelInboundHandler[WebSocketFrame] {
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    actor ! Failure(cause)
  }

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