package net.sf.vanadis.modules.examples.scalacalc.div

import calcservices.Divisor
import ext.{Module, Expose}

@Module { val moduleType = "scalacalc-div" }
class DivisionModule {

  @Expose
  def getDivisor() : Divisor = new MyDivisor()
}