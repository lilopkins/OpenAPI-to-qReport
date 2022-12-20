val scala3Version = "3.2.1"

lazy val root = project
  .in(file("."))
  .enablePlugins(PackPlugin)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "OpenAPI to qReport",
    version := "0.1.0",

    scalaVersion := scala3Version,
    buildInfoKeys := Seq(name, version),
    buildInfoPackage := "uk.hpkns.openapitoqreport",

    packMain := Map("openapi-to-qreport" -> "uk.hpkns.openapitoqreport.Main"),

    libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0",
    libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.5",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.5",
    libraryDependencies += "com.reprezen.kaizen" % "openapi-parser" % "4.0.4",
    libraryDependencies += "com.github.javafaker" % "javafaker" % "1.0.2",
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )
