package net.sf.vanadis.modules.examples.scalacalc.mul

import calcservices.Multiplier
import ext.{Module, Expose}

@Module { val moduleType = "scalacalc-mul" }
class MultiplicationModule {

  @Expose
  def getMultiplier() : Multiplier = new MyMultiplier()
}