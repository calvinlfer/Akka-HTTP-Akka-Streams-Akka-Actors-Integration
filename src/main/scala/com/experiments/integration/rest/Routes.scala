package com.experiments.integration.rest

import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Source
import com.experiments.integration.actors.JokePublisher
import com.experiments.integration.domain.JokeEvent
import com.experiments.integration.serialization.JokeEventJsonProtocol._
import de.heikoseeberger.akkasse.ServerSentEvent
import spray.json.JsonWriter
import de.heikoseeberger.akkasse.EventStreamMarshalling._ // Needed for marshalling
import ch.megard.akka.http.cors.CorsDirectives._          // Needed for CORS

trait Routes {
  val routes = cors() {
    streamingJokes
  }

  private def wrapWithServerSentEvent[T](element: T)(implicit writer: JsonWriter[T]): ServerSentEvent =
    ServerSentEvent(writer.write(element).compactPrint)

  def streamingJokes =
    path("streaming-jokes") {
      get {
        val source = Source.actorPublisher[JokeEvent](JokePublisher.props)
          // long notation is used to pass in implicit JSON marshaller
          .map(j => wrapWithServerSentEvent(j))

        complete(source)
      }
    }
}
