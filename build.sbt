organization := "com.github.xuwei-k"

name := "javadoc-badge"

licenses += ("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))

scalaVersion := "2.12.4"

libraryDependencies ++= (
  ("com.github.xuwei-k" %% "httpz-native" % "0.5.1") ::
  ("io.argonaut" %% "argonaut-scalaz" % "6.2.1") ::
  ("org.scalaz" %% "scalaz-concurrent" % "7.2.18") ::
  ("ws.unfiltered" %% "unfiltered-filter" % "0.9.1") ::
  ("javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided") ::
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
