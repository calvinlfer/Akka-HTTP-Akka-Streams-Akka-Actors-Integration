package com.experiments.integration.actors

import akka.actor.{ActorLogging, Props}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage._
import com.experiments.integration.domain.JokeEvent

import scala.annotation.tailrec

/**
  * This class is the integration point between Akka Actors and Akka Streams
  * This actor is instantiated for every request that requires a Streaming response
  * This lives for the duration of the Stream lifetime and is terminated after
  *
  * This actor subscribes to the Event Stream to obtain Joke Events and publishes those
  * Joke Events into the Stream
  */
class JokePublisher extends ActorPublisher[JokeEvent] with ActorLogging {
  val MaxBufferSize = 100
  var buffer = Vector.empty[JokeEvent]

  override def preStart(): Unit = {
    log.debug("Joke Publisher created, subscribing to Event Stream")
    context.system.eventStream.subscribe(self, classOf[JokeEvent])
  }

  override def postStop(): Unit = {
    log.debug("Joke Publisher stopping, un-subscribing to Event Stream")
    context.system.eventStream.unsubscribe(self)
  }

  @tailrec
  private def deliverBuffer(): Unit =
    if (totalDemand > 0 && isActive) {
      // You are allowed to send as many elements as have been requested by the stream subscriber
      // total demand is a Long and can be larger than what the buffer has
      if (totalDemand <= Int.MaxValue) {
        val (sendDownstream, holdOn) = buffer.splitAt(totalDemand.toInt)
        buffer = holdOn
        // send the stuff downstream
        sendDownstream.foreach(onNext)
      } else {
        val (sendDownStream, holdOn) = buffer.splitAt(Int.MaxValue)
        buffer = holdOn
        sendDownStream.foreach(onNext)
        // recursive call checks whether is more demand before repeating the process
        deliverBuffer()
      }
    }

  override def receive: Receive = {
    case j: JokeEvent if buffer.size == MaxBufferSize =>
      log.warning("Buffer is full, ignoring incoming JokeEvent")

    case j: JokeEvent =>
      // send elements to the stream immediately since there is demand from downstream and we
      // have not buffered anything so why bother buffering, send immediately
      // You send elements to the stream by calling onNext
      if (buffer.isEmpty && totalDemand > 0L && isActive) onNext(j)
      // there is no demand from downstream so we will store the result in our buffer
      // Note that :+= means add to end of Vector
      // this allows us to respect backpressure
      else buffer :+= j

    case Cancel =>
      log.info("Stream cancelled")
      // Note: postStop is automatically invoked
      context.stop(self)

    // A request from downstream to send more data
    // When the stream subscriber requests more elements the ActorPublisherMessage.Request message is
    // delivered to this actor, and you can act on that event. The totalDemand is updated automatically.
    case Request(_) =>
      deliverBuffer()

    case other =>
      log.warning(s"Unknown message $other received")
  }
}

object JokePublisher {
  def props = Props[JokePublisher]
}