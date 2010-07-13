/* sbt Scalariform plugin
 * Copyright 2010 Olivier Michallat
 */
package com.github.olim7t.sbtscalariform

import java.io.File
import sbt.{ Control, Logger }

object Actions {
  /** Derived from sbt.FileUtilities.withTemporaryFile, but with an action that returns an Option instead of an Either */
  def withTemporaryFile(log: Logger, prefix: String, postfix: String)(action: File => Option[String]): Option[String] =
    Control.trapUnit("Error creating temporary file: ", log) {
      val file = File.createTempFile(prefix, postfix)
      Control.trapUnitAndFinally("", log) { action(file) } { file.delete() }
    }

  /** Converts an exit code into an Option containing an error message. */
  def withSuccessCode(successCode: Int, errorMessage: String)(action: => Int): Option[String] = action match {
    case `successCode` => None
    case n => Some(errorMessage + " (error code: " + n + ")")
  }
}
