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

package helpers

import java.time.format.DateTimeParseException
import java.time.{LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}

import uk.gov.hmrc.play.test.UnitSpec

class DateHelperSpec extends UnitSpec {

  object TestDateHelper

  "formatTimeStamp" should {
    "Correctly format a ZonedDateTime to a String" in {
      // date time of 12:35 on 20th Feb, 2017
      val tstDate = ZonedDateTime.of(LocalDateTime.of(2017, 2, 20, 12, 35, 0), ZoneId.of("Z"))

      DateHelper.formatTimestamp(tstDate) shouldBe "2017-02-20T12:35:00Z"
    }

    "Correctly format a ZonedDateTime with nanoseconds to a String" in {
      // date time of 12:35.3 on 20th Feb, 2017
      val tstDate = ZonedDateTime.of(LocalDateTime.of(2017, 2, 20, 12, 35, 0, 300000000), ZoneId.of("Z"))

      DateHelper.formatTimestamp(tstDate) shouldBe "2017-02-20T12:35:00Z"
    }
  }

  "getDateFromTimestamp" should {
    "Correctly return a ZonedDateTime from a String which has the correct format" in {
      val timestamp = ZonedDateTime.of(LocalDateTime.of(2017, 2, 20, 12, 35, 0), ZoneId.of("Z"))

      DateHelper.getDateFromTimestamp("2017-02-20T12:35:00Z") shouldBe timestamp
    }

    "return an Exception when the input String has NOT the correct format" in {
      a[DateTimeParseException] shouldBe thrownBy(DateHelper.getDateFromTimestamp("2017-02-20T12:35:00XXX"))
    }
  }

  "DateHelper" should {
    "parse and convert an Argentinian time" in {
      val timestamp = ZonedDateTime.of(LocalDateTime.of(2017, 2, 20, 12, 35, 0), ZoneId.of("America/Argentina/Buenos_Aires"))

      DateHelper.formatTimestamp(timestamp) shouldBe "2017-02-20T15:35:00Z"
    }
  }

  "zonedDateTimeFromString" should {
    "return ZonedDateTime from a valid ZoneDateTimeStamp passed in" in {
      val timestamp = "2015-02-20T15:30:00Z"
      val zonedDateTime = ZonedDateTime.of(LocalDateTime.of(2015,2,20,15,30),ZoneOffset.UTC)
      DateHelper.zonedDateTimeFromString(timestamp) shouldBe zonedDateTime
    }
  }
}
