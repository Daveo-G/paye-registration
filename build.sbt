import TestPhases.oneForkedJvmPerTest
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import scoverage.ScoverageKeys

val appName = "paye-registration"

lazy val scoverageSettings = Seq(
    ScoverageKeys.coverageExcludedPackages  := "<empty>;Reverse.*;model.*;config.*;.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimum           := 80,
    ScoverageKeys.coverageFailOnMinimum     := false,
    ScoverageKeys.coverageHighlighting      := true
  )


lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala,SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin): _*)
  .settings(scalaSettings: _*)
  .settings(scoverageSettings : _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    libraryDependencies                           ++= AppDependencies(),
    retrieveManaged                               := true,
    evictionWarningOptions in update              := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator                               := InjectedRoutesGenerator,
    scalaVersion                                  := "2.11.11",
    Keys.fork in IntegrationTest                  := false,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    testGrouping in IntegrationTest               := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest          := false,
    resolvers                                     += Resolver.bintrayRepo("hmrc", "releases"),
    resolvers                                     += Resolver.jcenterRepo,
    addTestReportOption(IntegrationTest, "int-test-reports")
  )
