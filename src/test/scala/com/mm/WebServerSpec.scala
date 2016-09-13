package com.mm

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import com.mm.controller.{JsonSupport, WebServer}
import com.mm.model.{Loan, LoanRequest}
import org.json4s.ext.JavaTypesSerializers
import org.json4s.{Formats, NoTypeHints}
import org.json4s.jackson.Serialization
import org.scalatest.{Matchers, WordSpec}
//import org.json4s.ext




class WebServerSpec(_system: ActorSystem) extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport {

//  implicit val json4sJacksonFormats: Formats = Serialization.formats(NoTypeHints) ++ JavaTypesSerializers.all
  def this() = this(ActorSystem("MySpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


  "The service" should {

    "return a greeting for GET requests to the root path" in {
      Get("/hello") ~> WebServer.route ~> check {
        responseAs[String] shouldEqual "<h1>Hello Akka-Http</h1>"
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

  }

}
