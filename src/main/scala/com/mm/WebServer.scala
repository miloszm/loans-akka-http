package com.mm

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
import scala.io.StdIn




object WebServer {
  final case class Loan(amount:Int)
  def saveLoan(loan:Loan):Future[Done] = Future.successful(Done)
  implicit val loanFormat = jsonFormat1(Loan)


  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("mm-actor-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher


    val route =
      get {
        pathPrefix("hello") {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,"<h1>Hello Akka-Http</h1>"))
        }
      } ~
      post {
        pathPrefix("loans") {
          entity(as[Loan]) { loan =>
            val saved: Future[Done] = saveLoan(loan)
            onComplete(saved) { done =>
              complete(s"loan created for ${loan.amount}")
            }
          }
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
