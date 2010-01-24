package vanadis.modules.examples.scalacalc.add

import vanadis.modules.examples.scalacalc.calcservices.Adder
import vanadis.ext.{Module, Expose}

@Module { val moduleType = "scalacalc-add" }
class AdditionModule {

  @Expose
  def getAdder() : Adder = new MyAdder()
}
