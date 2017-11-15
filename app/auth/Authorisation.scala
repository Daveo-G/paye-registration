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

package auth

import play.api.mvc.Result
import utils.CustomLogger._
import connectors.{AuthConnect, Authority}

import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

sealed trait AuthorisationResult {}
case object NotLoggedInOrAuthorised extends AuthorisationResult
final case class NotAuthorised(authContext: Authority) extends AuthorisationResult
final case class Authorised(authContext: Authority) extends AuthorisationResult
final case class AuthResourceNotFound(authContext: Authority) extends AuthorisationResult

trait Authorisation[I] {

  val auth: AuthConnect
  val resourceConn : AuthorisationResource[I]

  implicit val self = getClass

  def authorised(id:I)(f: => AuthorisationResult => Future[Result])(implicit hc: HeaderCarrier) = {
    for {
      authority <- auth.getCurrentAuthority()
      resource <- resourceConn.getInternalId(id)
      result <- f(mapToAuthResult(authority, resource))
    } yield {
      result
    }
  }

  private def mapToAuthResult(authContext: Option[Authority], resource: Option[(I,String)] ) : AuthorisationResult = {
    authContext match {
      case None =>
        logger.warn("[Authorisation] - [mapToAuthResult]: No authority was found")
        NotLoggedInOrAuthorised
      case Some(context) => {
        resource match {
          case None =>
            logger.info("[Authorisation] - [mapToAuthResult]: No auth resource was found for the current user")
            AuthResourceNotFound(context)
          case Some((_, context.ids.internalId)) => Authorised(context)
          case Some((_, _)) =>
            logger.warn("[Authorisation] - [mapToAuthResult]: The current user is not authorised to access this resource")
            NotAuthorised (context)
        }
      }
    }
  }
}
