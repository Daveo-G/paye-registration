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

package services

import common.exceptions.DBExceptions.MissingRegDocument
import fixtures.RegistrationFixture
import models.{CompanyDetails, Employment}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import testHelpers.PAYERegSpec

import scala.concurrent.Future

class RegistrationServiceSpec extends PAYERegSpec with RegistrationFixture {

  class Setup {
    object Service extends RegistrationSrv {
      override val registrationRepository = mockRegistrationRepository
    }
  }

  "Calling newPAYERegistration" should {

    "return a DBDuplicate response when the database already has a PAYERegistration" in new Setup {
      when(mockRegistrationRepository.retrieveRegistration(ArgumentMatchers.contains("AC123456"))).thenReturn(Future.successful(Some(validRegistration)))

      val actual = await(Service.createNewPAYERegistration("AC123456", validRegistration.internalID))
      actual shouldBe validRegistration
    }

    "return a DBSuccess response when the Registration is correctly inserted into the database" in new Setup {
      when(mockRegistrationRepository.retrieveRegistration(ArgumentMatchers.contains("AC123456"))).thenReturn(Future.successful(None))
      when(mockRegistrationRepository.createNewRegistration(ArgumentMatchers.contains("AC123456"), ArgumentMatchers.any[String]())).thenReturn(Future.successful(validRegistration))

      val actual = await(Service.createNewPAYERegistration("AC123456", "09876"))
      actual shouldBe validRegistration
    }
  }

  "Calling fetchPAYERegistration" should {

    "return a None response when there is no registration in mongo for the reg ID" in new Setup {
      when(mockRegistrationRepository.retrieveRegistration(ArgumentMatchers.contains("AC123456"))).thenReturn(Future.successful(None))

      val actual = await(Service.fetchPAYERegistration("AC123456"))
      actual shouldBe None
    }

    "return a failed future with exception when the database errors" in new Setup {
      val exception = new RuntimeException("tst message")
      when(mockRegistrationRepository.retrieveRegistration(ArgumentMatchers.contains("AC123456"))).thenReturn(Future.failed(exception))

      intercept[RuntimeException] { await(Service.fetchPAYERegistration("AC123456")) }
    }

    "return a registration there is one matching the reg ID in mongo" in new Setup {
      when(mockRegistrationRepository.retrieveRegistration(ArgumentMatchers.contains("AC123456"))).thenReturn(Future.successful(Some(validRegistration)))

      val actual = await(Service.fetchPAYERegistration("AC123456"))
      actual shouldBe Some(validRegistration)
    }
  }

  "Calling getCompanyDetails" should {

    "return a None response when there is no registration in mongo for the reg ID" in new Setup {
      when(mockRegistrationRepository.retrieveCompanyDetails(ArgumentMatchers.contains("AC123456")))
        .thenReturn(Future.successful(None))

      val actual = await(Service.getCompanyDetails("AC123456"))
      actual shouldBe None
    }

    "return a failed future with exception when the database errors" in new Setup {
      val exception = new RuntimeException("tst message")
      when(mockRegistrationRepository.retrieveCompanyDetails(ArgumentMatchers.contains("AC123456")))
        .thenReturn(Future.failed(exception))

      intercept[RuntimeException] { await(Service.getCompanyDetails("AC123456")) }
    }

    "return a registration there is one matching the reg ID in mongo" in new Setup {
      when(mockRegistrationRepository.retrieveCompanyDetails(ArgumentMatchers.contains("AC123456")))
        .thenReturn(Future.successful(Some(validCompanyDetails)))

      val actual = await(Service.getCompanyDetails("AC123456"))
      actual shouldBe validRegistration.companyDetails
    }
  }

  "Calling upsertCompanyDetails" should {

    "return a DBNotFound response when there is no registration in mongo with the user's ID" in new Setup {
      when(mockRegistrationRepository.upsertCompanyDetails(ArgumentMatchers.contains("AC123456"), ArgumentMatchers.any[CompanyDetails]()))
        .thenReturn(Future.failed(new MissingRegDocument("AC123456")))

      intercept[MissingRegDocument] { await(Service.upsertCompanyDetails("AC123456", validCompanyDetails)) }
    }

    "return a DBSuccess response when the company details are successfully updated" in new Setup {
      val exception = new RuntimeException("tst message")
      when(mockRegistrationRepository.upsertCompanyDetails(ArgumentMatchers.contains("AC123456"), ArgumentMatchers.any[CompanyDetails]()))
        .thenReturn(Future.successful(validCompanyDetails))

      val actual = await(Service.upsertCompanyDetails("AC123456", validCompanyDetails))
      actual shouldBe validCompanyDetails
    }
  }

  "Calling getEmployment" should {

    "return a None response when there is no registration in mongo for the reg ID" in new Setup {
      when(mockRegistrationRepository.retrieveEmployment(ArgumentMatchers.contains("AC123456")))
        .thenReturn(Future.successful(None))

      val actual = await(Service.getEmployment("AC123456"))
      actual shouldBe None
    }

    "return a failed future with exception when the database errors" in new Setup {
      val exception = new RuntimeException("tst message")
      when(mockRegistrationRepository.retrieveEmployment(ArgumentMatchers.contains("AC123456")))
        .thenReturn(Future.failed(exception))

      intercept[RuntimeException] { await(Service.getEmployment("AC123456")) }
    }

    "return a registration there is one matching the reg ID in mongo" in new Setup {
      when(mockRegistrationRepository.retrieveEmployment(ArgumentMatchers.contains("AC123456")))
        .thenReturn(Future.successful(Some(validEmployment)))

      val actual = await(Service.getEmployment("AC123456"))
      actual shouldBe validRegistration.employment
    }
  }

  "Calling upsertEmployment" should {

    "return a DBNotFound response when there is no registration in mongo with the user's ID" in new Setup {
      when(mockRegistrationRepository.upsertEmployment(ArgumentMatchers.contains("AC123456"), ArgumentMatchers.any[Employment]()))
        .thenReturn(Future.failed(new MissingRegDocument("AC123456")))

      intercept[MissingRegDocument] { await(Service.upsertEmployment("AC123456", validEmployment)) }
    }

    "return a DBSuccess response when the company details are successfully updated" in new Setup {
      val exception = new RuntimeException("tst message")
      when(mockRegistrationRepository.upsertEmployment(ArgumentMatchers.contains("AC123456"), ArgumentMatchers.any[Employment]()))
        .thenReturn(Future.successful(validEmployment))

      val actual = await(Service.upsertEmployment("AC123456", validEmployment))
      actual shouldBe validEmployment
    }
  }

}
