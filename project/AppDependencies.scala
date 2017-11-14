
import sbt._

object AppDependencies {
  def apply() = MainDependencies() ++ UnitTestDependencies() ++ IntegrationTestDependecies()
}

private object MainDependencies {

  private val playJsonLoggerVersion         = "3.0.0"
  private val playConfigVersion             = "4.3.0"
  private val domainVersion                 = "5.0.0"
  private val playSchedulingVersion         = "4.1.0"
  private val cryptoVersion                 = "4.5.0"
  private val playUrlBindersVersion         = "2.1.0"
  private val mongoLockVersion              = "5.0.0"
  private val microserviceBootstrapVersion  = "6.13.0"
  private val playjsonJodaVersion           = "2.6.7"
  private val playReactivemongoVersion      = "5.2.0"

  def apply() = Seq(
    "uk.gov.hmrc"       %% "play-reactivemongo"     % playReactivemongoVersion,
    "uk.gov.hmrc"       %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc"       %% "play-url-binders"       % playUrlBindersVersion,
    "uk.gov.hmrc"       %% "domain"                 % domainVersion,
    "uk.gov.hmrc"       %% "mongo-lock"             % mongoLockVersion,
    "uk.gov.hmrc"       %% "play-scheduling"        % playSchedulingVersion,
    "uk.gov.hmrc"       %% "crypto"                 % cryptoVersion,
    "com.typesafe.play" %% "play-json-joda"         % playjsonJodaVersion
  )
}

trait TestDependencies {
  val hmrcTestVersion           = "2.3.0"
  val scalaTestVersion          = "3.0.1"
  val scalaTestPlusVersion      = "3.1.2"
  val mockitoCoreVersion        = "2.11.0"
  val wiremockVersion           = "2.9.0"
  val reactiveMongoTestVersion  = "2.0.0"

  val scope: Configuration
  val test : Seq[ModuleID]

  lazy val commonTestDependencies = Seq(
    "uk.gov.hmrc"             %%  "hmrctest"            % hmrcTestVersion           % scope,
    "org.scalatest"           %%  "scalatest"           % scalaTestVersion          % scope,
    "org.scalatestplus.play"  %%  "scalatestplus-play"  % scalaTestPlusVersion      % scope,
    "uk.gov.hmrc"             %%  "reactivemongo-test"  % reactiveMongoTestVersion  % scope
  )
}

object UnitTestDependencies extends TestDependencies {
  val scope = Test
  val test = commonTestDependencies ++ Seq(
    "org.mockito"           %   "mockito-core"      % mockitoCoreVersion
  )
  def apply() = test
}


object IntegrationTestDependecies extends TestDependencies {
  val scope = IntegrationTest
  val test = commonTestDependencies ++ Seq(
    "com.github.tomakehurst"  %   "wiremock"        % "2.9.0"              % scope
  )
  def apply() = test
}

