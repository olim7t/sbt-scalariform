/* sbt Scalariform plugin
 * Copyright 2010 Olivier Michallat
 */
package com.github.olim7t.sbtscalariform

/** The sole purpose of this file is to rename Option's "orElse" method to "andThen".
 * This is so much clearer when chaining functions that return Some[String] to indicate an error.
 */
object ChainedAction {
	implicit def optionToChainedAction(o: Option[String]) = new ChainedAction(o)
}

class ChainedAction(lastResult: Option[String]) {
	def andThen(nextAction: => Option[String]) = lastResult orElse nextAction
}
