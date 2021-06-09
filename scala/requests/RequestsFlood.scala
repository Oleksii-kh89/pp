package database.requests

object RequestsFlood {

  val authenticityTokenRegex = """name="authenticity_token" type="hidden" value="(.+?)""""
  val stepIdRegex = """name="challenger\[step_id\]" type="hidden" value="(.+?)""""
  val orderSelectedRegex = """<input class="radio_buttons optional".+? value="(.+?)" />"""
  val orderRegex = """<input id="challenger_order_[0-9]+" name="(.+?)" type="hidden" value="[0-9]+" />"""
  val orderValueRegex = """<input id="challenger_order_[0-9]+" name=".+?" type="hidden" value="([0-9]+)" />"""
  val orderValueLargestRegex = """(<span class="radio"><input class="radio_buttons optional".+? value=".+?" .+?>[0-9]+</label></span>)"""
  val ageRegex = """id="challenger_age" name="challenger\[age\]"""

  object HomePage{
    def loadHomePage = {
      exec(http("Open Home Page")
        .get("/"))
    }
  }

  object Steps {
    def getStep2 = {
      exec(http("Get start")
        .get("/step/2")
        .check(regex(authenticityTokenRegex).find.saveAs("authenticityToken"))
        .check(regex(stepIdRegex).find.saveAs("challengerStepID"))
        .check(regex(ageRegex).findRandom.saveAs("challengerAge")))
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
        .check(regex(stepIdRegex).find.saveAs("challengerStepID"))
        .check(regex(orderValueLargestRegex).findAll.transform((a) => a.toArray.max).saveAs("challengerLargestOrder"))
        .check(regex(orderRegex).findAll.transform((a) => a.toArray.max).saveAs("challengerOrderSelected"))
      )
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

    def getStep4 = {
      exec(http("Get, step 4")
        .get("/step/4")
        .check(regex(stepIdRegex).find.saveAs("challengerStepID"))
        .check(regex(orderRegex).findAll.saveAs("challengerOrderValue"))
        .check(regex(orderValueRegex).findAll.saveAs("challengerOrderNumber"))
      )
        .exec(session => {
          val challengerOrderValue = session("challengerOrderValue").as[Seq[String]]
          val challengerOrderNumber = session("challengerOrderNumber").as[Seq[String]]
          val challengerOrderSeq = challengerOrderNumber.zip(challengerOrderValue)

          session.set("challengerOrderSeq", challengerOrderSeq)
        })
    }
    def postStep4 = {
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
    }

    def getCode = {
      exec(http("Get one time token")
        .get("/code")
        .check(jsonPath("$.code").saveAs("oneTimeToken")))
    }
    def postStep5 = {
      exec(http("Post step 5, choose token")
        .post("/start")
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
}
