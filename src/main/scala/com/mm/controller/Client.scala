package com.mm.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Client {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def main(args: Array[String]): Unit = {
//     doHello(system)
//      doLoanCreation()
      doOfferCreation()
    }

  def doLoanCreation(): Unit = {

    val requestEntity = HttpEntity(ContentTypes.`application/json`,"{\"amount\": 145, \"durationInDays\": 365}")

    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] =
      Http().outgoingConnection("127.0.0.1", 8080)
    val responseFuture: Future[HttpResponse] =
      Source.single(HttpRequest(method = HttpMethods.POST, uri = "/loans", entity=requestEntity))
        .via(connectionFlow)
        .runWith(Sink.head)
    responseFuture.andThen {
      case Success(x) => println(s"loan request succeeded: $x")
      case Failure(_) => println("loan request failed")
    }.andThen {
      case _ => system.terminate()
    }
  }

  def doOfferCreation(): Unit = {

    val requestEntity = HttpEntity(ContentTypes.`application/json`,"{\"amount\": 90, \"interestRate\": 8}")

    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] =
      Http().outgoingConnection("127.0.0.1", 8080)
    val responseFuture: Future[HttpResponse] =
      Source.single(HttpRequest(method = HttpMethods.POST, uri = "/loans/6b3ee5d0-cf76-418d-960c-03de73a9086a/offers", entity=requestEntity))
        .via(connectionFlow)
        .runWith(Sink.head)
    responseFuture.andThen {
      case Success(x) => println(s"offer request succeeded: $x")
      case Failure(_) => println("offer request failed")
    }.andThen {
      case _ => system.terminate()
    }
  }

  def doHello(): Unit = {
    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] =
      Http().outgoingConnection("127.0.0.1", 8080)
    val responseFuture: Future[HttpResponse] =
      Source.single(HttpRequest(uri = "/hello"))
        .via(connectionFlow)
        .runWith(Sink.head)
    responseFuture.andThen {
      case Success(x) => println(s"request succeeded: $x")
      case Failure(_) => println("request failed")
    }.andThen {
      case _ => system.terminate()
    }
  }
}
