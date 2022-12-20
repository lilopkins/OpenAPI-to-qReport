package uk.hpkns.openapitoqreport

import com.github.javafaker.service.FakeValuesService
import java.util.Locale
import com.github.javafaker.service.RandomService
import scala.util.Random

object FakeSource {
  val random = Random()
  val fakeValuesService = FakeValuesService(Locale("en-GB"), RandomService(random.self))
}
