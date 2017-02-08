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

import java.time.LocalDate

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class PAYERegistrationSpec extends UnitSpec with JsonFormatValidation {

  "Creating a PAYERegistration model from Json" should {
    "complete successfully from full Json" in {

      val date = LocalDate.of(2016, 12, 20)

      val json = Json.parse(
        s"""
           |{
           |  "registrationID":"12345",
           |  "internalID" : "09876",
           |  "formCreationTimestamp":"2016-05-31",
           |  "companyDetails":
           |    {
           |      "crn":"Ac123456",
           |      "companyName":"Test Company",
           |      "tradingName":"Test Trading Name",
           |      "roAddress": {
           |        "line1":"14 St Test Walk",
           |        "line2":"Testley",
           |        "line3":"Testford",
           |        "line4":"Testshire",
           |        "postCode":"TE1 1ST",
           |        "country":"UK"
           |      }
           |    },
           |  "employment": {
           |    "first-payment-date": "$date",
           |    "cis": true,
           |    "employees": true,
           |    "ocpn": true
           |  }
           |}
        """.stripMargin)

      val tstPAYERegistration = PAYERegistration(
        registrationID = "12345",
        internalID = "09876",
        formCreationTimestamp = "2016-05-31",
        companyDetails = Some(
          CompanyDetails(
            crn = Some("Ac123456"),
            companyName = "Test Company",
            tradingName = Some("Test Trading Name"),
            Address("14 St Test Walk", "Testley", Some("Testford"), Some("Testshire"), Some("TE1 1ST"), Some("UK"))
          )
        ),
        employment = Some(Employment(employees = true, Some(true), subcontractors = true, firstPaymentDate = date))
      )

      Json.fromJson[PAYERegistration](json) shouldBe JsSuccess(tstPAYERegistration)
    }

    "complete successfully from Json with no companyDetails" in {
      val json = Json.parse(
        s"""
           |{
           |  "registrationID":"12345",
           |  "internalID" : "09876",
           |  "formCreationTimestamp":"2016-05-31"
           |}
        """.stripMargin)

      val tstPAYERegistration = PAYERegistration(
        registrationID = "12345",
        internalID = "09876",
        formCreationTimestamp = "2016-05-31",
        companyDetails = None,
        None
      )

      Json.fromJson[PAYERegistration](json) shouldBe JsSuccess(tstPAYERegistration)
    }

    "fail from json without registrationID" in {
      val json = Json.parse(
        s"""
           |{
           |  "internalID" : "09876",
           |  "formCreationTimestamp":"2016-05-31",
           |  "companyDetails":
           |    {
           |      "crn":"Ac123456",
           |      "companyName":"Test Company",
           |      "tradingName":"Test Trading Name",
           |      "roAddress": {
           |        "line1":"14 St Test Walk",
           |        "line2":"Testley",
           |        "line3":"Testford",
           |        "line4":"Testshire",
           |        "postCode":"TE1 1ST",
           |        "country":"UK"
           |      }
           |    }
           |}
        """.stripMargin)

      val result = Json.fromJson[PAYERegistration](json)
      shouldHaveErrors(result, JsPath() \ "registrationID", Seq(ValidationError("error.path.missing")))
    }
  }
}
