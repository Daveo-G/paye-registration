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

package utils

import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.play.test.UnitSpec

class FeatureSwitchSpec extends UnitSpec with BeforeAndAfterEach {

  override def beforeEach() {
    System.clearProperty("feature.test")
  }

  val payeFeatureSwitch = PAYEFeatureSwitches
  val booleanFeatureSwitch = BooleanFeatureSwitch("test", false)

  "getProperty" should {

    "return a disabled feature switch if the system property is undefined" in {
      FeatureSwitch.getProperty("test") shouldBe BooleanFeatureSwitch("test", enabled = false)
    }

    "return an enabled feature switch if the system property is defined as 'true'" in {
      System.setProperty("feature.test", "true")

      FeatureSwitch.getProperty("test") shouldBe BooleanFeatureSwitch("test", enabled = true)
    }

    "return an enabled feature switch if the system property is defined as 'false'" in {
      System.setProperty("feature.test", "false")

      FeatureSwitch.getProperty("test") shouldBe BooleanFeatureSwitch("test", enabled = false)
    }
  }

  "systemPropertyName" should {

    "append feature. to the supplied string'" in {
      FeatureSwitch.systemPropertyName("test") shouldBe "feature.test"
    }
  }

  "setProperty" should {

    "return a feature switch (testKey, false) when supplied with (testKey, testValue)" in {
      FeatureSwitch.setProperty("test", "testValue") shouldBe BooleanFeatureSwitch("test", enabled = false)
    }

    "return a feature switch (testKey, true) when supplied with (testKey, true)" in {
      FeatureSwitch.setProperty("test", "true") shouldBe BooleanFeatureSwitch("test", enabled = true)
    }
  }

  "enable" should {
    "set the value for the supplied key to 'true'" in {
      System.setProperty("feature.test", "false")

      FeatureSwitch.enable(booleanFeatureSwitch) shouldBe BooleanFeatureSwitch("test", enabled = true)
    }
  }

  "disable" should {
    "set the value for the supplied key to 'false'" in {
      System.setProperty("feature.test", "true")

      FeatureSwitch.disable(booleanFeatureSwitch) shouldBe BooleanFeatureSwitch("test", enabled = false)
    }
  }

  "dynamic toggling should be supported" in {
    FeatureSwitch.disable(booleanFeatureSwitch).enabled shouldBe false
    FeatureSwitch.enable(booleanFeatureSwitch).enabled shouldBe true
  }

  "SCRSFeatureSwitches" should {
    "return a disabled feature when the associated system property doesn't exist" in {
      payeFeatureSwitch.desService.enabled shouldBe false
    }

    "return an enabled feature when the associated system property is true" in {
      FeatureSwitch.enable(payeFeatureSwitch.desService)

      payeFeatureSwitch.desService.enabled shouldBe true
    }

    "return a disable feature when the associated system property is false" in {
      FeatureSwitch.disable(payeFeatureSwitch.desService)

      payeFeatureSwitch.desService.enabled shouldBe false
    }

    "return true if the desServiceFeature system property is true" in {
      System.setProperty("feature.desServiceFeature", "true")

      payeFeatureSwitch("desServiceFeature") shouldBe Some(BooleanFeatureSwitch("desServiceFeature", true))
    }

    "return false if the desServiceFeature system property is false" in {
      System.setProperty("feature.desServiceFeature", "false")

      payeFeatureSwitch("desServiceFeature") shouldBe Some(BooleanFeatureSwitch("desServiceFeature", false))
    }

    "return an empty option if a system property doesn't exist when using the apply function" in {
      payeFeatureSwitch("somethingElse") shouldBe None
    }
  }

}
