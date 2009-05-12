package net.sf.vanadis.modules.examples.scalacalc.mul

import calcservices.Multiplier

class MyMultiplier extends Multiplier {

  def mul(args: Array[Int]) = {
    var product = 1;
    args.foreach(i => product *= i)
    product
  }
}