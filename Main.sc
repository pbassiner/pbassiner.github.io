import $file.BlogBuilder, BlogBuilder._
import $file.Config, Config._

import ammonite.ops._

@main
def publishDev() = {
  build(
    Configuration(
      gitHubIntegration = Disabled
    )
  )
}

@main
def publishProd() = {
  build(
    Configuration(
      gitHubIntegration = Enabled
    )
  )
}

private[this] def build(configuration: Configuration) = {
  println(configuration)

  implicit val config: Configuration = configuration

  write(pwd / "index.html", Builder(config))
}
