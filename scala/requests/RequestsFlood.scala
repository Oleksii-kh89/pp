package database.requests

import io.gatling.core.Predef.{css, exec, jsonPath, regex, scenario}
import io.gatling.http.Predef.http
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object RequestsFlood {

  object MainPage {
    def openHomePage = {
      exec(http("Open Home Page")
        .get("/")
        .check(regex("""name="authenticity_token" type="hidden" value="(.+?)"""").find.saveAs("authenticityToken"))
        .check(regex("""name="challenger\[step_id\]" type="hidden" value="(.+?)"""").find.saveAs("challengerStepID")))
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
  }
}
