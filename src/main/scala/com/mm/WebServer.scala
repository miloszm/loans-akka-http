package com.mm

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.mm.model.{Loan, LoanRequest}
import services.{InMemoryLoanRepository, LoanRepository, RandomIdGenerator}
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
import scala.io.StdIn




object WebServer {
  def saveLoan(loanRequest:LoanRequest)(implicit repo:LoanRepository):Future[Loan] = {
    val loan = repo.storeLoan(loanRequest)
    Future.successful(loan)
  }
  implicit val loanFormat = jsonFormat2(LoanRequest)


  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("mm-actor-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    implicit val repo = new InMemoryLoanRepository(new RandomIdGenerator)

    val route =
      get {
        pathPrefix("hello") {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,"<h1>Hello Akka-Http</h1>"))
        }
      } ~
      post {
        pathPrefix("loans") {
          entity(as[LoanRequest]) { loanRequest =>
            val saved: Future[Loan] = saveLoan(loanRequest)
            onComplete(saved) { x =>
              complete(s"loan created: $x")
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
