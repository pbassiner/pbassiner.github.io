sealed trait Flag
final case object Enabled extends Flag
final case object Disabled extends Flag

case class Configuration(
  val gitHubIntegration: Flag
)

object Metadata {
  val author = "Pol Bassiner"
  val githubUser = "pbassiner"
  val githubRepo = s"$githubUser.github.io"
  val url = s"https://$githubRepo/"
  val feedUrl = s"${url}feed.xml"
}

object Files {
  val indexFilename = "index.html"
  val rssFeedFilename = "feed.xml"
  val generatedBlogPostsFolder = "blog"
}
