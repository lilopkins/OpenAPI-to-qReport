package uk.hpkns.openapitoqreport

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import com.reprezen.kaizen.oasparser.model3.Schema
import uk.hpkns.openapitoqreport.LocalImplicits._
import org.slf4j.LoggerFactory
import com.reprezen.kaizen.oasparser.model3.MediaType

class Variable(val name: String, val required: Boolean, val schema: Schema) {
  private def log = LoggerFactory.getLogger(getClass)

  def acceptedState: String =
    schema.getType() match
      case "string" =>
        if schema.hasEnums() then schema.getEnum(0).asInstanceOf[String]
        else if schema.getPattern() != null then FakeSource.fakeValuesService.regexify(schema.getPattern().clearRegexTrash)
        else FakeSource.fakeValuesService.bothify("?????###")
      case "boolean" => "true"
      case "number" =>
        val x = FakeSource.random.between(schema.getMinimum().orDouble(0), schema.getMaximum().orDouble(100))
        (x - (x % schema.getMultipleOf().orDouble(Double.MinPositiveValue))).toString()
      case _ =>
        log.error("Unknown type: {}", schema.getType())
        ???

  def tests: List[Test] = requiredTest ::: numberTests ::: regexTests ::: enumTests

  // Tests that come about as a result of whether a parameter is required or not
  def requiredTest: List[Test] =
    if required then (Test("%s Absent".format(name), TestOutcome.Failure) <-- (name, "")) :: Nil
    else             (Test("%s Ignored".format(name), TestOutcome.Success) <-- (name, "")) :: Nil
  
  // Tests that come about from numeric values (BVA)
  def numberTests: List[Test] =
    if schema.getType() != "number" then Nil
    else
      val tests = mutable.ListBuffer[Test]()
      if schema.getMinimum() != null then
        tests += (Test("%s below minimum".format(name), TestOutcome.Failure) <-- (name, (schema.getMinimum().orDouble(0) - 1).toString()))
      if schema.getMaximum() != null then
        tests += (Test("%s above maximum".format(name), TestOutcome.Failure) <-- (name, (schema.getMaximum().orDouble(0) + 1).toString()))
      tests.toList

  // Tests that come about as a result of a regex being specified
  def regexTests: List[Test] =
    if schema.getPattern() != null then (Test("%s fails pattern".format(name), TestOutcome.Failure) <-- (name, "W0ops§Th15!5½v3ry¤1nv4l1d")) :: Nil
    else                                Nil

  // Tests that come about as a result of an enum being specified
  def enumTests: List[Test] =
    if schema.hasEnums() then (Test("%s invalid option".format(name), TestOutcome.Failure) <-- (name, "ThisIsAnInvalidEnumOption")) :: Nil
    else                      Nil
}

object Variable {
  def process(name: String, required: Boolean, schema: Schema): Iterable[Variable] =
    schema.getType() match
      case "object" =>
        val required = schema.getRequiredFields().asScala
        for
          (prop, propSchema) <- schema.getProperties().asScala
          variable <- process("%s-%s".format(name, prop), required.contains(prop), propSchema)
        yield variable
      case "array" => Variable("%s-element".format(name), schema.getMaxLength().orInt(0) != 0, schema.getItemsSchema()) :: Nil
      case _ => Variable(name, required, schema) :: Nil
}
