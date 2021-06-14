import io.gatling.core.Predef._
import io.gatling.http.Predef._

class FloodSimulation extends Simulation {

  //val domain = "challengers.flood.io"
  val longPauseMin = 10
  val longPauseMax = 15

  val httpProtocol = http
    .baseUrl("https://challengers.flood.io")
    //.inferHtmlResources(BlackList(""""https://fonts.googleapis.com/css?family=Lobster|Lobster+Two""""))
    .inferHtmlResources(BlackList(""".*\.js""", """.*css.*""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36")
    .disableFollowRedirect


  object HomePage {
    def loadHomePage = {
      exec(http("Open Home Page")
        .get("/"))
    }

  }

  object Steps {
    def getStep2 = {
      exec(http("Get start")
        .get("/step/2")
        .check(regex("""name="authenticity_token" type="hidden" value="(.+?)"""").find.saveAs("authenticityToken"))
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        .check(regex("""id="challenger_age" name="challenger\[age\]""").findRandom.saveAs("challengerAge")))

    }

    def postStep2 = {
      exec(http("Post step , age")
        .post("/start")

        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "2"),
          ("challenger[age]", "${challengerAge}"),
          ("commit", "Next")
        ))
        .check(status.is(302)))
    }

    def getStep3 = {
      exec(http("Get value")
        .get("/step/3")
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        //.check(css(".collection_radio_buttons").findAll.saveAs("challengerLargestOrderAll"))
        .check(regex("""(<span class="radio"><input class="radio_buttons optional".+? value=".+?" .+?>[0-9]+</label></span>)""").findAll.transform((a) => a.toArray.max).saveAs("challengerLargestOrder"))
        //.check(regex("""<input class="radio_buttons optional".+? value="(.+?)" />""").findAll.saveAs("challengerOrderSelectedAll"))
        .check(regex("""<input class="radio_buttons optional".+? value="(.+?)" />""").findAll.transform((a) => a.toArray.max).saveAs("challengerOrderSelected"))
      )
      /*.exec(session => {
        val challengerLargestOrder = session("challengerLargestOrderAll").as[Seq[String]].maxBy(x => x.toInt)
        val challengerLargestOrderIndex = session("challengerLargestOrderAll").as[Seq[String]].indexOf(challengerLargestOrder)
        val challengerOrderSelected = session("challengerOrderSelectedAll").as[Seq[String]].apply(challengerLargestOrderIndex)
        session.setAll(("challengerLargestOrder", challengerLargestOrder), ("challengerOrderSelected", challengerOrderSelected))
      })*/
    }

    def postStep3 = {
      exec(http("Post step 3, value")
        .post("/start")
        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "3"),
          ("challenger[largest_order]", "${challengerLargestOrder}"),
          ("challenger[order_selected]", "${challengerOrderSelected}"),
          ("commit", "Next")
        ))
        .check(status.is(302)))
    }

    def get4 = {
      exec(http("Get, step 4")
        .get("/step/4")
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        .check(regex("""<input id="challenger_order_[0-9]+" name="(.+?)" type="hidden" value="[0-9]+" />""").findAll.saveAs("challengerOrderValue"))
        .check(regex("""<input id="challenger_order_[0-9]+" name=".+?" type="hidden" value="([0-9]+)" />""").findAll.saveAs("challengerOrderNumber"))
      )
        .exec(session => {
          val challengerOrderValue = session("challengerOrderValue").as[Seq[String]]
          val challengerOrderNumber = session("challengerOrderNumber").as[Seq[String]]
          val challengerOrderSeq = challengerOrderNumber.zip(challengerOrderValue)

          session.set("challengerOrderSeq", challengerOrderSeq)
        })

    }

    def post4 = {
      exec(http("Post step 4, Wait")
        .post("/start")

        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "4"),
          ("commit", "Next")
        )).formParamSeq("${challengerOrderSeq}")
        .check(status.is(302)))
    } /*
    def postStep4 = {
      exec(http("Step 4, Wait")
        .post("/start")
        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "4"),
          ("commit", "Next")
        )).formParamSeq("${challengerOrderSeq}"))
    }
    def getStep4 = {
      exec(http("Step 4, Wait")
        .get("/step/4")
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        .check(regex("""name="authenticity_token" type="hidden" value="(.+?)"""").find.saveAs("authenticityToken")))
        .exec(http("Get one time token")
          .get("/code")
          .check(jsonPath("$.code").saveAs("oneTimeToken")))
    }*/

    def getCode = {
      exec(http("Get one time token")
        .get("/code")
        .check(jsonPath("$.code").saveAs("oneTimeToken")))


    }

    def postStep5 = {
      exec(http("Post step 5, choose token")
        .post("/start")
        .check(status.is(302))
        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "5"),
          ("challenger[one_time_token]", "${oneTimeToken}"),
          ("commit", "Next")))
        .check(status.is(302)))
    }

  }
  /*object MainPage {

    def openHomePage = {
      exec(http("Open Home Page")
        .get("/")
        .check(regex("""name="authenticity_token" type="hidden" value="(.+?)"""").find.saveAs("authenticityToken"))
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID")))

    }
  }
  object Steps {

    def postForStepOne = {
      exec(http("Post to start")
        .post("/start")
        .check(regex("""name="authenticity_token" type="hidden" value="(.+?)"""").find.saveAs("authenticityToken"))
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
      )
    }

    def getStepOne = {
      exec(http("Step 1, press Start")
        .get("/step/2")
        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "1"),
          ("commit", "Start")
        ))
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID")))

    }

    def postForStepTwo = {
      exec(http("Post to step 2")
        .post("/start"))
        /*.check(regex("""name="authenticity_token" type="hidden" value="(.+?)"""").find.saveAs("authenticityToken"))
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID")))*/
    }

    def getStepTwo = {
      exec(http("Step 2, choose age")
        .get("/step/3")
        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "2"),
          ("challenger[age]", "31"),
          ("commit", "Next")
        ))
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        .check(css(".collection_radio_buttons").findAll.saveAs("challengerLargestOrderAll"))
        .check(regex("""<input class="radio_buttons optional".+? value="(.+?)" />""").findAll.saveAs("challengerOrderSelectedAll"))
      )
        .exec(session => {
          val challengerLargestOrder = session("challengerLargestOrderAll").as[Seq[String]].maxBy(x => x.toInt)
          val challengerLargestOrderIndex = session("challengerLargestOrderAll").as[Seq[String]].indexOf(challengerLargestOrder)
          val challengerOrderSelected = session("challengerOrderSelectedAll").as[Seq[String]].apply(challengerLargestOrderIndex)
          session.setAll(("challengerLargestOrder", challengerLargestOrder), ("challengerOrderSelected", challengerOrderSelected))
        })

    }

    def postForStepThree = {
      exec(http("Post for step 3")
        .post("/start")
        /*.check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        .check(css(".collection_radio_buttons").findAll.saveAs("challengerLargestOrderAll"))
        .check(regex("""<input class="radio_buttons optional".+? value="(.+?)" />""").findAll.saveAs("challengerOrderSelectedAll"))
      )
        .exec(session => {
          val challengerLargestOrder = session("challengerLargestOrderAll").as[Seq[String]].maxBy(x => x.toInt)
          val challengerLargestOrderIndex = session("challengerLargestOrderAll").as[Seq[String]].indexOf(challengerLargestOrder)
          val challengerOrderSelected = session("challengerOrderSelectedAll").as[Seq[String]].apply(challengerLargestOrderIndex)
          session.setAll(("challengerLargestOrder", challengerLargestOrder), ("challengerOrderSelected", challengerOrderSelected))
        }*/)
    }

    def getStepThree = {
      exec(http("Step 3, choose the biggest num")
        .get("/step/3")
        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "3"),
          ("challenger[largest_order]", "${challengerLargestOrder}"),
          ("challenger[order_selected]", "${challengerOrderSelected}"),
          ("commit", "Next")
        ))
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        .check(regex("""<input id="challenger_order_[0-9]+" name="(.+?)" type="hidden" value="[0-9]+" />""").findAll.saveAs("challengerOrderValue"))
        .check(regex("""<input id="challenger_order_[0-9]+" name=".+?" type="hidden" value="([0-9]+)" />""").findAll.saveAs("challengerOrderNumber"))
      )
        .exec(session => {
          val challengerOrderValue = session("challengerOrderValue").as[Seq[String]]
          val challengerOrderNumber = session("challengerOrderNumber").as[Seq[String]]
          val challengerOrderSeq = challengerOrderNumber.zip(challengerOrderValue)

          session.set("challengerOrderSeq", challengerOrderSeq)
        })

    }

    def postForStepFour = {
      exec(http("Post step 4")
        .post("/start"))

        /*.check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))

        .check(regex("""<input id="challenger_order_[0-9]+" name="(.+?)" type="hidden" value="[0-9]+" />""").findAll.saveAs("challengerOrderValue"))

        .check(regex("""<input id="challenger_order_[0-9]+" name=".+?" type="hidden" value="([0-9]+)" />""").findAll.saveAs("challengerOrderNumber"))
      )
        .exec(session => {
          val challengerOrderValue = session("challengerOrderValue").as[Seq[String]]
          val challengerOrderNumber = session("challengerOrderNumber").as[Seq[String]]
          val challengerOrderSeq = challengerOrderNumber.zip(challengerOrderValue)

          session.set("challengerOrderSeq", challengerOrderSeq)
        }
        )*/
    }

    def getStepFour = {
      exec(http("Step 4, Wait")
        .get("/step/4")

        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "4"),
          ("commit", "Next")
        )).formParamSeq("${challengerOrderSeq}")
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        .check(regex("""name="authenticity_token" type="hidden" value="(.+?)"""").find.saveAs("authenticityToken")))

    }

    def postForStepFive = {
      exec(http("Step 5")
        .post("/start")
        /*.check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        .check(regex("""name="authenticity_token" type="hidden" value="(.+?)"""").find.saveAs("authenticityToken"))*/
      )
    }

    def getCode = {
      exec(http("Code")
        .get("/code")
        .check(jsonPath("$.code").saveAs("oneTimeToken")))
    }

    def getStepFive = {
      exec(http("Step 5, choose token")
        .get("/step/5")

        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "5"),
          ("challenger[one_time_token]", "${oneTimeToken}"),
          ("commit", "Next")
        )))
    }
  }*/
  /*object Steps {
    def stepOne = {
      exec(http("Step 1, press Start")
        .post("/start")
        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "1"),
          ("commit", "Start")
        ))
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID")))

    }

    def stepTwo = {
      exec(http("Step 2, choose age")
        .post("/start")
        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "2"),
          ("challenger[age]", "31"),
          ("commit", "Next")
        ))
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        .check(css(".collection_radio_buttons").findAll.saveAs("challengerLargestOrderAll"))
        .check(regex("""<input class="radio_buttons optional".+? value="(.+?)" />""").findAll.saveAs("challengerOrderSelectedAll"))
      )
        .exec(session => {
          val challengerLargestOrder = session("challengerLargestOrderAll").as[Seq[String]].maxBy(x => x.toInt)
          val challengerLargestOrderIndex = session("challengerLargestOrderAll").as[Seq[String]].indexOf(challengerLargestOrder)
          val challengerOrderSelected = session("challengerOrderSelectedAll").as[Seq[String]].apply(challengerLargestOrderIndex)
          session.setAll(("challengerLargestOrder", challengerLargestOrder), ("challengerOrderSelected", challengerOrderSelected))
        })
    }

    def stepThree = {
      exec(http("Step 3, choose the biggest num")
        .post("/start")
        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "3"),
          ("challenger[largest_order]", "${challengerLargestOrder}"),
          ("challenger[order_selected]", "${challengerOrderSelected}"),
          ("commit", "Next")
        ))
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        .check(regex("""<input id="challenger_order_[0-9]+" name="(.+?)" type="hidden" value="[0-9]+" />""").findAll.saveAs("challengerOrderValue"))
        .check(regex("""<input id="challenger_order_[0-9]+" name=".+?" type="hidden" value="([0-9]+)" />""").findAll.saveAs("challengerOrderNumber"))
      )
        .exec(session => {
          val challengerOrderValue = session("challengerOrderValue").as[Seq[String]]
          val challengerOrderNumber = session("challengerOrderNumber").as[Seq[String]]
          val challengerOrderSeq = challengerOrderNumber.zip(challengerOrderValue)

          session.set("challengerOrderSeq", challengerOrderSeq)
        })

    }

    def stepFour = {
      exec(http("Step 4, Wait")
        .post("/start")
        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "4"),
          ("commit", "Next")
        )).formParamSeq("${challengerOrderSeq}")
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID"))
        .check(regex("""name="authenticity_token" type="hidden" value="(.+?)"""").find.saveAs("authenticityToken")))
        .exec(http("Get one time token")
          .get("/code")
          .check(jsonPath("$.code").saveAs("oneTimeToken")))
    }

    def stepFive = {
      exec(http("Step 5, choose token")
        .post("/start")
        .formParamSeq(Seq(
          ("utf8", "✓"),
          ("authenticity_token", "${authenticityToken}"),
          ("challenger[step_id]", "${challengerStepID}"),
          ("challenger[step_number]", "5"),
          ("challenger[one_time_token]", "${oneTimeToken}"),
          ("commit", "Next"))))
    }
  }*/
  /*val user_scenario = scenario("Test")
    .exec(MainPage.openHomePage)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.postForStepOne)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.getStepOne)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.postForStepTwo)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.getStepTwo)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.postForStepThree)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.getStepThree)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.postForStepFour)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.getStepFour)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.getCode)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.postForStepFive)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.getStepFive)*/
/*
  private def getProperty(propertyName: String, defaultValues: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValues)
  }
  def userCount: Int = getProperty("USERS", "5").toInt
  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt
  def testDuration: Int = getProperty("DURATION", "60").toInt

  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration}")
    println(s"Total Test duration: ${testDuration}")
  }*/

  val user_scenario = scenario("Tes")
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
    .exec(Steps.get4)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.post4)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.getCode)
    .pause(longPauseMin, longPauseMax)
    .exec(Steps.postStep5)


  before {
    println(s"Running test with ${numberUsers} users")
    println(s"Ramping users over ${myRamp}")
    println(s"Total Test duration: ${testDuration}")
  }

    val numberUsers = Integer.getInteger("users", 1)
  val myRamp = java.lang.Long.getLong("ramp", 0L)
  val testDuration = Integer.getInteger("duration", 70)
  setUp(user_scenario.inject(rampUsers(numberUsers).during(myRamp))).protocols(httpProtocol).maxDuration(testDuration)
  //setUp(user_scenario.inject(atOnceUsers(1))).protocols(httpProtocol)
  /*setUp(
    user_scenario.inject(
      nothingFor(5),
      rampUsers(userCount) during  (rampDuration))
  )
    .protocols(httpProtocol).maxDuration(testDuration)*/
}
