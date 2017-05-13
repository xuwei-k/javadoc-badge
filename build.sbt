organization := "com.github.xuwei-k"

name := "javadoc-badge"

licenses += ("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))

scalaVersion := "2.11.11"

// https://github.com/unfiltered/unfiltered/blob/v0.8.1/project/common.scala#L6
// https://github.com/unfiltered/unfiltered/blob/v0.8.2/project/common.scala#L6
// https://code.google.com/p/googleappengine/issues/detail?id=3091
libraryDependencies ++= (
  ("com.github.xuwei-k" %% "httpz-native" % "0.5.1") ::
  ("io.argonaut" %% "argonaut-scalaz" % "6.2") ::
  ("org.scalaz" %% "scalaz-concurrent" % "7.2.12") ::
  ("net.databinder" %% "unfiltered-filter" % "0.8.1") ::
  ("javax.servlet" % "servlet-api" % "2.3" % "provided") ::
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
