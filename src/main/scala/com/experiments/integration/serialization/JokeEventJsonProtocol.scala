package com.experiments.integration.serialization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.experiments.integration.domain.JokeEvent
import spray.json.DefaultJsonProtocol

object JokeEventJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jokeEventFormat = jsonFormat2(JokeEvent)
}
