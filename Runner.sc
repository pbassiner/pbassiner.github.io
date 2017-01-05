import ammonite.ops._

import $file.src.main.scala.com.pbassiner.blog.Main, Main._

@main
def main(methodArg: String, envArg: String) = {
  val method: Method = methodArg
  val env: Env = envArg

  method match {
    case Publish => Main.publish(env)
    case Clean => Main.clean(env)
  }
}

private[this] sealed trait Method
private[this] case object Publish extends Method
private[this] case object Clean extends Method

private[this] implicit def toMethod(method: String): Method = method match {
  case "publish" => Publish
  case "clean" => Clean
}

private[this] implicit def toEnv(env: String): Env = env match {
  case "prod" => Prod
  case "dev" => Dev
  case "drafts" => Drafts
}
