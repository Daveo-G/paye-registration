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

package models

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class DirectorSpec extends UnitSpec with JsonFormatValidation {

  "Director" should {
    val tstJson = Json.parse(
      s"""
         |{
         |  "nino":"SR098765C",
         |  "director": {
         |    "name":"Thierry Henry"
         |  }
         |}
      """.stripMargin)

    val tstName = "Thierry Henry"

    val tstModel = Director(tstName, Some("SR098765C"))

    "read from json with full data" in {
      Json.fromJson[Director](tstJson) shouldBe JsSuccess(tstModel)
    }

    "write to json with full data" in {
      Json.toJson[Director](tstModel) shouldBe tstJson
    }

    val tstEmptyJson = Json.parse(s"""{"director":{"name":""}}""".stripMargin)
    val tstEmptyModel = Director("", None)
    "read from json with empty data" in {
      Json.fromJson[Director](tstEmptyJson) shouldBe JsSuccess(tstEmptyModel)
    }

    "write to json with empty data" in {
      Json.toJson[Director](tstEmptyModel) shouldBe tstEmptyJson
    }

    "fail from Json with invalid nino" in {
      def json(nino: String) = Json.parse(
        s"""
           |{
           |  "nino":"$nino",
           |  "director": {
           |    "name":"Thierry Henry"
           |  }
           |}
      """.stripMargin)

      def result(nino: String) = Json.fromJson[Director](json(nino))

      shouldHaveErrors(result("BG098765B"), JsPath() \ "nino", Seq(ValidationError("error.pattern")))

      shouldHaveErrors(result("AD098765D"), JsPath() \ "nino", Seq(ValidationError("error.pattern")))

      shouldHaveErrors(result("CV098765C"), JsPath() \ "nino", Seq(ValidationError("error.pattern")))

      shouldHaveErrors(result("SR098765Z"), JsPath() \ "nino", Seq(ValidationError("error.pattern")))

      shouldHaveErrors(result("SR098765Z"), JsPath() \ "nino", Seq(ValidationError("error.pattern")))

      shouldHaveErrors(result("SR 09 87 65 C"), JsPath() \ "nino", Seq(ValidationError("error.pattern")))
    }
  }
}
