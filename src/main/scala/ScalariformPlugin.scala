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

  // Find the Scalariform jar downloaded as a dependency of the plugin
  private def sfClasspath: Option[String] = {
    val searchPaths = info.parent match {
      case None => info.pluginsManagedDependencyPath
      // If the current project inherits from a parent, the plugin could be declared at the parent level
      case Some(p) => info.pluginsManagedDependencyPath +++ p.info.pluginsManagedDependencyPath
    }
    val jarFinder = descendents(searchPaths, "scalariform*.jar")
    if (jarFinder.get.size > 1) log.warn("Multiple scalariform jars found: " + jarFinder.absString)
    if (jarFinder.get.isEmpty) None else Some(jarFinder.absString)
  }

  private def sfScalaJars = {
    val si = getScalaInstance(ScalariformPlugin.ScalaVersion)
    si.libraryJar :: si.compilerJar :: Nil
  }

  def scalariformOptions = Seq[ScalariformOption]()
  def scalariformTestOptions = scalariformOptions

  def scalaSourcesEncoding = "UTF-8"

  lazy val formatSources = formatSourcesAction
  lazy val testFormatSources = testFormatSourcesAction

  def sourcesTimestamp = "sources.lastFormatted"
  def testSourcesTimestamp = "testSources.lastFormatted"

  def formatSourcesAction = forAllSourcesTask(sourcesTimestamp from mainSources) {
    ScalariformPlugin.runFormatter(sfScalaJars, sfClasspath _, scalariformOptions, scalaSourcesEncoding, log) _
  } describedAs ("Format main Scala sources")

  def testFormatSourcesAction = forAllSourcesTask(testSourcesTimestamp from testSources) {
    ScalariformPlugin.runFormatter(sfScalaJars, sfClasspath _, scalariformTestOptions, scalaSourcesEncoding, log) _
  } describedAs ("Format test Scala sources")

  override def compileAction = super.compileAction dependsOn (formatSources)
  override def testCompileAction = super.testCompileAction dependsOn (testFormatSources)
}
object ScalariformPlugin {
  /** The version of Scala used to run Scalariform.*/
  val ScalaVersion = "2.8.0"

  val MainClass = "scalariform.commandline.Main"

  def runFormatter(scalaJars: List[File], classpath: () => Option[String], options: Seq[ScalariformOption], encoding: String, log: Logger)(sources: Iterable[Path]): Option[String] = classpath() match {
    case None => Some("Scalariform jar not found. Try running `;clean-plugins;reload`.")
    case Some(cp) =>
      def run(listOfFiles: File): Option[String] = {
        val fork = new ForkScala(MainClass)
        // Assume InPlace if neither InPlace nor Test are provided
        val finalOpts = if ((options contains InPlace) || (options contains Test)) options else options ++ Seq(InPlace)
        val args = finalOpts.map(_.asArgument)
        withSuccessCode(0, "Scalariform invocation failed") {
          fork(None, Seq("-cp", cp, "-Dfile.encoding=" + encoding), scalaJars, args ++ Seq("-l=" + listOfFiles.getAbsolutePath), log)
        }
      }
      withTemporaryFile(log, "sbt-scalariform", ".lst") { file =>
        write(file, sources.map(_.absolutePath).mkString(Newline), log) orElse
          run(file)
      }
  }
}
