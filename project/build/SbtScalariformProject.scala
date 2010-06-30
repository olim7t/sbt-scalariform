package com.github.olim7t.sbtscalariform

import sbt._

class SbtScalariformProject(info: ProjectInfo) extends PluginProject(info) with AutoCompilerPlugins {
	val sxr = compilerPlugin("org.scala-tools.sxr" %% "sxr" % "0.2.5")
	override def compileOptions =
		CompileOption("-P:sxr:base-directory:" + mainScalaSourcePath.absolutePath) ::
		CompileOption("-P:sxr:output-formats:html+vim") ::
		super.compileOptions.toList
}
