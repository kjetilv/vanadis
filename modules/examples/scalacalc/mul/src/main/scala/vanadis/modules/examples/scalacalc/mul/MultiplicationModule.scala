package vanadis.modules.examples.scalacalc.mul

import vanadis.modules.examples.scalacalc.calcservices.Multiplier
import vanadis.ext.{Module, Expose}

@Module { val moduleType = "scalacalc-mul" }
class MultiplicationModule {

  @Expose
  def getMultiplier() : Multiplier = new MyMultiplier()
}
