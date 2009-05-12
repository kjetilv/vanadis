package net.sf.vanadis.modules.examples.scalacalc.add

import calcservices.Adder

class MyAdder extends Adder {

  def add(args: Array[Int]) : Int = {
    var sum = 0;
    args.foreach(i => sum += i)
    sum
  }
}