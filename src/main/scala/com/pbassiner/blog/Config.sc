sealed trait Flag
final case object Enabled extends Flag
final case object Disabled extends Flag

case class Configuration(
  val gitHubIntegration: Flag
)

object Metadata {
  val url = "https://pbassiner.github.io/"
  val feedUrl = "https://pbassiner.github.io/feed.xml"
  val author = "Pol Bassiner"
}

object Files {
  val indexFilename = "index.html"
  val rssFeedFilename = "feed.xml"
  val generatedBlogPostsFolder = "blog"
}
