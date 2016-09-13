package com.mm.controller

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import com.mm.model._
import com.mm.services.{InMemoryLoanRepository, LoanRepository, LoanService, RandomIdGenerator}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.{Failure, Success}
//import scala.concurrent.ExecutionContext.Implicits.global

// Json4JackonSupport
// json4s

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)
    def read(value: JsValue) = value match {
        case JsString(x) => UUID.fromString(x)
        case x           => throw new Exception(s"Expected UUID as JsString, but got $x")
      }
  }
  implicit val loanIdFormat = jsonFormat1(LoanId)
  implicit val loanRequestFormat = jsonFormat2(LoanRequest)
  implicit val loanFormat = jsonFormat3(Loan)
  implicit val offerIdFormat = jsonFormat1(OfferId)
  implicit val offerRequestFormat = jsonFormat2(OfferRequest)
  implicit val offerFormat = jsonFormat4(Offer)
  implicit val currentOfferFormat = jsonFormat2(CurrentOffer)
}

object WebServer extends Directives with JsonSupport{
  def saveLoan(loanRequest:LoanRequest)(implicit repo:LoanRepository):Future[Loan] = {
    val loan = repo.storeLoan(loanRequest)
    Future.successful(loan)
  }
  def saveOffer(loanId:LoanId, offerRequest:OfferRequest)(implicit repo:LoanRepository):Future[Offer] = {
    val offer = repo.storeOffer(loanId, offerRequest)
    Future.successful(offer)
  }
  def fetchLoan(loanId:LoanId)(implicit repo:LoanRepository):Future[Option[Loan]] = {
    val loan = repo.getLoan(loanId)
    Future.successful(loan)
  }
  def getCurrentOffer(loanId:LoanId)(implicit repo:LoanRepository):Future[Option[CurrentOffer]] = {
    val currentOffer = LoanService.getCurrentOffer(loanId)(repo)
    Future.successful(currentOffer)
  }

  implicit val repo = new InMemoryLoanRepository(new RandomIdGenerator)

  val route =
      get {
        pathPrefix("hello") {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,"<h1>Hello Akka-Http</h1>"))
        }
      } ~
      get {
        pathPrefix("loan" / JavaUUID){ loanId =>
          val maybeLoan:Future[Option[Loan]] = fetchLoan(LoanId(loanId))
          onSuccess(maybeLoan) {
            case Some(loan) => complete(loan)
            case None => complete(StatusCodes.NotFound)
          }

        }
      } ~
      post {
        pathPrefix("loans") {
          entity(as[LoanRequest]) { loanRequest =>
            val saved: Future[Loan] = saveLoan(loanRequest)
            onComplete(saved) { _ match {
                case Success(loan) => complete(loan)
                case Failure(ex) => complete(StatusCodes.BadRequest)
              }
            }
          }
        }
      } ~
      post {
        pathPrefix("loans" / JavaUUID / "offers") { loanId => {
          entity(as[OfferRequest]) { offerRequest =>
            val saved: Future[Offer] = saveOffer(LoanId(loanId), offerRequest)
            onComplete(saved) { _ match {
                case Success(offer) => complete(offer)
                case Failure(ex) => complete(StatusCodes.BadRequest)
              }
            }
          }
        }
        }
      } ~
      get {
        pathPrefix("loans" / JavaUUID / "offers" / "current"){ loanId =>
          val maybeOffer:Future[Option[CurrentOffer]] = getCurrentOffer(LoanId(loanId))
          onSuccess(maybeOffer) {
            case Some(currentOffer) => complete(currentOffer)
            case None => complete(StatusCodes.NotFound)
          }
        }
      }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("mm-actor-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println("server online - press ENTER to stop")

    StdIn.readLine()
    bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
  }
}
