/* sbt Scalariform plugin
 * Copyright 2010 Olivier Michallat
 */
package com.github.olim7t.sbtscalariform

import sbt._
import com.github.olim7t.sbtscalariform._

class SbtScalariformProject(info: ProjectInfo) extends PluginProject(info) with AutoCompilerPlugins with ScalariformPlugin {

	// Publishing
  override def managedStyle = ManagedStyle.Maven

  val publishTo =
    if (version.toString.endsWith("-SNAPSHOT"))
      "Scala Tools Nexus (snapshots)" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
    else
      "Scala Tools Nexus (releases)" at "http://nexus.scala-tools.org/content/repositories/releases/"
  
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)

  // SXR
  val sxr = compilerPlugin("org.scala-tools.sxr" %% "sxr" % "0.2.5")

  override def compileOptions =
    CompileOption("-P:sxr:base-directory:" + mainScalaSourcePath.absolutePath) ::
    CompileOption("-P:sxr:output-formats:vim") ::
    super.compileOptions.toList

  // Scalariform
  override def scalariformOptions = Seq(VerboseScalariform)
}
