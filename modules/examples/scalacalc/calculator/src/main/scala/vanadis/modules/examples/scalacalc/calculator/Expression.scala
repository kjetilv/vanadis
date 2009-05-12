package vanadis.modules.examples.scalacalc.calculator

class Expression(expr: String) {
  def split:Array[String] = expr.toLowerCase.substring(0, expr.length - 1).split("\\s+")

  val op = split(0).toLowerCase
  val args = new Array[Int](split.length - 1)

  for (i:Int <- 1.to(args.length)) {
    args(i - 1) = Integer.parseInt(split(i))
  }
}