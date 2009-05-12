package net.sf.vanadis.modules.examples.scalacalc.add

import calcservices.Adder
import ext.{Module, Expose}

@Module { val moduleType = "scalacalc-add" }
class AdditionModule {

  @Expose
  def getAdder() : Adder = new MyAdder();
}