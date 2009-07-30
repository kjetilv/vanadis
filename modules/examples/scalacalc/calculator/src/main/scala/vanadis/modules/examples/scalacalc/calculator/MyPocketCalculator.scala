package vanadis.modules.examples.scalacalc.calculator

import _root_.java.lang.String
import calcservices._

class MyPocketCalculator(module: CalculatorModule) extends PocketCalculator {

  private val calculatorModule = module

  override def calculate(e: String) : Int = {
    def expr = new Expression(e)
    if (expr.op == "add") {
      calculatorModule.adder.add(expr.args);
    } else if (expr.op == "sub") {
      calculatorModule.subtractor.sub(expr.args)
    } else if (expr.op == "div") {
      calculatorModule.divisor.div(expr.args)
    } else {
      calculatorModule.multiplier.mul(expr.args)
    }
  }
}