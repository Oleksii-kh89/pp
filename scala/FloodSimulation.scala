CH

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class FloodSimulation extends Simulation {

  val domain = "challengers.flood.io"
  val longPauseMin = 10
  val longPauseMax = 15
  val numberUsers = Integer.getInteger("users", 1)
  val myRamp = java.lang.Long.getLong("ramp", 0L)

  val httpProtocol = http
    .baseUrl("https://" + domain)
    .inferHtmlResources()
    .inferHtmlResources(BlackList(""".*\.js""", """.*css.*""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36")
    .disableFollowRedirect

  val user_scenario = scenario("Test")
    .exec(HomePage.loadHomePage)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.getStep2)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.postStep2)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.getStep3)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.postStep3)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.getStep4)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.postStep4)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.getCode)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.postStep5)

  setUp(user_scenario.inject(rampUsers(numberUsers).during(myRamp))).protocols(httpProtocol)
}
