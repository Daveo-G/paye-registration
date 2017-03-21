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

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, OK}
import helpers.PAYERegSpec
import utils.{BooleanFeatureSwitch, FeatureManager, PAYEFeatureSwitches}

import scala.concurrent.Future

class FeatureSwitchControllerSpec extends PAYERegSpec {

  val mockPAYEFeatureSwitch = mock[PAYEFeatureSwitches]
  val mockFeatureManager = mock[FeatureManager]

  val testFeatureSwitch = BooleanFeatureSwitch(name = "desStubFeature", enabled = true)
  val testDisabledSwitch = BooleanFeatureSwitch(name = "desStubFeature", enabled = false)

  class Setup {
    val controller = new FeatureSwitchCtrl {
      override val PayeFeatureSwitch = mockPAYEFeatureSwitch
      override val featureManager = mockFeatureManager
    }
  }

  "switch" should {
    "enable the desStubFeature and return an OK" when {
      "desStubFeature and true are passed in the url" in new Setup {
        when(mockPAYEFeatureSwitch(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testFeatureSwitch)))

        when(mockFeatureManager.enable(ArgumentMatchers.any()))
          .thenReturn(testFeatureSwitch)

        val result = controller.switch("desStubFeature","true")(FakeRequest())
        status(result) shouldBe OK
      }
    }

    "disable the desStubFeatureFeature and return an OK" when {
      "desStubFeature and some other featureState is passed into the URL" in new Setup {
        when(mockPAYEFeatureSwitch(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testFeatureSwitch)))

        when(mockFeatureManager.disable(ArgumentMatchers.any()))
          .thenReturn(testDisabledSwitch)

        val result = await(controller.switch("desStubFeature","someOtherState")(FakeRequest()))
        status(result) shouldBe OK
      }
    }

    "return a bad request" when {
      "an unknown feature is trying to be enabled" in new Setup {
        when(mockPAYEFeatureSwitch(ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        val result = controller.switch("invalidName","invalidState")(FakeRequest())
        status(result) shouldBe BAD_REQUEST
      }
    }
  }
}