package com.mm

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import com.mm.controller.{JsonSupport, WebServer}
import com.mm.model._
import org.scalatest.{Matchers, WordSpec}




class WebServerSpec(_system: ActorSystem) extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport {

  def this() = this(ActorSystem("MySpec"))

  var loanId:Option[LoanId] = None

  override protected def beforeAll(): Unit = {
    val request = LoanRequest(1000.0, 100)

    Post("/loans", request) ~> WebServer.route ~> check {
      val response = responseAs[Loan]
      loanId = Some(response.loanId)
    }

  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


  "The service" should {

    "return a greeting for GET requests to the root path" in {

      Get("/info") ~> WebServer.route ~> check {
        responseAs[String] shouldEqual "<h1>peer to peer lending system</h1>"
      }
    }

    "return a loan for GET requests with loan id in the path" in {

      Get(s"/loan/${loanId.get.loanId.toString}") ~> WebServer.route ~> check {
        val loan = responseAs[Loan]
        loan.amount shouldEqual (1000)
        loan.durationInDays shouldEqual (100)
        loan.loanId shouldEqual (loanId.get)
      }
    }

    "create loan for POST request to loans" in {

      val request = LoanRequest(200.0, 365)

      Post("/loans", request) ~> WebServer.route ~> check {
        val response = responseAs[Loan]
        response.amount shouldEqual (200)
        response.durationInDays shouldEqual (365)
      }
    }

    "create offer for a loan with POST request to loans/loadid/offers" in {

      val offerRequest = OfferRequest(100.0, 5.0)

      Post(s"/loans/${loanId.get.loanId.toString}/offers", offerRequest) ~> WebServer.route ~> check {
        val offer = responseAs[Offer]
        offer.amount shouldEqual (100)
        offer.interestRate shouldEqual (5.0)
        offer.loanId shouldEqual (loanId.get)
      }
    }

    "get combined offer for a given loan" in {

      Post(s"/loans/${loanId.get.loanId.toString}/offers", OfferRequest(500.0, 8.6)) ~> WebServer.route ~> check {}
      Get(s"/loans/${loanId.get.loanId.toString}/offers/current") ~> WebServer.route ~> check {
        val currentOffer = responseAs[CurrentOffer]
        currentOffer.amount shouldEqual (600)
        currentOffer.combinedInterest shouldEqual (8.0)
      }
    }

  }

}
