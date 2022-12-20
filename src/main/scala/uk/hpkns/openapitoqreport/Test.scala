package uk.hpkns.openapitoqreport

import scala.collection.mutable

class Test(val testName: String, val expectOutcome: TestOutcome) {
  private val overriddenValues = mutable.HashMap[String, String]()

  def <--(overrideValue: (String, String)): Test =
    overriddenValues.addOne(overrideValue)
    this
  
  def name(sequence: Int): String = "TC%03d_%s".format(sequence, testName.replace(' ', '_').toUpperCase())

  def valOrDefault(name: String, defaults: mutable.HashMap[String, String]): String =
    if overriddenValues.contains(name) then overriddenValues(name)
    else                                    defaults(name)
}
