/*
 * Copyright 2018 HM Revenue & Customs
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

package connectors

import helpers.PAYERegSpec
import mocks.WSHTTPMock
import models.external.BusinessProfile
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._

import scala.concurrent.Future
import uk.gov.hmrc.http.{ ForbiddenException, HeaderCarrier, NotFoundException }

class BusinessRegistrationConnectorSpec extends PAYERegSpec with WSHTTPMock {
  val mockBusRegConnector = mock[BusinessRegistrationConnector]

  trait Setup {
    val connector = new BusinessRegistrationConnect {
      override val businessRegUrl = "testBusinessRegUrl"
      override val http = mockWSHttp
    }
  }

  lazy val validBusinessRegistrationResponse = BusinessProfile(
    "12345",
    Some("director"),
    "ENG"
  )

  implicit val hc = HeaderCarrier()

  "retrieveCurrentProfile" should {
    "return a a CurrentProfile response if one is found in business registration micro-service" in new Setup {
    mockHttpGet[BusinessProfile]("testUrl", validBusinessRegistrationResponse)

    await(connector.retrieveCurrentProfile("12345")) shouldBe validBusinessRegistrationResponse
  }

    "return a Not Found response when a CurrentProfile record can not be found" in new Setup {
    when(mockWSHttp.GET[BusinessProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
    .thenReturn(Future.failed(new NotFoundException("Bad request")))

    intercept[NotFoundException](await(connector.retrieveCurrentProfile("12345")))
  }

    "return a Forbidden response when a CurrentProfile record can not be accessed by the user" in new Setup {
    when(mockWSHttp.GET[BusinessProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
    .thenReturn(Future.failed(new ForbiddenException("Forbidden")))

    intercept[ForbiddenException](await(connector.retrieveCurrentProfile("12345")))
  }

    "return an Exception response when an unspecified error has occurred" in new Setup {
    when(mockWSHttp.GET[BusinessProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
    .thenReturn(Future.failed(new RuntimeException("Runtime Exception")))

    intercept[RuntimeException](await(connector.retrieveCurrentProfile("12345")))
  }
  }
}
