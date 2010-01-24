package vanadis.modules.examples.scalacalc.div

import vanadis.modules.examples.scalacalc.calcservices.Divisor
import vanadis.ext.{Module, Expose}

@Module { val moduleType = "scalacalc-div" }
class DivisionModule {

  @Expose
  def getDivisor() : Divisor = new MyDivisor()
}
