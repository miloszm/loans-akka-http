package com.mm

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import com.mm.controller.WebServer
import org.scalatest.{Matchers, WordSpec}

class WebServerSpec(_system: ActorSystem) extends WordSpec with Matchers with ScalatestRouteTest {

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
  }

}
