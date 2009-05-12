package net.sf.vanadis.modules.examples.scalacalc.calcservices

import scala.collection.mutable
import ext.{Track, Expose}

trait OperatorWiring {

  @Track { val trackedType = classOf[Adder], val remotable = true }
  def adders: mutable.Set[Adder]

  @Track { val trackedType = classOf[Subtractor], val remotable = true }
  def subtractors: mutable.Set[Subtractor]

  @Track { val trackedType = classOf[Multiplier], val remotable = true }
  def multipliers: mutable.Set[Multiplier]

  @Track { val trackedType = classOf[Divisor], val remotable = true }
  def divisors: mutable.Set[Divisor]

  @Expose
  def calculator: PocketCalculator
}