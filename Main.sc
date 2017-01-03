import $file.BlogBuilder, BlogBuilder._
import $file.Config, Config._

import ammonite.ops._
import ammonite.ops.ImplicitWd._

@main
def clean() = {
  %git("checkout", "index.html")
  %git("checkout", "blog/")
  val untrackedFiles = %%git("ls-files", "-o")
  untrackedFiles.out.lines.filter(_.endsWith(".html")).foreach(rm! pwd/RelPath(_))
}

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
