package vanadis.modules.examples.scalacalc.add

import calcservices.Adder

class MyAdder extends Adder {

  override def add(args: Array[Int]) = {
    var sum = 0;
    args.foreach(sum += _)
    sum
  }
}