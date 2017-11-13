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

package models.validation

import java.time.LocalDate
import play.api.libs.json._

import scala.collection.Seq

object APIValidation extends BaseJsonFormatting {

  private val emailRegex              = """^(?!.{71,})([-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\.[a-zA-Z]{2,4})$"""
  private val phoneNumberRegex        = """^[0-9 ]{1,20}$"""
  private val nameRegex               = """^[A-Za-z 0-9\-']{1,100}$"""
  private val natureOfBusinessRegex   = """^[A-Za-z 0-9\-,/&']{1,100}$"""
  private val tradingNameRegex        = """^[A-Za-z0-9\-,.()/&'!][A-Za-z 0-9\-,.()/&'!]{0,34}$"""
  private val completionCapacityRegex = """^[A-Za-z0-9 '\-]{1,100}$"""

  private val addressLineRegex  = """^[a-zA-Z0-9,.\(\)/&'\"\-\\]{1}[a-zA-Z0-9, .\(\)/&'\"\-\\]{0,26}$"""
  private val addressLine4Regex = """^[a-zA-Z0-9,.\(\)/&'\"\-\\]{1}[a-zA-Z0-9, .\(\)/&'\"\-\\]{0,17}$"""
  private val postcodeRegex     = """^[A-Z]{1,2}[0-9][0-9A-Z]? [0-9][A-Z]{2}$"""
  private val countryRegex      = """^[A-Za-z0-9]{1}[A-Za-z 0-9]{0,19}$"""

  private val validNinoFormat    = "[[A-Z]&&[^DFIQUV]][[A-Z]&&[^DFIQUVO]]\\d{2}\\d{2}\\d{2}[A-D]{1}"
  private val directorNameRegex  = """^[A-Za-z 0-9\-';]{1,100}$"""
  private val directorTitleRegex = """^[A-Za-z ]{1,20}$"""

  private val invalidPrefixes = List("BG", "GB", "NK", "KN", "TN", "NT", "ZZ")


  private val minDate = LocalDate.of(1900,1,1)

  private def isValidPhoneNumber(phoneNumber: String): Boolean = {
    def isValidNumberCount(s: String): Boolean = phoneNumber.replaceAll(" ", "").matches("[0-9]{10,20}")
    isValidNumberCount(phoneNumber) & phoneNumber.matches(phoneNumberRegex)
  }

  private def beforeMinDate(date: LocalDate): Boolean = {
    date.isBefore(minDate)
  }

  private def hasValidPrefix(nino: String) = !invalidPrefixes.exists(nino.startsWith)

  private def isValidNino(nino: String) = nino.nonEmpty && hasValidPrefix(nino) && nino.matches(validNinoFormat)

  override val phoneNumberReads      = Reads.StringReads.filter(JsonValidationError("Invalid phone number pattern"))(isValidPhoneNumber)

  override val emailAddressReads     = Reads.StringReads.filter(JsonValidationError("Invalid email pattern"))(_.matches(emailRegex))

  override val nameReads             = Reads.StringReads.filter(JsonValidationError("Invalid name"))(_.matches(nameRegex))

  override val natureOfBusinessReads = Reads.StringReads.filter(JsonValidationError("Invalid nature of business"))(_.matches(natureOfBusinessRegex))

  override val completionCapacityReads: Reads[String] = Reads.StringReads.filter(JsonValidationError("bad string"))(_.matches(completionCapacityRegex))

  override val tradingNameFormat     = new Format[String] {
    override def reads(json: JsValue) = json match {
      case JsString(tradingName) => if(tradingName.matches(tradingNameRegex)) {
        JsSuccess(tradingName)
      } else {
        JsError(Seq(JsPath() -> Seq(JsonValidationError("Invalid trading name"))))
      }
      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.jsstring"))))
    }

    override def writes(o: String) = Writes.StringWrites.writes(o)
  }

  //Address validation
  override val addressLineValidate  = Reads.StringReads.filter(JsonValidationError("Invalid address line pattern"))(_.matches(addressLineRegex))
  override val addressLine4Validate = Reads.StringReads.filter(JsonValidationError("Invalid address line 4 pattern"))(_.matches(addressLine4Regex))
  override val postcodeValidate     = Reads.StringReads.filter(JsonValidationError("Invalid postcode"))(_.matches(postcodeRegex))
  override val countryValidate      = Reads.StringReads.filter(JsonValidationError("Invalid country"))(_.matches(countryRegex))

  override val firstPaymentDateFormat: Format[LocalDate] = {
    val rds = Reads.DefaultLocalDateReads.filter(JsonValidationError("invalid date - too early"))(date => !beforeMinDate(date))
    Format(rds, Writes.DefaultLocalDateWrites)
  }

  override val directorNameFormat   = readToFmt(Reads.StringReads.filter(JsonValidationError("error.pattern"))(_.matches(directorNameRegex)))
  override val directorTitleFormat  = readToFmt(Reads.StringReads.filter(JsonValidationError("error.pattern"))(_.matches(directorTitleRegex)))
  override val directorNinoFormat   = readToFmt(Reads.StringReads.filter(JsonValidationError("error.pattern"))(isValidNino))
}
