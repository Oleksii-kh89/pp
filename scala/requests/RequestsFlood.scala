package database.requests

object RequestsFlood {

  val authenticityTokenRegex = """name="authenticity_token" type="hidden" value="(.+?)""""
  val stepIdRegex = """name="challenger\[step_id\]" type="hidden" value="(.+?)""""
  val orderSelectedRegex = """<input class="radio_buttons optional".+? value="(.+?)" />"""
  val orderRegex = """<input id="challenger_order_[0-9]+" name="(.+?)" type="hidden" value="[0-9]+" />"""
  val orderValueRegex = """<input id="challenger_order_[0-9]+" name=".+?" type="hidden" value="([0-9]+)" />"""

  object MainPage {
    def openHomePage = {
      exec(http("Open Home Page")
        .get("/")
        .check(regex(authenticityTokenRegex).find.saveAs("authenticityToken"))
        .check(regex(stepIdRegex).find.saveAs("challengerStepID")))
    }
  }

  object Steps {
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
        .check(regex(stepIdRegex).find.saveAs("challengerStepID")))

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
        .check(regex(stepIdRegex).find.saveAs("challengerStepID"))
        .check(css(".collection_radio_buttons").findAll.saveAs("challengerLargestOrderAll"))
        .check(regex(orderSelectedRegex).findAll.saveAs("challengerOrderSelectedAll"))
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
        .check(regex(stepIdRegex).find.saveAs("challengerStepID"))
        .check(regex(authenticityTokenRegex).find.saveAs("authenticityToken")))
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
  }
}
