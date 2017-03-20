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

import javax.inject.{Inject, Singleton}

import common.exceptions.DBExceptions.MissingRegDocument
import common.exceptions.RegistrationExceptions._
import common.exceptions.SubmissionExceptions.RegistrationAlreadySubmitted
import connectors.{DESConnect, DESConnector}
import enums.PAYEStatus
import models._
import models.submission._
import play.api.Logger
import repositories._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SubmissionService @Inject()(injSequenceMongoRepository: SequenceMongo,
                                  injRegistrationMongoRepository: RegistrationMongo,
                                  injDESConnector: DESConnector) extends SubmissionSrv {
  val sequenceRepository = injSequenceMongoRepository.store
  val registrationRepository = injRegistrationMongoRepository.store
  val desConnector = injDESConnector
}

trait SubmissionSrv {

  val sequenceRepository: SequenceRepository
  val registrationRepository: RegistrationRepository
  val desConnector: DESConnect

  def submitPartialToDES(regId: String)(implicit hc: HeaderCarrier): Future[String] = {
    for {
      ackRef        <- assertOrGenerateAcknowledgementReference(regId)
      desSubmission <- buildPartialDesSubmission(regId)
      test          <- desConnector.submitToDES(desSubmission)
      testset       <- processSuccessfulDESResponse(regId)
    } yield ackRef
  }


  private[services] def assertOrGenerateAcknowledgementReference(regId: String): Future[String]= {
    registrationRepository.retrieveAcknowledgementReference(regId) flatMap {
      case Some(ackRef) => Future.successful(ackRef)
      case None => for {
        newAckref <- generateAcknowledgementReference
        _ <- registrationRepository.saveAcknowledgementReference(regId, newAckref)
      } yield newAckref
    }
  }

  private[services] def generateAcknowledgementReference: Future[String] = {
    val sequenceID = "AcknowledgementID"
    sequenceRepository.getNext(sequenceID)
      .map(ref => f"BRPY$ref%011d")
  }

  private[services] def buildPartialDesSubmission(regId: String): Future[PartialDESSubmission] = {
    registrationRepository.retrieveRegistration(regId) map {
      case None => throw new MissingRegDocument(regId)
      case Some(payeReg) if( payeReg.status == PAYEStatus.draft ) => payeReg2PartialDESSubmission(payeReg)
      case _ => throw new RegistrationAlreadySubmitted(regId)
    }
  }

  private def processSuccessfulDESResponse(regId: String): Future[PAYEStatus.Value] = {
    for {
      status <- registrationRepository.updateRegistrationStatus(regId, PAYEStatus.held)
      _ <- registrationRepository.cleardownRegistration(regId)
    } yield status
  }

  private[services] def payeReg2PartialDESSubmission(payeReg: PAYERegistration): PartialDESSubmission = {
    val companyDetails = payeReg.companyDetails.getOrElse{throw new CompanyDetailsNotDefinedException}
    PartialDESSubmission(
      acknowledgementReference = payeReg.acknowledgementReference.getOrElse {
        Logger.warn(s"[SubmissionService] - [payeReg2PartialDESSubmission]: Unable to Partial DES Submission model for reg ID ${payeReg.registrationID}, Error: Missing Acknowledgement Reference")
        throw new AcknowledgementReferenceNotExistsException(payeReg.registrationID)
      },
      company = buildDESCompanyDetails(companyDetails),
      directors = buildDESDirectors(payeReg.directors),
      payeContact = buildDESPAYEContact(payeReg.payeContact),
      businessContact = buildDESBusinessContact(companyDetails.businessContactDetails),
      sicCodes = buildDESSicCodes(payeReg.sicCodes),
      employment = buildDESEmploymentDetails(payeReg.employment),
      completionCapacity = buildDESCompletionCapacity(payeReg.completionCapacity)
    )
  }

  private def buildDESCompanyDetails(details: CompanyDetails): DESCompanyDetails = {
    DESCompanyDetails(
      companyName = details.companyName,
      tradingName = details.tradingName,
      ppob = details.ppobAddress,
      regAddress = details.roAddress
    )
  }

  private def buildDESDirectors(directors: Seq[Director]): Seq[DESDirector] = {
    directors.map(dir => {
      DESDirector(
        forename = dir.name.forename,
        surname = dir.name.surname,
        otherForenames = dir.name.otherForenames,
        title = dir.name.title,
        nino = dir.nino
      )
    })
  }

  private[services] def buildDESPAYEContact(payeContact: Option[PAYEContact]): DESPAYEContact = {
    payeContact match {
      case Some(contact) =>
        DESPAYEContact(
          name = contact.contactDetails.name,
          email = contact.contactDetails.digitalContactDetails.email,
          tel = contact.contactDetails.digitalContactDetails.phoneNumber,
          mobile = contact.contactDetails.digitalContactDetails.mobileNumber,
          correspondenceAddress = contact.correspondenceAddress
        )
      case None => throw new PAYEContactNotDefinedException
    }
  }

  private def buildDESBusinessContact(businessContactDetails: DigitalContactDetails): DESBusinessContact = {
    DESBusinessContact(
      email = businessContactDetails.email,
      tel = businessContactDetails.phoneNumber,
      mobile = businessContactDetails.mobileNumber
    )
  }

  private def buildDESSicCodes(sicsCodeSeq: Seq[SICCode]): List[DESSICCode] = {
    sicsCodeSeq.toList map { list =>
      DESSICCode(
        code = list.code,
        description = list.description
      )
    }
  }

  private[services] def buildDESEmploymentDetails(employment: Option[Employment]): DESEmployment = {
    employment match {
      case Some(details) =>
        DESEmployment(
          employees = details.employees,
          ocpn = details.companyPension,
          cis = details.subcontractors,
          firstPaymentDate = details.firstPaymentDate
        )
      case None => throw new EmploymentDetailsNotDefinedException
    }
  }

  private[services] def buildDESCompletionCapacity(capacity: Option[String]): DESCompletionCapacity = {
    val DIRECTOR = "director"
    val AGENT = "agent"
    val OTHER = "other"
    capacity.map(_.trim.toLowerCase).map {
        case DIRECTOR => DESCompletionCapacity(DIRECTOR, None)
        case AGENT => DESCompletionCapacity(AGENT, None)
        case other => DESCompletionCapacity(OTHER, Some(other))
      }.getOrElse{
        throw new CompletionCapacityNotDefinedException
      }
  }
}