name := "WebSignature"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  javaJdbc,
  javaEbean,
  "commons-lang" % "commons-lang" % "2.6"
  "postgresql" % "postgresql" % "8.4-702.jdbc4"
)     

play.Project.playScalaSettings
