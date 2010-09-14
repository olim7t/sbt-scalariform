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
import ChainedAction._

trait ScalariformPlugin extends BasicScalaProject with SourceTasks {
  import ScalariformPlugin._

  def scalariformOptions = Seq[ScalariformOption]()
  def scalariformTestOptions = scalariformOptions
  def scalaSourcesEncoding = "UTF-8"
  def sourcesTimestamp = "sources.lastFormatted"
  def testSourcesTimestamp = "testSources.lastFormatted"

  lazy val formatSources = formatSourcesAction
  lazy val testFormatSources = testFormatSourcesAction

  def formatSourcesAction = forAllSourcesTask(sourcesTimestamp from mainSources) { sources =>
    format(sources, scalariformOptions)
  } describedAs ("Format main Scala sources")

  def testFormatSourcesAction = forAllSourcesTask(testSourcesTimestamp from testSources) { sources =>
    format(sources, scalariformTestOptions)
  } describedAs ("Format test Scala sources")

  override def compileAction = super.compileAction dependsOn (formatSources)
  override def testCompileAction = super.testCompileAction dependsOn (testFormatSources)

  private val Scalariform = new ForkScala(ScalariformMainClass)
  private val NoJavaHome = None

  private def format(sources: Iterable[Path], options: Seq[ScalariformOption]): Option[String] = sfClasspath match {
    case None => Some("Scalariform jar not found. Try running `;clean-plugins;reload`.")
    case Some(cp) =>
      def run(fileList: File): Option[String] = {
        val jvmOptions = Seq("-cp", cp, "-Dfile.encoding=" + scalaSourcesEncoding)
        val finalOpts = completeWithDefaults(options)
        val arguments = finalOpts.map(_.asArgument) ++ Seq("-l=" + fileList.getAbsolutePath)

        withSuccessCode(0, "Scalariform invocation failed") {
          Scalariform(NoJavaHome, jvmOptions, sfScalaJars, arguments, log)
        }
      }
      withTemporaryFile(log, "sbt-scalariform", ".lst") { file =>
        write(file, sources.map(_.absolutePath).mkString(Newline), log) andThen run(file)
      }
  }

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

  private def completeWithDefaults(options: Seq[ScalariformOption]) =
    if ((options contains InPlace) || (options contains Test))
    	options
    else
    	options ++ Seq(InPlace)
}
object ScalariformPlugin {
  /** The version of Scala used to run Scalariform.*/
  val ScalaVersion = "2.8.0"

  val ScalariformMainClass = "scalariform.commandline.Main"
}
