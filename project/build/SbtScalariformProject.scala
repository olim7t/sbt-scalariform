/* sbt Scalariform plugin
 * Copyright 2010 Olivier Michallat
 */
package com.github.olim7t.sbtscalariform

import sbt._
import com.github.olim7t.sbtscalariform._

class SbtScalariformProject(info: ProjectInfo) extends PluginProject(info)
  with SxrConfig
  with ScalariformConfig
{
  val scalaToolsSnapshotRepo = "Scala-Tools Maven Repository" at "http://scala-tools.org/repo-snapshots"

  val scalariform = "org.scalariform" % "scalariform_2.8.0" % "0.0.5-SNAPSHOT"

  // The test action runs an analysis of the jars that fails with a 2.8.0 jar in the classpath. Desactivating it as
  // a workaround (I don't have tests yet anyway)
  override def testAction = task { None }

  override def managedStyle = ManagedStyle.Maven

  val publishTo =
    if (version.toString.endsWith("-SNAPSHOT"))
      "Scala Tools Nexus (snapshots)" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
    else
      "Scala Tools Nexus (releases)" at "http://nexus.scala-tools.org/content/repositories/releases/"
  
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
}

trait SxrConfig extends BasicScalaProject with MavenStyleScalaPaths with AutoCompilerPlugins {
  val sxr = compilerPlugin("org.scala-tools.sxr" %% "sxr" % "0.2.5")

  override def compileOptions =
    CompileOption("-P:sxr:base-directory:" + mainScalaSourcePath.absolutePath) ::
    CompileOption("-P:sxr:output-formats:vim") ::
    super.compileOptions.toList
}

trait ScalariformConfig extends BasicScalaProject with ScalariformPlugin {
  override def scalariformOptions = Seq(VerboseScalariform)
}

