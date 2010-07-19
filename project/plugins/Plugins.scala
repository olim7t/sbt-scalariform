/* sbt Scalariform plugin
 * Copyright 2010 Olivier Michallat
 */
package com.github.olim7t.sbtscalariform

import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val formatter = "com.github.olim7t" % "sbt-scalariform" % "1.0.0"
}
