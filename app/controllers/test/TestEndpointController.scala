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

import auth.{Authenticated, LoggedIn, NotLoggedIn}
import com.google.inject.{Inject, Singleton}
import connectors.AuthConnector
import models.PAYERegistration
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent}
import repositories.{RegistrationMongo, RegistrationMongoRepository}
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TestEndpointController @Inject()(authConnector: AuthConnector, registrationMongo : RegistrationMongo) extends TestEndpointCtrl {
  val auth: AuthConnector = authConnector
  val registrationRepository: RegistrationMongoRepository = registrationMongo.store
}

trait TestEndpointCtrl extends BaseController with Authenticated {

  val registrationRepository: RegistrationMongoRepository

  def registrationTeardown: Action[AnyContent] = Action.async {
    implicit request =>
      registrationRepository.dropCollection map {
        _ => Ok
      } recover {
        case e => InternalServerError(e.getMessage)
      }
  }

  def deleteRegistration(regID: String): Action[AnyContent] = Action.async {
    implicit request =>
      registrationRepository.deleteRegistration(regID) map {
        _ => Ok
      } recover {
        case e => InternalServerError(e.getMessage)
      }
  }

  def updateRegistration(regID: String): Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      authenticated {
        case NotLoggedIn => Future.successful(Forbidden)
        case LoggedIn(context) =>
          withJsonBody[JsObject] {
            reg =>
              reg. +("internalID" -> JsString(context.ids.internalId)).validate[PAYERegistration].fold (
                  errs => Future.successful(BadRequest(errs.toString())),
                  registration => registrationRepository.updateRegistration(registration) map {
                    _ => Ok(Json.toJson(reg).as[JsObject])
                  } recover {
                    case e => InternalServerError(e.getMessage)
                  }
                )
              }
          }
  }

}
