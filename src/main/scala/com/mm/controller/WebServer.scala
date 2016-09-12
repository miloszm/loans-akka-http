package com.mm.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.mm.model.{Loan, LoanId, LoanRequest}
import com.mm.services.{InMemoryLoanRepository, LoanRepository, RandomIdGenerator}
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
import scala.io.StdIn




object WebServer {
  def saveLoan(loanRequest:LoanRequest)(implicit repo:LoanRepository):Future[Loan] = {
    val loan = repo.storeLoan(loanRequest)
    Future.successful(loan)
  }
  def fetchLoan(loanId:LoanId)(implicit repo:LoanRepository):Future[Option[Loan]] = {
    val loan = repo.getLoan(loanId)
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
//      get {
//        pathPrefix("loan" / JavaUUID){ loanId =>
//          val maybeLoan:Future[Option[Loan]] = fetchLoan(LoanId(loanId.toString))
//          onSuccess(maybeLoan) {
//            case Some(loan) => complete(loan)
//            case None => complete(StatusCodes.NotFound)
//          }
//
//        }
//      } ~
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
