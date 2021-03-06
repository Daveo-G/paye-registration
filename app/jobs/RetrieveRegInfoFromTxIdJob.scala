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

package jobs

import java.util.Base64

import common.exceptions.DBExceptions.MissingRegDocument
import play.api.{Configuration, Logger}
import repositories.RegistrationMongoRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

class RetrieveRegInfoFromTxIdJob(registrationRepo: RegistrationMongoRepository, configuration: Configuration) {

  lazy val txIds: List[String] = Some(new String(Base64.getDecoder
    .decode(configuration.getString("txIdListToRegIdForStartupJob").getOrElse("")), "UTF-8"))
    .fold(Array.empty[String])(_.split(",").filter(_.nonEmpty)).toList

  def logRegInfoFromTxId(): Unit = {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    txIds.foreach ( txId =>
      registrationRepo.retrieveRegistrationByTransactionID(txId) map { oDoc =>
          oDoc.fold(Logger.warn(s"[RetrieveRegInfoFromTxIdJob] txId: $txId has no registration document")) { doc =>
            val (regId, status, lastUpdated, lastAction) = (doc.registrationID, doc.status, doc.lastUpdate, doc.lastAction)
            Logger.info(s"[RetrieveRegInfoFromTxIdJob] txId: $txId returned a document with regId: $regId, status: $status, lastUpdated: $lastUpdated and lastAction: $lastAction")
          }
        } recover {
          case e => Logger.warn(s"[RetrieveRegInfoFromTxIdJob] an error occurred while retrieving regId for txId: $txId", e)
        }
    )
  }
}
