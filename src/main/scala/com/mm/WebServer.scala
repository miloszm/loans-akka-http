package com.mm

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

object WebServer {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("mm-actor-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,"<h1>Hello Akka-Http</h1>"))
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println("server online - press ENTER to stop")

    StdIn.readLine()
    bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
  }
}
