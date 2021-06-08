package database

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import database.requests.RequestsFlood._

class FloodSimulation extends Simulation {

  val domain = "challengers.flood.io"
  val longPauseMin = 10
  val longPauseMax = 15

  val httpProtocol = http
    .baseUrl("https://" + domain)
    .inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36")

  val user_scenario = scenario("Test")
    .exec(MainPage.openHomePage)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.stepOne)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.stepTwo)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.stepThree)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.stepFour)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.stepFive)


  setUp(user_scenario.inject(atOnceUsers(1))).protocols(httpProtocol)
}
