import Dependencies._

ThisBuild / resolvers ++= sys.env
  .get("LIGHTBEND_COMMERCIAL_MVN")
  .map { repo =>
    Seq(
      "lightbend-commercial-mvn".at(repo),
      Resolver.url(
        "lightbend-commercial-ivy",
        url(repo)
      )(Resolver.ivyStylePatterns)
    )
  }
  .getOrElse(Seq.empty)

lazy val `akka-platform-dependencies` =
  Project(id = "akka-platform-dependencies", base = file("."))
    .enablePlugins(BillOfMaterialsPlugin)
    .settings(
      crossScalaVersions := Versions.CrossScalaVersions,
      scalaVersion := Versions.Scala213,
      organization := "com.lightbend.akka",
      sonatypeProfileName := "com.lightbend",
      name := "akka-platform-dependencies",
      description := s"${description.value} (depending on Scala ${CrossVersion.binaryScalaVersion(scalaVersion.value)})",
      homepage := Some(url("https://akka.io/")),
      licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
      developers := List(
        Developer(
          "akka-contributors",
          "Akka Contributors",
          "akka.official@gmail.com",
          url("https://github.com/akka/akka/graphs/contributors")
        )
      ),
      bomIncludeModules := akka ++
        akkaHttp ++
        akkaManagement ++
        akkaProjections ++
        akkaGrpc ++
        akkaPersistencePlugins ++
        akkaResilienceEnhancements ++
        alpakka ++
        telemetry,
      // to check that all dependencies can be pulled and there are no conflicts
      libraryDependencies ++= {
        val bomDeps = bomIncludeModules.value
        if (sys.env.contains("LIGHTBEND_COMMERCIAL_MVN")) {
          bomDeps
        } else {
          // Run the validation for at least the non-commercial dependencies
          bomDeps.filterNot(allCommercialLibs.contains)
        }
      },
      publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
      publishM2Configuration := publishM2Configuration.value.withOverwrite(true)
    )

addCommandAlias("checkBom", ";scalafmtSbtCheck;+akka-platform-dependencies/billOfMaterials:publishM2")
addCommandAlias("checkPullBom", ";scalafmtSbtCheck;+update;+akka-platform-dependencies/billOfMaterials:publishM2")
