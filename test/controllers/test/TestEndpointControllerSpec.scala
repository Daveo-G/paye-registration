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

package controllers.test

import enums.PAYEStatus
import fixtures.{AuthFixture, RegistrationFixture}
import helpers.PAYERegSpec
import models.PAYERegistration
import play.api.libs.json.Json
import repositories.RegistrationMongoRepository
import play.api.test.FakeRequest
import play.api.http.Status
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global
class TestEndpointControllerSpec extends PAYERegSpec with AuthFixture with RegistrationFixture {

  val mockRepo = mock[RegistrationMongoRepository]

  class Setup {
    val controller = new TestEndpointCtrl {
      override val auth = mockAuthConnector
      override val registrationRepository = mockRepo
    }
  }

  override def beforeEach() {
    reset(mockAuthConnector)
    reset(mockRepo)
  }

  "Teardown registration collection" should {
    "return a 200 response for success" in new Setup {
      when(mockRepo.dropCollection(ArgumentMatchers.any())).thenReturn(Future.successful(()))

      val response = await(controller.registrationTeardown()(FakeRequest()))
      status(response) shouldBe Status.OK
    }

    "return a 500 response for failure" in new Setup {
      when(mockRepo.dropCollection(ArgumentMatchers.any())).thenReturn(Future.failed(new RuntimeException("test failure message")))

      val response = await(controller.registrationTeardown()(FakeRequest()))
      status(response) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "Delete Registration" should {
    "return a 200 response for success" in new Setup {
      when(mockRepo.deleteRegistration(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(true))

      val response = await(controller.deleteRegistration("AC123456")(FakeRequest()))
      status(response) shouldBe Status.OK
    }

    "return a 500 response for failure" in new Setup {
      when(mockRepo.deleteRegistration(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.failed(new RuntimeException("test failure message")))

      val response = await(controller.deleteRegistration("AC123456")(FakeRequest()))
      status(response) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "Insert Registration" should {
    "return a 200 response for success" in new Setup {
      when(mockRepo.updateRegistration(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(validRegistration))
      when(mockAuthConnector.getCurrentAuthority()(ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(Some(validAuthority)))

      val response = await(controller.updateRegistration("AC123456")(FakeRequest().withBody(Json.toJson[PAYERegistration](validRegistration))))
      status(response) shouldBe Status.OK
    }

    "return a 500 response for failure" in new Setup {
      when(mockRepo.updateRegistration(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.failed(new RuntimeException("test failure message")))
      when(mockAuthConnector.getCurrentAuthority()(ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(Some(validAuthority)))

      val response = await(controller.updateRegistration("AC123456")(FakeRequest().withBody(Json.toJson[PAYERegistration](validRegistration))))
      status(response) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return a Bad Request response for incorrect Json" in new Setup {
      when(mockAuthConnector.getCurrentAuthority()(ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(Some(validAuthority)))

      val response = await(controller.updateRegistration("AC123456")(FakeRequest().withBody(Json.parse("""{"formCreationTimestamp":"testTimestamp","regID":"invalid"}"""))))
      status(response) shouldBe Status.BAD_REQUEST
    }

    "return a forbidden response for unauthorised" in new Setup {
      when(mockAuthConnector.getCurrentAuthority()(ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(None))

      val response = await(controller.updateRegistration("AC123456")(FakeRequest().withBody(Json.toJson[PAYERegistration](validRegistration))))
      status(response) shouldBe Status.FORBIDDEN
    }
  }

  "newStatus" should {
    "return a 200 response for success" in new Setup {
      when(mockRepo.createNewRegistration(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(validRegistration))

      when(mockRepo.updateRegistrationStatus(ArgumentMatchers.eq("AC123456"), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(PAYEStatus.draft))

      when(mockRepo.updateRegistrationStatus(ArgumentMatchers.eq("AC654321"), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new RuntimeException("")))

      def newstatus(s: String) = await(controller.newStatus("AC123456", s)(FakeRequest()))
      status(newstatus("draft")) shouldBe Status.OK
      status(newstatus("held")) shouldBe Status.OK
      status(newstatus("submitted")) shouldBe Status.OK
      status(newstatus("acknowledged")) shouldBe Status.OK
      status(newstatus("invalid")) shouldBe Status.OK
      status(newstatus("cancelled")) shouldBe Status.OK
      status(newstatus("rejected")) shouldBe Status.OK
      status(await(controller.newStatus("AC654321", "bananaFruitcake")(FakeRequest()))) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "updateStatus" should {
    "return a 200 response for success" in new Setup {

      when(mockRepo.updateRegistrationStatus(ArgumentMatchers.eq("AC123456"), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(PAYEStatus.draft))

      status(await(controller.updateStatus("AC123456", "submitted")(FakeRequest()))) shouldBe Status.OK
    }
  }

}
