/* sbt Scalariform plugin
 * Copyright 2010 Olivier Michallat
 */
package com.github.olim7t.sbtscalariform

import sbt._
import java.io.File

trait TimestampSources extends NotNull {
	def timestamp: File
	def sources: Iterable[Path]
}
trait TimestampWrapper extends NotNull {
	def from(sources: => Iterable[Path]): TimestampSources =
		from(Path.lazyPathFinder(sources))
	def from(sources: PathFinder): TimestampSources
}

/** Provides methods to define tasks with basic conditional execution based on the sources of
* the task and a timestamp file in the output directory.
* When the task is run, we want to process sources newer than the timestamp; then the timestamp
* is touched.
*/
trait SourceTasks extends Project {
	implicit def wrapTimestamp(name: String): TimestampWrapper =
		SourceTasks.wrapTimestamp(outputPath, name)

	/** Runs an individual action on each modified source. */
	def forEachSourceTask(label: String, files: TimestampSources)(action: Path => Option[String]): Task =
		task { SourceTasks.processEach(label, files, log)(action) }
	def forEachSourceTask(files: TimestampSources)(action: Path => Option[String]): Task = forEachSourceTask("", files)(action)

	/** Runs a global action that takes the list of modified sources. */
	def forAllSourcesTask(label: String, files: TimestampSources)(action: Iterable[Path] => Option[String]): Task =
		task { SourceTasks.processAll(label, files, log)(action) }
	def forAllSourcesTask(files: TimestampSources)(action: Iterable[Path] => Option[String]): Task =
		forAllSourcesTask("", files)(action)
}
object SourceTasks {
	implicit def wrapTimestamp(basePath: Path, name: String): TimestampWrapper = new TimestampWrapper {
		def from(sourceFinder: PathFinder) = new TimestampSources {
			def timestamp = basePath / name asFile
			def sources = sourceFinder.get
		}
	}
	def processEach(label: String, files: TimestampSources, log: Logger)(action: Path => Option[String]): Option[String] = {
		import files._
		val fileErrors = sources.filter(_.lastModified > timestamp.lastModified).flatMap(action(_))
		val touchError = FileUtilities.touch(timestamp, log)
		val errors = fileErrors ++ touchError
		if (errors.isEmpty) None else Some(errors mkString "\n")
	}
	def processAll(label: String, files: TimestampSources, log: Logger)(globalAction: Iterable[Path] => Option[String]): Option[String] = {
		import files._
		val modified = sources.filter(_.lastModified > timestamp.lastModified)
		if (modified isEmpty)
			None
		else
			globalAction(modified) orElse
				FileUtilities.touch(timestamp, log)
	}
}
