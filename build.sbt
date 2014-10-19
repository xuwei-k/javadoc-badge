organization := "com.github.xuwei-k"

name := "javadoc-badge"

licenses += ("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))

scalaVersion := "2.11.2"

libraryDependencies ++= (
  ("net.databinder" %% "unfiltered-filter" % "0.8.2") ::
  ("javax.servlet" % "servlet-api" % "2.3" % "provided") ::
  Nil
)

scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  "-Ywarn-unused" ::
  "-Ywarn-unused-import" ::
  Nil
)
