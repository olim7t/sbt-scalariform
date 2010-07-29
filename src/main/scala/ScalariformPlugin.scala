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
  import ScalariformPlugin._

  // Find the Scalariform jar, which has been downloaded as a dependency of the plugin
  private def sfClasspath: Option[String] = {
    def pluginDeps(p: Project) = p.info.pluginsManagedDependencyPath
    val projectSearchPath = pluginDeps(this)
    // The plugin might be declared on the parent project
    val parentSearchPath = info.parent.map(pluginDeps(_)) getOrElse Path.emptyPathFinder

    val jarFinder = descendents(projectSearchPath +++ parentSearchPath, "scalariform*.jar")
    val jars = jarFinder.get
    if (jars.size > 1) log.warn("Multiple scalariform jars found: " + jars)
    if (jars.size == 0)
      None
    else
      Some(Path.makeString(jars))
  }

  private def sfScalaJars = {
    val si = getScalaInstance(ScalaVersion)
    si.libraryJar :: si.compilerJar :: Nil
  }

  def scalariformOptions = Seq[ScalariformOption]()
  def scalariformTestOptions = scalariformOptions

  def scalaSourcesEncoding = "UTF-8"

  lazy val formatSources = formatSourcesAction
  lazy val testFormatSources = testFormatSourcesAction

  def sourcesTimestamp = "sources.lastFormatted"
  def testSourcesTimestamp = "testSources.lastFormatted"

  def formatSourcesAction = forAllSourcesTask(sourcesTimestamp from mainSources) { sources =>
    format(sources, scalariformOptions)
  } describedAs ("Format main Scala sources")

  def testFormatSourcesAction = forAllSourcesTask(testSourcesTimestamp from testSources) { sources =>
    format(sources, scalariformTestOptions)
  } describedAs ("Format test Scala sources")

  override def compileAction = super.compileAction dependsOn (formatSources)
  override def testCompileAction = super.testCompileAction dependsOn (testFormatSources)

  private def format(sources: Iterable[Path], options: Seq[ScalariformOption]): Option[String] = sfClasspath match {
    case None => Some("Scalariform jar not found. Try running `;clean-plugins;reload`.")
    case Some(cp) =>
      def run(fileList: File): Option[String] = {
        val fork = new ForkScala(ScalariformMainClass)
        // Assume InPlace if neither InPlace nor Test are provided
        val finalOpts = if ((options contains InPlace) || (options contains Test)) options else options ++ Seq(InPlace)
        val args = finalOpts.map(_.asArgument)
        withSuccessCode(0, "Scalariform invocation failed") {
          fork(None,
            Seq("-cp", cp, "-Dfile.encoding=" + scalaSourcesEncoding),
            sfScalaJars,
            args ++ Seq("-l=" + fileList.getAbsolutePath),
            log)
        }
      }
      withTemporaryFile(log, "sbt-scalariform", ".lst") { file =>
        write(file, sources.map(_.absolutePath).mkString(Newline), log) orElse
          run(file)
      }
  }
}
object ScalariformPlugin {
  /** The version of Scala used to run Scalariform.*/
  val ScalaVersion = "2.8.0"

  val ScalariformMainClass = "scalariform.commandline.Main"
}
