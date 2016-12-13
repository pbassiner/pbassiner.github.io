import $file.BlogBuilder, BlogBuilder._
import $file.Config, Config._

import ammonite.ops._

@main
def main(gh: Boolean = false) = {
  implicit val config: Config = new Config {
    override val gitHubIntegration: Boolean = gh
  }

  write(pwd / "index.html", Builder(config))
}
