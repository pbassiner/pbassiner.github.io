import ammonite.ops._
import scala.util.{Success, Failure}

import $file.BlogBuilder, BlogBuilder._
import $file.Config, Config._
import $file.Drafts
import $file.Git

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

@main
def clean() = {
  Git checkout Builder.indexFilename
  Git checkout(Builder.generatedBlogPostsFolder + "/")
  Git.untrackedFiles.filter(_.endsWith(".html")).foreach(rm! pwd/RelPath(_))
}

@main
def publishDrafts() = {
  Drafts.publishDrafts match {
    case Success(_) => publishDev
    case Failure(e) => println(e.getMessage)
  }
}

@main
def cleanDrafts() = {
  clean
  Drafts.cleanDrafts match {
    case Success(_) => ()
    case Failure(e) => println(e.getMessage)
  }
}

private[this] def build(configuration: Configuration) = {
  println(configuration)

  implicit val config: Configuration = configuration
  Builder(config)
}
