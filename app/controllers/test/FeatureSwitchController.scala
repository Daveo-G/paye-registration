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

package controllers.test

import javax.inject.{Inject, Singleton}

import play.api.mvc.Action
import uk.gov.hmrc.play.microservice.controller.BaseController
import utils._

import scala.concurrent.Future

@Singleton
class FeatureSwitchController extends FeatureSwitchCtrl



trait FeatureSwitchCtrl extends BaseController {

  val fs =  FeatureSwitch

  def switch(featureName: String, featureState: String) = Action.async {
    implicit request =>

      def feature: FeatureSwitch = featureState match {
        case "true"                                         => fs.enable(BooleanFeatureSwitch(featureName, enabled = true))
        case x if x.matches(FeatureSwitch.datePatternRegex) => fs.setSystemDate(ValueSetFeatureSwitch(featureName, featureState))
        case x@"time-clear"                                 => fs.clearSystemDate(ValueSetFeatureSwitch(featureName, x))
        case _                                              => fs.disable(BooleanFeatureSwitch(featureName, enabled = false))
      }

      PAYEFeatureSwitches(featureName) match {
        case Some(_) => Future.successful(Ok(feature.toString))
        case None => Future.successful(BadRequest)
      }
  }
}
