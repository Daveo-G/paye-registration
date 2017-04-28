/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package audit

import models.submission.{DESSubmission, TopUpDESSubmission}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.play.test.UnitSpec

class DesTopUpAuditEventDetailSpec extends UnitSpec {
  "DesTopUpAuditEventDetail" should {
    val regId = "123456789"
    val ackRef = "ackRef"

    "construct full json as per definition" when {
      "incorporation is accepted" in {
        val validTopUpDESSubmission = TopUpDESSubmission(ackRef, "accepted", Some("AA123456"))

        val expected = Json.parse(
          s"""
             |{
             |   "journeyId": "$regId",
             |   "acknowledgementReference": "$ackRef",
             |   "status": "accepted",
             |   "payAsYouEarn": {
             |     "crn": "AA123456"
             |   }
             |}
          """.stripMargin)

        val testModel = DesTopUpAuditEventDetail(
          regId,
          Json.toJson[DESSubmission](validTopUpDESSubmission).as[JsObject]
        )
        Json.toJson(testModel)(DesTopUpAuditEventDetail.writes) shouldBe expected
      }

      "incorporation is rejected" in {
        val validTopUpDESSubmission = TopUpDESSubmission(ackRef, "rejected", None)

        val expected = Json.parse(
          s"""
             |{
             |   "journeyId": "$regId",
             |   "acknowledgementReference": "$ackRef",
             |   "status": "rejected"
             |}
          """.stripMargin)

        val testModel = DesTopUpAuditEventDetail(
          regId,
          Json.toJson[DESSubmission](validTopUpDESSubmission).as[JsObject]
        )
        Json.toJson(testModel)(DesTopUpAuditEventDetail.writes) shouldBe expected
      }
    }
  }
}
