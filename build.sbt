name := "6110-sbt"

version := "0.13.8"

scalaVersion := "2.11.7"

//scaladoc
scalacOptions in (Compile,doc) ++= Seq("-groups", "-implicits")


resolvers += "bintray-drdozer" at "http://dl.bintray.com/content/drdozer/maven"

// Dependencies
// libraryDependencies += "net.liftweb" % "lift-json_2.11" % "3.0-M6"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.7"

// graph for scala
libraryDependencies += "com.assembla.scala-incubator" % "graph-core_2.11" % "1.9.4"

//json (net.liftweb.json)
libraryDependencies += "com.assembla.scala-incubator" % "graph-json_2.11" % "1.9.2"

// dot (graph4s to dot)
libraryDependencies += "com.assembla.scala-incubator" % "graph-dot_2.11" % "1.10.0"

libraryDependencies += "org.ow2.asm" % "asm-all" % "5.0.4"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.5"

libraryDependencies += "org.scalacheck" % "scalacheck_2.11" % "1.12.4"

// Bokeh (charting from scala, binding from python's bokeh library)
libraryDependencies += "io.continuum.bokeh" %% "bokeh" % "0.6"

