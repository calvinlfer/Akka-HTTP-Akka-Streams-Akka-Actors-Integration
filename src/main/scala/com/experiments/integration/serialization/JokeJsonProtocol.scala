package com.experiments.integration.serialization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.experiments.integration.actors.JokeFetcher.{Joke, Result}
import spray.json.DefaultJsonProtocol

object JokeJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jokeFormat = jsonFormat3(Joke)
  // remapping JSON field names to Scala (note: only JSON fields can be seen here)
  // JSON -> Scala
  // type -> response
  // value -> joke
  implicit val resultFormat = jsonFormat(Result, "type", "value")
}
