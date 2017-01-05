import scala.util.{Try, Success}

import $file.^.git.Git

private[this] val master = "master"
private[this] val test = "test"
private[this] val excludedBranches = Set(master, test, "bootstrap4")

def publish(): Try[Unit] =
  Git runIfClean { () =>
    Git checkout master
    Git checkoutNewBranchAndRun(test, () => {
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
