package uk.hpkns.openapitoqreport

object LocalImplicits {
  implicit class RichString(self: String) {
    def clearRegexTrash: String =
      val intermediate = if self.startsWith("^") then self.substring(1) else self
      if intermediate.endsWith("$") then intermediate.substring(0, intermediate.length() - 1)
      else                               intermediate
  }

  implicit class RichNumber(self: Number) {
    def orDouble(alternative: Double): Double =
      if self == null then alternative
      else                 self.doubleValue()
  }
}
