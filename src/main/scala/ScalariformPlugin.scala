/* sbt Scalariform plugin
 * Copyright 2010 Olivier Michallat
 */
package com.github.olim7t.sbtscalariform

import sbt._
import sbt.Fork.ForkScala
import scala.io.Source
import java.io.File
import FileUtilities.{ Newline, write }
import Actions._

trait ScalariformPlugin extends BasicScalaProject with SourceTasks {

  // Use a custom private configuration to retrieve the binaries without
  // leaking the dependency to the client project.
  private val sfConfig = config("sfConfig") hide
  private val sfDep = "com.github.mdr" % "scalariform.core" % ScalariformPlugin.Version % "sfConfig" from ScalariformPlugin.CoreUrl
  private def sfClasspath: Option[String] = {
    val jarFinder = descendents(configurationPath(sfConfig), "*.jar")
    if (jarFinder.get.isEmpty) None else Some(jarFinder.absString)
  }

  private def sfScalaJars = {
    val si = getScalaInstance(ScalariformPlugin.ScalaVersion)
    si.libraryJar :: si.compilerJar :: Nil
  }

  def scalariformOptions = Seq[ScalariformOption]()

  lazy val formatSources = formatSourcesAction
  lazy val testFormatSources = testFormatSourcesAction

  def sourcesTimestamp = "sources.lastFormatted"
  def testSourcesTimestamp = "testSources.lastFormatted"

  private val configuredRun = ScalariformPlugin.runFormatter(sfScalaJars, sfClasspath _, scalariformOptions, log) _

  def formatSourcesAction = forAllSourcesTask(sourcesTimestamp from mainSources)(configuredRun) describedAs ("Format main Scala sources")
  def testFormatSourcesAction = forAllSourcesTask(testSourcesTimestamp from testSources)(configuredRun) describedAs ("Format test Scala sources")

  override def compileAction = super.compileAction dependsOn (formatSources)
  override def testCompileAction = super.testCompileAction dependsOn (testFormatSources)
}
object ScalariformPlugin {
  val Version = "0.0.4.201007151246"
  val CoreUrl = "http://scalariform.googlecode.com/svn/trunk/update-site/plugins/scalariform.core_" + Version + ".jar"

  /** The version of Scala used to run Scalariform.*/
  val ScalaVersion = "2.8.0"

  val MainClass = "scalariform.commandline.Main"

  def runFormatter(scalaJars: List[File], classpath: () => Option[String], options: Seq[ScalariformOption], log: Logger)(sources: Iterable[Path]): Option[String] = classpath() match {
    case None => Some("Scalariform jar not found. Please run update.")
    case Some(cp) =>
      def run(listOfFiles: File): Option[String] = {
        val fork = new ForkScala(MainClass)
        // Assume InPlace if neither InPlace nor Test are provided
        val finalOpts = if ((options contains InPlace) || (options contains Test)) options else options ++ Seq(InPlace)
        val args = finalOpts.map(_.asArgument)
        withSuccessCode(0, "Scalariform invocation failed") {
          fork(None, Seq("-cp", cp), scalaJars, args ++ Seq("-l=" + listOfFiles.getAbsolutePath), log)
        }
      }
      withTemporaryFile(log, "sbt-scalariform", ".lst") { file =>
        write(file, sources.map(_.absolutePath).mkString(Newline), log) orElse
          run(file)
      }
  }
}
