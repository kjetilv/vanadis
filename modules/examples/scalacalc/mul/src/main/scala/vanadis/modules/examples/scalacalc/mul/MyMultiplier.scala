package vanadis.modules.examples.scalacalc.mul

import calcservices.Multiplier

class MyMultiplier extends Multiplier {

  override def mul(args: Array[Int]) = {
    var product = 1;
    args.foreach(product *= _)
    product
  }
}