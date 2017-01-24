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

package controllers

import connectors.AuthConnector
import models.{CompanyDetails, Employment}
import play.api.mvc._
import services._
import uk.gov.hmrc.play.microservice.controller.BaseController
import auth._
import common.exceptions.DBExceptions.MissingRegDocument
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object RegistrationController extends RegistrationController {
  //$COVERAGE-OFF$
  override val auth = AuthConnector
  override val registrationService = RegistrationService
  val resourceConn = RegistrationService.registrationRepository
  //$COVERAGE-ON$
}

trait RegistrationController extends BaseController with Authenticated with Authorisation[String] {

  val registrationService: RegistrationService

  def newPAYERegistration(regID: String) : Action[AnyContent] = Action.async {
    implicit request =>
      authenticated {
        case NotLoggedIn => Future.successful(Forbidden)
        case LoggedIn(context) =>
          registrationService.createNewPAYERegistration(regID, context.ids.internalId) map {
            reg => Ok(Json.toJson(reg))
          }
      }
  }

  def getPAYERegistration(regID: String) : Action[AnyContent] = Action.async {
    implicit request =>
      authorised(regID) {
        case Authorised(context) =>
          registrationService.fetchPAYERegistration(regID) map {
            case Some(registration) => Ok(Json.toJson(registration))
            case None => NotFound
          }
        case NotLoggedInOrAuthorised =>
          Logger.info(s"[RegistrationController] [getPAYERegistration] User not logged in")
          Future.successful(Forbidden)
        case NotAuthorised(_) =>
          Logger.info(s"[RegistrationController] [getPAYERegistration] User logged in but not authorised for resource $regID")
          Future.successful(Forbidden)
        case AuthResourceNotFound(_) => Future.successful(NotFound)
      }
  }

  def getCompanyDetails(regID: String) : Action[AnyContent] = Action.async {
    implicit request =>
      authorised(regID) {
        case Authorised(_) =>
          registrationService.getCompanyDetails(regID) map {
            case Some(companyDetails) => Ok(Json.toJson(companyDetails))
            case None => NotFound
          }
        case NotLoggedInOrAuthorised =>
          Logger.info(s"[RegistrationController] [getCompanyDetails] User not logged in")
          Future.successful(Forbidden)
        case NotAuthorised(_) =>
          Logger.info(s"[RegistrationController] [getCompanyDetails] User logged in but not authorised for resource $regID")
          Future.successful(Forbidden)
        case AuthResourceNotFound(_) => Future.successful(NotFound)
      }
  }

  def upsertCompanyDetails(regID: String) : Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      authorised(regID) {
        case Authorised(_) =>
          withJsonBody[CompanyDetails] { companyDetails =>
            registrationService.upsertCompanyDetails(regID, companyDetails) map { companyDetailsResponse =>
              Ok(Json.toJson(companyDetailsResponse))
            } recover {
              case missing : MissingRegDocument => NotFound
            }
          }
        case NotLoggedInOrAuthorised =>
          Logger.info(s"[RegistrationController] [upsertCompanyDetails] User not logged in")
          Future.successful(Forbidden)
        case NotAuthorised(_) =>
          Logger.info(s"[RegistrationController] [upsertCompanyDetails] User logged in but not authorised for resource $regID")
          Future.successful(Forbidden)
        case AuthResourceNotFound(_) => Future.successful(NotFound)
      }
  }

  def getEmployment(regID: String) : Action[AnyContent] = Action.async {
    implicit request =>
      authorised(regID) {
        case Authorised(_) =>
          registrationService.getEmployment(regID) map {
            case Some(employment) => Ok(Json.toJson(employment))
            case None => NotFound
          }
        case NotLoggedInOrAuthorised =>
          Logger.info(s"[RegistrationController] [getEmployment] User not logged in")
          Future.successful(Forbidden)
        case NotAuthorised(_) =>
          Logger.info(s"[RegistrationController] [getEmployment] User logged in but not authorised for resource $regID")
          Future.successful(Forbidden)
        case AuthResourceNotFound(_) => Future.successful(NotFound)
      }
  }

  def upsertEmployment(regID: String) : Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      authorised(regID) {
        case Authorised(_) =>
          withJsonBody[Employment] { employmentDetails =>
            registrationService.upsertEmployment(regID, employmentDetails) map { employmentResponse =>
              Ok(Json.toJson(employmentResponse))
            } recover {
              case missing : MissingRegDocument => NotFound
            }
          }
        case NotLoggedInOrAuthorised =>
          Logger.info(s"[RegistrationController] [upsertEmployment] User not logged in")
          Future.successful(Forbidden)
        case NotAuthorised(_) =>
          Logger.info(s"[RegistrationController] [upsertEmployment] User logged in but not authorised for resource $regID")
          Future.successful(Forbidden)
        case AuthResourceNotFound(_) => Future.successful(NotFound)
      }
  }
}
