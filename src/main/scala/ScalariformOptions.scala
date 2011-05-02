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
trait IntOption extends ScalariformOption {
  def name: String
  def value: Int
  override def asArgument = "-" + name + "=" + value
}
case class AlignParameters(enabled: Boolean) extends BooleanOption {
  override def name = "alignParameters"
}
case class AlignSingleLineCaseStatements(enabled: Boolean) extends BooleanOption {
  override def name = "alignSingleLineCaseStatements"
}
case class CompactStringConcatenation(enabled: Boolean) extends BooleanOption {
  override def name = "compactStringConcatenation"
}
case class DoubleIndentClassDeclaration(enabled: Boolean) extends BooleanOption {
  override def name = "doubleIndentClassDeclaration"
}
case class FormatXml(enabled: Boolean) extends BooleanOption {
  override def name = "formatXml"
}
case class IndentLocalDefs(enabled: Boolean) extends BooleanOption {
  override def name = "indentLocalDefs"
}
case class IndentPackageBlocks(enabled: Boolean) extends BooleanOption {
  override def name = "indentPackageBlocks"
}
// Scalariform 0.1.0
//case class IndentWithTabs(enabled: Boolean) extends BooleanOption {
//  override def name = "indentWithTabs"
//}
case class IndentSpaces(value: Int) extends IntOption {
  override def name= "indentSpaces"
}
case class MaxArrowIndent(value: Int) extends IntOption {
  override def name = "alignSingleLineCaseStatements.maxArrowIndent"
}
// Scalariform 0.1.0
//case class MultilineScaladocCommentsStartOnFirstLine(enabled: Boolean) extends BooleanOption {
//  override def name = "multilineScaladocCommentsStartOnFirstLine"
//}
case class PreserveDanglingCloseParenthesis(enabled: Boolean) extends BooleanOption {
  override def name = "preserveDanglingCloseParenthesis"
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
// Scalariform 0.1.0
//case class SpaceInsideBrackets(enabled: Boolean) extends BooleanOption {
//  override def name = "spaceInsideBrackets"
//}
//case class SpaceInsideParentheses(enabled: Boolean) extends BooleanOption {
//  override def name = "spaceInsideParentheses"
//}
//case class SpacesWithinPatternBinders(enabled: Boolean) extends BooleanOption {
//  override def name = "spacesWithinPatternBinders"
//}
case object VerboseScalariform extends ScalariformOption {
  override def asArgument = "-v"
}
case object Test extends ScalariformOption {
  override def asArgument = "-t"
}
case object InPlace extends ScalariformOption {
  override def asArgument = "-i"
}
