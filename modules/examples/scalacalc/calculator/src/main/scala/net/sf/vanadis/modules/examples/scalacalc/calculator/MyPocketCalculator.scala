package net.sf.vanadis.modules.examples.scalacalc.calculator

import _root_.java.lang.String
import calcservices._

class MyPocketCalculator(module: CalculatorModule) extends PocketCalculator {

  private val calculatorModule = module

  def calculate(e: String) : Int = {
    def expr = new Expression(e)
    if (expr.op == "add") {
      return calculatorModule.adder.add(expr.args);
    } else if (expr.op == "sub") {
      return calculatorModule.subtractor.sub(expr.args)
    } else if (expr.op == "div") {
      return calculatorModule.divisor.div(expr.args)
    } else {
      return calculatorModule.multiplier.mul(expr.args)
    }
  }
}