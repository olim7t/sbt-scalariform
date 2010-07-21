An [sbt](http://code.google.com/p/simple-build-tool/) plugin to format your Scala source files with [Scalariform](http://github.com/mdr/scalariform).

#How to use

##Declaring the plugin

This is done in `project/plugins/Plugins.scala`:

	import sbt._
	
	class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
	  val formatter = "com.github.olim7t" % "sbt-scalariform" % "1.0.0"
	}

##Configuring your project

Mix the `ScalariformPlugin` trait into your project definition and customize the formatting options if necessary:

	import sbt._
	import com.github.olim7t.sbtscalariform._
	
	class MySbtProject(info: ProjectInfo) extends DefaultProject(info) with ScalariformPlugin {
	  // See ScalariformOptions.scala and the Scalariform documentation for the list of options.
	  // NB: InPlace is assumed unless Test is specified
	  override def scalariformOptions = Seq(VerboseScalariform)
	}

This will by default format all your main and test sources. If you need to customize this behavior, you can override the `formatSourcesAction` and `testFormatSourcesAction` methods, for instance:

	  // Completely disable formatting of the tests
	  override def testFormatSourcesAction = task { None }

You can also use a separate set of options for test sources:

	  override def scalariformTestOptions = Seq(PreserveSpaceBeforeArguments(true))

##Running

From sbt, reload your project definition by running `reload` (or just launch `sbt` if you were not already in interactive mode). This will also download and compile the plugin.

**Then** you need to run `update` manually to download Scalariform. *Note:* this is normal sbt behavior, since Scalariform is set up as a dependency of your project (not of the plugin), in order to allow forking (see below).

Your project now has two new actions: `format-sources` and `test-format-sources`, which get run automatically before `compile` and `test-compile` respectively.

#How it works

For each type of sources (main, test), the plugin uses a timestamp file in the target directory to detect which files have changed, and therefore need to be reformatted.

The plugin forks a new VM to invoke Scalariform; this is required, since sbt project definitions are compiled against Scala 2.7.7, while Scalariform uses 2.8.0. The list of files to format is passed through a temporary file (fed to Scalariform's `-l` option).

