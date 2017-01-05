import ammonite.ops._
import ammonite.ops.ImplicitWd._
import scala.util.{Try, Success, Failure}

def checkout(branchOrFile: String): Unit =
  %git("checkout", branchOrFile)

def checkoutNewBranch(branch: String): Unit =
  %git("checkout", "-b", branch)

def branches(): Seq[String] = {
  val branches = %%git("branch")
  branches.out.lines.map(cleanBranchOutput)
}

def branches(exclude: Set[String]): Seq[String] =
  branches.filter(!exclude.contains(_))

def deleteBranch(branch: String): Unit =
  %git("branch", "-D", branch)

def status(): Seq[String] = {
  val result = %%git("status", "-s")
  result.out.lines
}

def untrackedFiles(): Seq[String] = {
  val untrackedFiles = %%git("ls-files", "-o")
  untrackedFiles.out.lines
}

def mergeNoEdit(branch: String): Unit =
  %git("merge", branch, "--no-edit")

def runIfClean(f: () => Try[Unit]): Try[Unit] =
  status length match {
    case 0 => f()
    case _ => {
      Failure(new Exception("Working directory is not clean"))
    }
  }

def checkoutNewBranchAndRun(branch: String, f: () => Try[Unit]): Try[Unit] =
  branches contains(branch) match {
    case true => {
      Failure(new Exception(s"Branch $branch already exists"))
    }
    case false => {
      checkoutNewBranch(branch)
      f()
    }
  }

private[this] def cleanBranchOutput(value: String): String =
  value.replace("*", "").trim()
