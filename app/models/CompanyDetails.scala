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

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsObject, JsValue, Json, Writes, __}

case class CompanyDetails(companyName: String,
                          tradingName: Option[String],
                          roAddress: Address,
                          ppobAddress: Address,
                          businessContactDetails: DigitalContactDetails)

case class Address(line1: String,
                   line2: String,
                   line3: Option[String],
                   line4: Option[String],
                   postCode: Option[String],
                   country: Option[String] = None)

object Address {
  implicit val format = Json.format[Address]

  val writesDES: Writes[Address] = new Writes[Address] {
    override def writes(address: Address): JsValue = {
      val successWrites = (
        (__ \ "addressLine1").write[String] and
        (__ \ "addressLine2").write[String] and
        (__ \ "addressLine3").writeNullable[String] and
        (__ \ "addressLine4").writeNullable[String] and
        (__ \ "postcode").writeNullable[String] and
        (__ \ "country").writeNullable[String]
      )(unlift(Address.unapply))

      Json.toJson(address)(successWrites).as[JsObject]
    }
  }
}

object CompanyDetails extends CompanyDetailsValidator {

  implicit val format: Format[CompanyDetails] = (
      (__ \ "companyName").format[String](companyNameValidator) and
      (__ \ "tradingName").formatNullable[String](companyNameValidator) and
      (__ \ "roAddress").format[Address] and
      (__ \ "ppobAddress").format[Address] and
      (__ \ "businessContactDetails").format[DigitalContactDetails]
    )(CompanyDetails.apply, unlift(CompanyDetails.unapply))

}
