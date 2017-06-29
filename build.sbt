organization := "com.github.xuwei-k"

name := "javadoc-badge"

licenses += ("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))

scalaVersion := "2.12.2"

libraryDependencies ++= (
  ("com.github.xuwei-k" %% "httpz-native" % "0.5.1") ::
  ("io.argonaut" %% "argonaut-scalaz" % "6.2") ::
  ("org.scalaz" %% "scalaz-concurrent" % "7.2.14") ::
  ("ws.unfiltered" %% "unfiltered-filter" % "0.9.1") ::
  ("javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided") ::
  ("joda-time" % "joda-time" % "2.9.9") ::
  ("org.joda" % "joda-convert" % "1.2") ::
  Nil
)

val unusedWarnings = (
  "-Ywarn-unused" ::
  "-Ywarn-unused-import" ::
  Nil
)

scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  Nil
) ::: unusedWarnings

Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) ~= {_.filterNot(unusedWarnings.toSet)}
)

fullResolvers ~= {_.filterNot(_.name == "jcenter")}
