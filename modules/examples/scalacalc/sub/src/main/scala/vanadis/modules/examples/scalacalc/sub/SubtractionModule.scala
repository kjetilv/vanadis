package vanadis.modules.examples.scalacalc.sub

import vanadis.modules.examples.scalacalc.calcservices.Subtractor
import vanadis.ext.{Module, Expose}

@Module { val moduleType = "scalacalc-sub" }
class SubtractionModule {

  @Expose
  def getSubtractor() : Subtractor = new MySubtractor()
}
