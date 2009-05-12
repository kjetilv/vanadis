package net.sf.vanadis.modules.examples.scalacalc.sub

import calcservices.Subtractor
import ext.{Module, Expose}

@Module { val moduleType = "scalacalc-sub" }
class SubtractionModule {

  @Expose
  def getSubtractor() : Subtractor = new MySubtractor()
}