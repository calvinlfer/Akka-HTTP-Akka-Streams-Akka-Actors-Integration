package com.experiments.integration

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.experiments.integration.actors.JokeFetcher
import com.experiments.integration.rest.Routes
import com.typesafe.config.ConfigFactory

object ServerMain extends App with Routes {
  val config = ConfigFactory.load()
  implicit val actorSystem = ActorSystem(name = "jokes-actor-system", config)
  implicit val streamMaterializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher
  val log = actorSystem.log

  val host = config.getString("app.host")
  val port = config.getInt("app.port")

  actorSystem.actorOf(JokeFetcher.props, "joke-fetcher")

  val bindingFuture = Http().bindAndHandle(routes, host, port)
  bindingFuture.map(_.localAddress).map(addr => s"Bound to $addr").foreach(log.info)
}
