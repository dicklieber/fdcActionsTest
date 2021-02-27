
package org.wa9nnn.fdcluster.store.network

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props}
import akka.io.{IO, Udp}
import com.typesafe.config.Config
import org.wa9nnn.fdcluster.store.JsonContainer

class MultcastSenderActor(val config: Config) extends MulticastActor {

  import context.system

  IO(Udp) ! Udp.SimpleSender

  def receive: PartialFunction[Any, Unit] = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
  }

  def ready(send: ActorRef): Receive = {
    case something: JsonContainer =>
      val byteString = something.toByteString
      send ! Udp.Send(byteString, new InetSocketAddress(multicastGroup, port))

    case x ⇒
      println(s"MultcastSenderActor: Unexpected: $x")
  }
}

object MultcastSenderActor {
  def props(config: Config): Props = {
    Props(new MultcastSenderActor(config) )
  }
}