package net.sf.vanadis.modules.examples.scalacalc.calculator

import ext.{Inject, AbstractModule, Module, Expose}
import scala.collection.mutable
import scalacalc.calcservices._

@Module { val moduleType = "scalacalc-calculator" }
class CalculatorModule {

  @Inject var subtractor:Subtractor = null

  @Inject var adder:Adder = null

  @Inject var divisor:Divisor = null

  @Inject var multiplier:Multiplier = null

  @Expose
  def getCalculator() : PocketCalculator = new MyPocketCalculator(this)
}
