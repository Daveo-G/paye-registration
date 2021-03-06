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

package controllers

import javax.inject.{Inject, Singleton}

import auth._
import common.exceptions.RegistrationExceptions.UnmatchedStatusException
import config.AuthClientConnector
import enums.PAYEStatus
import play.api.mvc.{Action, AnyContent}
import repositories.RegistrationMongoRepository
import services.RegistrationService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController

@Singleton
class RepositoryController @Inject()(injRegistrationService: RegistrationService) extends RepositoryCtrl {
  override lazy val authConnector: AuthConnector = AuthClientConnector

  val resourceConn: RegistrationMongoRepository = injRegistrationService.registrationRepository
  val registraitonService: RegistrationService = injRegistrationService
}

trait RepositoryCtrl extends BaseController with Authorisation {

  val registraitonService: RegistrationService

  def deleteRegistrationFromDashboard(regId: String) : Action[AnyContent] = Action.async {
    implicit request =>
      isAuthorised(regId) { authResult =>
        authResult.ifAuthorised(regId, "RepositoryCtrl", "deleteRegistrationFromDashboard") {
          registraitonService.deletePAYERegistration(regId, PAYEStatus.draft, PAYEStatus.invalid) map { deleted =>
            if(deleted) Ok else InternalServerError
          } recover {
            case _: UnmatchedStatusException => PreconditionFailed
          }
        }
      }
  }

}
