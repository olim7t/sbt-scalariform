An [sbt](http://code.google.com/p/simple-build-tool/) plugin to format your Scala source files with [Scalariform](http://github.com/mdr/scalariform).

#How to use

##Declaring the plugin

This is done in `project/plugins/Plugins.scala`:

	import sbt._
	
	class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
	  val formatter = "com.github.olim7t" % "sbt-scalariform" % "1.0.3"
	}

##Configuring a simple project

Mix the `ScalariformPlugin` trait into your project definition and customize the formatting options if necessary:

	import sbt._
	import com.github.olim7t.sbtscalariform._
	
	class MySbtProject(info: ProjectInfo) extends DefaultProject(info) with ScalariformPlugin {
	  // See ScalariformOptions.scala and the Scalariform documentation for the list of options.
	  // NB: InPlace is assumed unless Test is specified
	  override def scalariformOptions = Seq(VerboseScalariform)
	}

This will by default format all your main and test sources before each compilation.

###Disabling automatic formatting

	  override def formatBeforeCompiling = false

The formatting actions still exist, but are not run automatically. You need to run `format-sources` or `test-format-sources` manually from the sbt prompt.

###Disabling formatting

You can override `formatSourcesAction` or `testFormatSourcesAction` to completely remove the corresponding action. A typical scenario is when you keep automatic mode, but don't want to format the test sources:

	  override def testFormatSourcesAction = task { None }

###Using specific options for test sources

	  override def scalariformTestOptions = Seq(PreserveSpaceBeforeArguments(true))

###Specifying the sources' encoding

The default is UTF-8.

	  override def scalaSourcesEncoding = "ISO-8859-1"

###Controlling failure behavior

By default, a Scalariform failure does not abort the whole build: the error is logged, the files that couldn't be formatted are left as-is, and the build proceeds to compilation. There are two reasons for this:

* in the event of a Scalariform bug, valid Scala code should not be prevented from compiling;
* if the error is a genuine Scala error, the Scala compiler currently provides better error messages.

To override this behavior:

	  // Abort the build if Scalariform fails:
	  override def failOnFormattingError = true

##Configuring a multi-module project

Declare the plugin for the parent project. Mix the trait into the definitions of the sub-modules that you want to format; you can create an intermediary trait to factor the formatting options. Scalariform itself uses this setup: see [here](http://github.com/mdr/scalariform/blob/master/project/build/Project.scala#L6).

##Running

From sbt, reload your project definition by running `reload` (or just launch `sbt` if you were not already in interactive mode). This will also download and compile the plugin.

Your project now has two new actions: `format-sources` and `test-format-sources`, which get run automatically before `compile` and `test-compile` respectively.

#How it works

For each type of sources (main, test), the plugin uses a timestamp file in the target directory to detect which files have changed, and therefore need to be reformatted.

The plugin forks a new VM to invoke Scalariform; this is required, since sbt project definitions are compiled against Scala 2.7.7, while Scalariform uses 2.8.0. The list of files to format is passed through a temporary file (fed to Scalariform's `-l` option).

#Building from source

If you want to try the latest features, or just hack on the plugin, here is the procedure to build it locally:

    git clone git://github.com/olim7t/sbt-scalariform.git
    cd sbt-scalariform
    sbt
    # From the sbt prompt:
    update
    # ... downloads Scalariform ...
    publish-local

If you repeatedly deploy and test the same version of the plugin, remember to run `;clean-plugins ;reload` in the target project(s).

