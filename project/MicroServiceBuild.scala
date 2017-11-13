import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object MicroServiceBuild extends Build with MicroService {

  val appName = "paye-registration"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val playAuthVersion = "4.3.0"
  private val playHealthVersion = "2.1.0"
  private val playJsonLoggerVersion = "3.0.0"
  private val logbackJsonLoggerVersion = "3.1.0"
  private val playConfigVersion = "4.3.0"
  private val domainVersion = "4.1.0"
  private val hmrcTestVersion = "2.3.0"
  private val scalaTestVersion_test = "3.0.1"
  private val scalaTestVersion_it = "3.0.1"
  private val scalaTestPlusVersion = "2.0.0"
  private val pegdownVersion = "1.6.0"
  private val cryptoVersion = "4.4.0"

  private val playReactivemongoVersion = "5.2.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "play-reactivemongo" % playReactivemongoVersion,
    "uk.gov.hmrc" %% "microservice-bootstrap" % "6.9.0",
    "uk.gov.hmrc" %% "play-url-binders" % "2.1.0",
    "uk.gov.hmrc" %% "domain" % "5.0.0",
    "uk.gov.hmrc" %% "mongo-lock" % "5.0.0",
    "uk.gov.hmrc" %% "play-scheduling" % "4.1.0",
    "uk.gov.hmrc" %% "crypto" % cryptoVersion,
    "com.typesafe.play" %% "play-json-joda" % "2.6.7"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "3.0.4" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "reactivemongo-test" % "2.0.0" % scope,
        "org.mockito" % "mockito-core" % "2.11.0"
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "2.9.0" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "reactivemongo-test" % "2.0.0" % scope,
        "com.github.tomakehurst" % "wiremock" % "2.9.0" % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
