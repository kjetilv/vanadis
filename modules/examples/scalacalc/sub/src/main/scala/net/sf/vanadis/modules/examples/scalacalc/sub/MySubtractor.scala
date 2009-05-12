package net.sf.vanadis.modules.examples.scalacalc.sub

import calcservices.Subtractor

class MySubtractor extends Subtractor {

  def sub(args: Array[Int]) = {
    var sum = args(0);
    args.slice(1).foreach(i => sum-= i)
    sum
  }
}