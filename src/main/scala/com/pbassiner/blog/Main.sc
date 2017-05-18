import ammonite.ops._
import scala.util.{ Failure, Success, Try }

import $file.Config, Config._
import $file.Generator
import $file.git.Git

sealed trait Env
final case object Prod extends Env
final case object Dev extends Env
final case object Drafts extends Env

def publish(env: Env): Unit = {
  env match {
    case Prod => generate(
      Configuration(
        gitHubIntegration = Enabled
      )
    )
    case Dev => generate(
      Configuration(
        gitHubIntegration = Disabled
      )
    )
    case Drafts =>
      DraftsPublisher.publish match {
        case Success(_) => publish(Dev)
        case Failure(e) => println(e.getMessage)
      }
  }
}

def clean(env: Env): Unit = {
  env match {
    case Prod | Dev => {
      Git checkout Files.Html.indexFilename
      Git checkout Files.Html.archiveFilename
      Git checkout Files.Html.aboutFilename
      Git checkout (Files.Html.generatedBlogPostsFolder + "/")
      Git checkout Files.rssFeedFilename
      Git.untrackedFiles.filter(_.endsWith(".html")).foreach(rm ! pwd / RelPath(_))
    }
    case Drafts => {
      clean(Dev)
      DraftsPublisher.clean match {
        case Success(_) => ()
        case Failure(e) => println(e.getMessage)
      }
    }
  }
}

private[this] def generate(configuration: Configuration) = {
  println(configuration)
  Generator.generate(configuration)
}

private[this] object DraftsPublisher {
  private[this] val master = "master"
  private[this] val test = "test"
  private[this] val excludedBranches = Set(master, test, "bootstrap4")

  def publish(): Try[Unit] =
    Git runIfClean { () =>
      Git checkout master
      Git checkoutNewBranchAndRun (test, () => {
        Git.branches(excludedBranches).foreach(Git mergeNoEdit _)
        Success(())
      })
    }

  def clean(): Try[Unit] =
    Git runIfClean { () =>
      Git checkout master
      Git deleteBranch test
      Success(())
    }
}
