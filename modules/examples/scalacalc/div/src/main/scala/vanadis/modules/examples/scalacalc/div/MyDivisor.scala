package vanadis.modules.examples.scalacalc.div

import calcservices.Divisor

class MyDivisor extends Divisor {

  def div(args: Array[Int]) = {
    var dividend = args(0)
    args.slice(1).foreach(i => dividend /= i)
    dividend
  }
}