name := "slick-exercises"

version := "1.0"

ThisBuild / scalaVersion := "2.13.10"

ThisBuild / scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-Xlint", "-Xfatal-warnings")

Global / onChangedBuildSource := ReloadOnSourceChanges

val commonDependencies = Seq(
  "com.typesafe.slick" %% "slick"           % "3.4.1",
  "ch.qos.logback"      % "logback-classic" % "1.4.5",
  "com.beachape"       %% "enumeratum"      % "1.7.2",
)

val h2Dependencies = Seq(
  "com.h2database" % "h2" % "2.1.214",
)

val mysqlDependencies = Seq(
  "mysql" % "mysql-connector-java" % "8.0.13",
)

val akkaStreamsDependencies = Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "5.0.0",
  "com.typesafe.akka"  %% "akka-stream"               % "2.7.0"
)

lazy val example = project
  .in(file("example"))
  .settings(
    libraryDependencies ++= commonDependencies ++ h2Dependencies
  )

lazy val exercise = project
  .in(file("exercise"))
  .settings(
    libraryDependencies ++= commonDependencies ++ mysqlDependencies ++ akkaStreamsDependencies
  )
