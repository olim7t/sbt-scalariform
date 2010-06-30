/* sbt Scalariform plugin
 * Copyright 2010 Olivier Michallat
 */
package com.github.olim7t.sbtscalariform

import sbt._
import scala.io.Source
import sbt.Fork.ForkScala
import java.io.File

trait ScalariformPlugin extends BasicScalaProject with SourceTasks {
	import ScalariformPlugin._

	val sfConfig = config("sfConfig") hide
	val scalariform = "com.github.mdr" % "scalariform.core" % "0.0.4" % "sfConfig" from "http://scalariform.googlecode.com/svn/trunk/update-site/plugins/scalariform.core_0.0.4.201006281921.jar"
	def scalariformClasspath = descendents(configurationPath(sfConfig), "*.jar").absString

	def formatterScalaJars = {
		val si = getScalaInstance(ScalariformScalaVersion)
		si.libraryJar :: si.compilerJar :: Nil
	}

	lazy val formatSources = formatSourcesAction
	lazy val formatTests = formatTestsAction

	def sourceTimestamp = "sources.lastFormatted"
	def testTimestamp = "tests.lastFormatted"

	def formatSourcesAction = forAllSourcesTask(sourceTimestamp from mainSources)(runFormatter) describedAs("Format main Scala sources")
	def formatTestsAction = forAllSourcesTask(testTimestamp from testSources)(runFormatter) describedAs("Format test Scala sources")

	override def compileAction = super.compileAction dependsOn(formatSources)
	override def testCompileAction = super.testCompileAction dependsOn(formatTests)

	private def runFormatter(sources: Iterable[Path]): Option[String] = {
		val forkFormatter = new ForkScala(ScalariformMainClass)
		for (source <- sources) {
			log.debug("Formatting " + source)
			forkFormatter(None, Seq("-cp", scalariformClasspath) , formatterScalaJars, Seq("-i", source.absolutePath), log) 
		}
		None
	}
}
object ScalariformPlugin {
	/** The version of Scala used to run Scalariform.*/
	val ScalariformScalaVersion = "2.8.0.RC6"

	val ScalariformMainClass = "scalariform.commandline.Main"
}
