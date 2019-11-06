organization := "com.github.xuwei-k"

name := "javadoc-badge"

licenses += ("MIT License" -> url("https://www.opensource.org/licenses/mit-license.php"))

scalaVersion := "2.13.1"

libraryDependencies ++= (
  ("com.github.xuwei-k" %% "httpz-native" % "0.6.1") ::
  ("io.argonaut" %% "argonaut-scalaz" % "6.2.3") ::
  ("org.scalaz" %% "scalaz-concurrent" % "7.2.29") ::
  ("ws.unfiltered" %% "unfiltered-filter" % "0.10.0-M5") ::
  ("javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided") ::
  Nil
)

val unusedWarnings = "-Ywarn-unused" :: Nil

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
