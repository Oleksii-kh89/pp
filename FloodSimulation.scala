package database

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class FloodSimulation extends Simulation {

  val domain = "challengers.flood.io"
  val pauseMin = 10
  val pauseMax = 15
  val duration = 30
  val numberOfUsers = 10

  val httpProtocol = http
    .baseUrl("https://" + domain)
    .inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36")

  val user_scenario = scenario("Test")
    .exec(MainPage.openHomePage)
    .pause(pauseMin, pauseMax)
    .exec(Steps.stepOne)
    .pause(pauseMin, pauseMax)
    .exec(Steps.stepTwo)
    .pause(pauseMin, pauseMax)
    .exec(Steps.stepThree)
    .pause(pauseMin, pauseMax)
    .exec(Steps.stepFour)
    .pause(pauseMin, pauseMax)
    .exec(Steps.stepFive)


  setUp(user_scenario.inject(
    rampUsers(numberOfUsers) during (duration seconds)
  ).protocols(httpProtocol)
  )
}
