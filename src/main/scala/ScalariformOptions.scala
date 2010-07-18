/* sbt Scalariform plugin
 * Copyright 2010 Olivier Michallat
 */
package com.github.olim7t.sbtscalariform

sealed abstract class ScalariformOption {
  def asArgument: String
}
trait BooleanOption extends ScalariformOption {
  def name: String
  def enabled: Boolean
  override def asArgument = (if (enabled) "+" else "-") + name
}
case class AlignParameters(enabled: Boolean) extends BooleanOption {
  override def name = "alignParameters"
}
case class CompactStringConcatenation(enabled: Boolean) extends BooleanOption {
  override def name = "compactStringConcatenation"
}
case class DoubleIndentClassDeclaration(enabled: Boolean) extends BooleanOption {
  override def name = "doubleIndentClassDeclaration"
}
case class IndentSpaces(spaces: Int) extends ScalariformOption {
  override def asArgument = "-indentSpaces=" + spaces
}
case class PreserveSpaceBeforeArguments(enabled: Boolean) extends BooleanOption {
  override def name = "preserveSpaceBeforeArguments"
}
case class RewriteArrowSymbols(enabled: Boolean) extends BooleanOption {
  override def name = "rewriteArrowSymbols"
}
case class SpaceBeforeColon(enabled: Boolean) extends BooleanOption {
  override def name = "spaceBeforeColon"
}
case object VerboseScalariform extends ScalariformOption {
  override def asArgument = "-v"
}
case object Test extends ScalariformOption {
  override def asArgument = "-t"
}
case object InPlace extends ScalariformOption {
  override def asArgument = "-i"
}
