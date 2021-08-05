import com.typesafe.sbt.packager.docker.ExecCmd
import sbt.Keys.libraryDependencies

resolvers += Resolver.sonatypeRepo("snapshots")

lazy val http4sVersion = "0.21.24"
lazy val http4sRhoVersion = "0.21.0"
lazy val catsEffectVersion = "2.5.1"
lazy val logBackVersion = "1.2.3"
lazy val scalaLoggingVersion = "3.9.3"
lazy val log4catsVersion = "1.3.1"
lazy val circeVersion = "0.14.1"
lazy val fs2KafkaVersion = "1.7.0"
lazy val fs2Version = "2.5.8"
lazy val pureConfigVersion = "0.14.1"
lazy val bot4sVersion = "5.0.1"
lazy val specs2Version = "4.11.0"
lazy val scalacheckVersion = "1.14.1"
lazy val catsScalacheckVersion = "0.2.0"
lazy val kindProjectorVersion = "0.13.0"
lazy val catsEffectsVersion = "2.5.1"
lazy val sttpVersion = "3.3.9"
lazy val fs2Cron4sVersion = "0.5.0"
lazy val mouseVersion = "1.0.4"


lazy val projectName = "sv-alert"

lazy val scalaLangVersion = "2.13.6"

lazy val commonSettings = List(
//  fork := true,
  organization := "ch.mfactory",
  scalaVersion := scalaLangVersion,
//  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions ++= Seq(
    "-encoding",
    "utf8",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Yrangepos",
    "-Xfatal-warnings"
  ),
  scalacOptions --= Seq(
    "-Xlint:nullary-override",
//    "-Xfatal-warnings"
  ),
  Test / parallelExecution  := false,
  libraryDependencies ++= Seq(

  ),
  wartremoverErrors ++= Warts.allBut(Wart.Any, Wart.Nothing),
  wartremoverWarnings ++= Warts.allBut(Wart.Any, Wart.Nothing),

  addCompilerPlugin("org.typelevel" % "kind-projector" % kindProjectorVersion cross CrossVersion.full)
)

lazy val commonMainProjectSettings = List(
  dockerBaseImage := "openjdk:11",
  dockerRepository := Some("ghcr.io"),
  dockerUsername := Some("alexanderray"),
  buildInfoPackage := "mfactory.ch.buildinfo",
  dockerUpdateLatest := true
)

lazy val `sv-alert` = (project in file("."))
  .aggregate(kafkaProxyEndpoint, telegramBot, rpcEndpoint)
//  .dependsOn(endpoint, telegramBot)

lazy val shared = (project in file("shared"))
  .settings(
    commonSettings,
    name := "shared",
    libraryDependencies ++=
      circeDependencies ++ cats ++ pureConfigDependencies ++
        testDependencies ++ fs2Dependencies
  )

lazy val cats = Seq(
  "org.typelevel" %% "cats-effect" % catsEffectsVersion,
  "org.typelevel" %% "mouse" % mouseVersion
)

lazy val circeDependencies = Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-literal" % circeVersion
)

lazy val loggingDependencies = Seq(
  "ch.qos.logback" % "logback-classic" % logBackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
)

lazy val pureConfigDependencies = Seq(
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
  "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion
)

lazy val fs2Dependencies = Seq(
  "com.github.fd4s" %% "fs2-kafka" % fs2KafkaVersion,
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version,
  "eu.timepit" %% "fs2-cron-cron4s" % fs2Cron4sVersion
)

lazy val testScope = "test"

lazy val testDependencies = Seq(
  "org.specs2" %% "specs2-scalacheck" % specs2Version % testScope,
  "org.specs2" %% "specs2-cats" % specs2Version % testScope,
//  "org.scalacheck" %% "scalacheck" % scalacheckVersion,
//  "io.chrisdavenport" %% "cats-scalacheck" % catsScalacheckVersion % testScope,
  "org.specs2" %% "specs2-junit" % specs2Version % testScope
)

lazy val blaze = Seq(
  "org.http4s" %% "rho-swagger" % http4sRhoVersion,
  "org.http4s" %% "rho-swagger-ui" % http4sRhoVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion
)

lazy val sttp = Seq(
  "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
//  "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats-ce2" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "http4s-ce2-backend" % sttpVersion,
)

lazy val kafkaProxyEndpoint = (project in file("kafka-proxy-endpoint"))
  .enablePlugins(JavaAppPackaging, DockerPlugin, BuildInfoPlugin)
  .settings(
    commonSettings,
    commonMainProjectSettings,
    name := projectName + "-kafka-proxy-endpoint",
    libraryDependencies ++= blaze ++ circeDependencies ++
      loggingDependencies ++ fs2Dependencies ++ testDependencies,
    dockerExposedPorts ++= Seq(3000),
    dockerEnvVars ++= Map(
      "BOOTSTRAP_SERVERS" -> "",
      "SASL_JAAS_CONFIG" -> "",
      "ENVIRONMENT" -> "",
      "HTTP_PASSWORD" -> "",
      "HTTP_USER" -> ""
    ),
  )
  .dependsOn(shared)

lazy val rpcEndpoint = (project in file("rpc-endpoint"))
  .enablePlugins(JavaAppPackaging, DockerPlugin, BuildInfoPlugin)
  .settings(
    commonSettings,
    commonMainProjectSettings,
    name := projectName + "-rpc-endpoint",
    libraryDependencies ++= blaze ++ circeDependencies ++
      loggingDependencies ++ testDependencies,
    dockerExposedPorts ++= Seq(3000),
    dockerCommands ++= Seq(
      ExecCmd("RUN",
        "curl", "-sSfL", "https://release.solana.com/v1.6.16/install", "--output", "/tmp/install.sh"
      ),
      ExecCmd("RUN",
        "sh", "/tmp/install.sh"
      )
    )
  )
  .dependsOn(shared)

lazy val telegramBot = (project in file("telegram-bot"))
  .enablePlugins(JavaAppPackaging, DockerPlugin, BuildInfoPlugin)
  .settings(
    commonSettings,
    commonMainProjectSettings,
    name := projectName + "-telegram-bot",
    libraryDependencies ++= Seq(
      "com.bot4s" %% "telegram-core" % bot4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    ) ++ circeDependencies ++ loggingDependencies
      ++ fs2Dependencies ++ testDependencies
      ++ sttp ++ cats,
    dockerEnvVars ++= Map(
      "BOOTSTRAP_SERVERS" -> "",
      "SASL_JAAS_CONFIG" -> "",
      "BOT_TOKEN" -> "",
      "ENVIRONMENT" -> ""
    )
  )
  .dependsOn(shared)

