package vanadis.modules.examples.scalacalc.sub

import vanadis.modules.examples.scalacalc.calcservices.Subtractor

class MySubtractor extends Subtractor {

  override def sub(args: Array[Int]) = {
    var sum = args(0);
    args.slice(1).foreach(sum-= _)
    sum
  }
}
