import ammonite.ops._
import scala.util.{ Success, Failure }

import $file.Config, Config._
import $file.builder.DraftsBuilder
import $file.builder.BlogBuilder
import $file.git.Git

sealed trait Env
final case object Prod extends Env
final case object Dev extends Env
final case object Drafts extends Env

def publish(env: Env): Unit = {
  env match {
    case Prod => build(
      Configuration(
        gitHubIntegration = Enabled
      )
    )
    case Dev => build(
      Configuration(
        gitHubIntegration = Disabled
      )
    )
    case Drafts =>
      DraftsBuilder.publish match {
        case Success(_) => publish(Dev)
        case Failure(e) => println(e.getMessage)
      }
  }
}

def clean(env: Env): Unit = {
  env match {
    case Prod | Dev => {
      Git checkout BlogBuilder.indexFilename
      Git checkout (BlogBuilder.generatedBlogPostsFolder + "/")
      Git.untrackedFiles.filter(_.endsWith(".html")).foreach(rm ! pwd / RelPath(_))
    }
    case Drafts => {
      clean(Dev)
      DraftsBuilder.clean match {
        case Success(_) => ()
        case Failure(e) => println(e.getMessage)
      }
    }
  }
}

private[this] def build(configuration: Configuration) = {
  println(configuration)

  implicit val config: Configuration = configuration
  BlogBuilder.Builder(config)
}
