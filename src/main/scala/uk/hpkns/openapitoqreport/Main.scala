package uk.hpkns.openapitoqreport

import scopt.OParser
import java.io.File
import org.slf4j.LoggerFactory
import com.reprezen.kaizen.oasparser.OpenApi3Parser
import scala.jdk.CollectionConverters._
import scala.collection.mutable
import java.io.BufferedWriter
import java.io.FileWriter

object Main {
  private def log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit =
    val builder = OParser.builder[Config]
    val parser = {
      import builder._
      OParser.sequence(
        programName(BuildInfo.name),
        head(BuildInfo.name, BuildInfo.version),
        opt[Map[String, String]]("defaults")
          .valueName("parameter1=default1,parameter2=default2...")
          .action((x, c) => c.copy(overrideDefaults = x))
          .text("Overrride parameter defaults."),
        arg[File]("<input>")
          .action((f, c) => c.copy(in = f))
          .text("OpenAPI spec input file."),
        arg[File]("<output>")
          .action((f, c) => c.copy(out = f))
          .text("CSV output file."),
      )
    }
    OParser.parse(parser, args, Config()) match
      case Some(config) => run(config)
      case None => sys.exit(1)
    
  def run(config: Config) =
    log.info("Reading OpenAPI spec from {}", config.in.getName())
    val model = OpenApi3Parser().parse(config.in)
    if !model.isValid() then
      log.warn("Invalid API spec!")
      for item <- model.getValidationItems().toArray() do
        log.warn("  {}", item)
    
    val vars = new mutable.ListBuffer[Variable]
    for (id, parameter) <- model.getParameters().asScala do
      val variable = Variable(parameter.getName(), parameter.getRequired(), parameter.getSchema())
      vars += variable
    log.info("{} varying parameters detected.", vars.size)
    
    val tests = new mutable.ListBuffer[Test]
    val defaults = new mutable.HashMap[String, String]
    tests += Test("Valid", TestOutcome.Success)
    for variable <- vars do
      tests ++= variable.tests
      defaults += ((variable.name, variable.acceptedState))
    for (name, value) <- config.overrideDefaults do
      defaults.update(name, value)
    log.info("Generated {} tests", tests.length)
    
    val headings = for variable <- vars yield variable.name
    headings.prepend("ID")
    headings.append("_request_step")
    val headingLine = mutable.StringBuilder()
    for heading <- headings do
      headingLine ++= heading + ","
    headingLine.dropRight(1)

    log.debug("Writing to file.")
    config.out.createNewFile()
    val bw = BufferedWriter(FileWriter(config.out))
    bw.write(headingLine.toString() + "\n")
    var seq = 1
    for test <- tests do
      val line = mutable.StringBuilder()
      line ++= test.name(seq) + ","
      for variable <- vars do
        line ++= test.valOrDefault(variable.name, defaults) + ","
      line ++= (test.expectOutcome match
        case TestOutcome.Success => "PositiveRequest"
        case TestOutcome.Failure => "NegativeRequest")

      bw.write(line.toString() + "\n")
      seq += 1
    bw.flush()
    bw.close()
    log.info("Done!")
}