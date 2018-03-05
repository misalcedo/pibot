package com.salcedo.rapbot.driver

import java.awt.event.KeyEvent

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.salcedo.rapbot.driver.CommandDriverActor._
import com.salcedo.rapbot.driver.KeyBoardDriverActor.{Key, key}

object KeyBoardDriverActor {

  case class Key(event: Int, keyCode: Int)

  def props(driver: ActorRef): Props = Props(new KeyBoardDriverActor(driver))

  def key(keyEvent: KeyEvent): Key = {
    Key(keyEvent.getID, keyEvent.getKeyCode)
  }
}

class KeyBoardDriverActor(val driver: ActorRef) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[KeyEvent])
  }

  override def receive: Receive = {
    case keyEvent: KeyEvent => drive(key(keyEvent))
  }

  def drive(key: Key): Unit = {
    val command = key match {
      case Key(KeyEvent.KEY_PRESSED, KeyEvent.VK_UP) => Faster
      case Key(KeyEvent.KEY_PRESSED, KeyEvent.VK_DOWN) => Slower
      case Key(KeyEvent.KEY_PRESSED, KeyEvent.VK_LEFT) => TurnLeft
      case Key(KeyEvent.KEY_PRESSED, KeyEvent.VK_RIGHT) => TurnRight
      case Key(KeyEvent.KEY_PRESSED, KeyEvent.VK_SPACE) => Forward
      case Key(KeyEvent.KEY_PRESSED, KeyEvent.VK_R) => Reverse
      case Key(KeyEvent.KEY_PRESSED, KeyEvent.VK_B) => Brake
      case Key(KeyEvent.KEY_PRESSED, KeyEvent.VK_F) => FullThrottle
      case _ => NoOp
    }

    driver ! command
  }
}
