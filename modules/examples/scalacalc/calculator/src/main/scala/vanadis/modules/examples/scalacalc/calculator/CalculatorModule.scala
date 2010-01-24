package vanadis.modules.examples.scalacalc.calculator

import vanadis.ext.{Inject, Module, Expose}
import vanadis.modules.examples.scalacalc.calcservices._

@Module { val moduleType = "scalacalc-calculator" }
class CalculatorModule {

  @Inject var subtractor:Subtractor = null

  @Inject var adder:Adder = null

  @Inject var divisor:Divisor = null

  @Inject var multiplier:Multiplier = null

  @Expose
  def getCalculator() : PocketCalculator = new MyPocketCalculator(this)
}
