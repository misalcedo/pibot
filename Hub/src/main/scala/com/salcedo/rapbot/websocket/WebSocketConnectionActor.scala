package com.salcedo.rapbot.websocket

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.routing.{BroadcastRoutingLogic, Router}
import com.salcedo.rapbot.snapshot.SnapshotActor.Snapshot
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE
import io.netty.handler.codec.http.websocketx.{TextWebSocketFrame, WebSocketFrame, WebSocketServerProtocolHandler}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}

object WebSocketConnectionActor {
  def props: Props = Props(new WebSocketConnectionActor)
}

class WebSocketConnectionActor extends Actor with ActorLogging {
  var router = Router(BroadcastRoutingLogic())
  val channel = new EmbeddedChannel(
    new HttpServerCodec,
    new HttpObjectAggregator(2048),
    new WebSocketServerProtocolHandler("/"),
    new WebSocketFrameHandler(self)
  )

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[Snapshot])
  }

  override def receive: Receive = {
    case HANDSHAKE_COMPLETE => channel.writeOutbound(new TextWebSocketFrame("{foo: 1}"))
    case frame: TextWebSocketFrame => log.info(frame.text())
    case Failure(e) => log.error("An exception occurred handling the WebSocket frame. {}", e)
    case snapshot: Snapshot => log.info(snapshot.toString)
    case terminated: Terminated => this.terminate(terminated)
  }

  private def terminate(message: Terminated): Unit = {
    router = router.removeRoutee(message.actor)
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
      case _: TextWebSocketFrame => actor ! frame
      case _ =>
        actor ! Failure(throw new UnsupportedOperationException("Unsupported frame type: " + frame.getClass.getName))
    }
  }
}