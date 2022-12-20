package uk.hpkns.openapitoqreport

import java.io.File

final case class Config(
  in: File = null,
  out: File = null,
  overrideDefaults: Map[String, String] = Map(),
)
