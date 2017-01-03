import scala.util.{Try, Success}

import $file.Git

val master = "master"
val test = "test"
val excludedBranches = Set(master, test, "bootstrap4")

def publishDrafts(): Try[Unit] =
  Git runIfClean { () =>
    Git checkout master
    Git checkoutNewBranchAndRun(test, () => {
      Git.branches(excludedBranches).foreach(Git mergeNoEdit _)
      Success(())
    })
  }

def cleanDrafts(): Try[Unit] =
  Git runIfClean { () =>
    Git checkout master
    Git deleteBranch test
    Success(())
  }
