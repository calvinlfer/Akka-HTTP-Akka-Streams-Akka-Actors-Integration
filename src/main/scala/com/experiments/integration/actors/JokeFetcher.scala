package com.experiments.integration.actors

import akka.actor.{Actor, ActorLogging, Cancellable, Props, Status}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.experiments.integration.actors.JokeFetcher.{FetchJoke, Result}
import com.experiments.integration.domain.JokeEvent
import com.experiments.integration.serialization.JokeJsonProtocol._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class JokeFetcher extends Actor with ActorLogging {
  implicit val timeout = Timeout(5 seconds)
  implicit val materializer = ActorMaterializer()(context.system)
  implicit val ec = context.system.dispatcher

  val http = Http(context.system)
  val url = "http://api.icndb.com/jokes/random?escape=javascript"
  var cancellable: Option[Cancellable] = None

  override def preStart(): Unit = {
    log.debug("Starting up Joke Fetcher")
    cancellable = Some(
      context.system.scheduler.schedule(initialDelay = 1 second, interval = 1 second, receiver = self, FetchJoke)
    )
  }

  override def postStop(): Unit =
    cancellable.foreach(c => c.cancel())

  override def receive: Receive = {
    case FetchJoke =>
      val futureResponse = http.singleRequest(HttpRequest(GET, url)) flatMap {
        httpResponse =>
          httpResponse.status match {
            case OK =>
              val futureResult = Unmarshal(httpResponse).to[Result]
              futureResult.map(x => Some(x))
            case _ =>
              log.error("Non 200 OK response code, error obtaining jokes")
              Future.successful(None)
          }
      }
      futureResponse pipeTo self

    case Status.Failure(throwable) =>
      log.error("Could not obtain jokes", throwable)

    case Some(Result(_, joke)) =>
      context.system.eventStream.publish(JokeEvent(joke.id, joke.joke))
  }
}

object JokeFetcher {

  sealed trait Command

  case object FetchJoke extends Command

  // Type safe HTTP response
  case class Joke(id: Int, joke: String, categories: List[String])

  case class Result(response: String, joke: Joke)

  def props = Props[JokeFetcher]
}
