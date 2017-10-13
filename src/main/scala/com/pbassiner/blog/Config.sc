sealed trait Flag
final case object Enabled extends Flag
final case object Disabled extends Flag

case class Configuration(
  val gitHubIntegration: Flag
)

object Metadata {
  val author = "Pol Bassiner"
  val twitterUser = "polbassiner"
  val linkedinUser = "polbassiner"
  val githubUser = "pbassiner"
  val githubRepo = s"$githubUser.github.io"
  val url = s"https://$githubRepo/"
  val feedUrl = s"${url}feed.xml"
}

object Files {
  val rssFeedFilename = "feed.xml"

  object Html {
    val indexFilename = "index.html"
    val archiveFilename = "archive.html"
    val monthlyDigestsFilename = "monthlydigests.html"
    val aboutFilename = "about.html"
    val generatedBlogPostsFolder = "blog"
    val cssFilename = "blog.css"
    val jsFilename = "blog.js"
  }

  object Md {
    val postCommentsFooterFilename = "postCommentsFooter.md"
    val aboutMeFilename = "aboutMe.md"
    val aboutBlogFilename = "aboutBlog.md"
  }
}

object Copys {
  val blogTitle = "Blog"
  val blogSubtitle1 = "This is a personal blog."
  val blogSubtitle2 = "The opinions expressed here represent my own and not those of my employer."
  val homeTitle = "Home"
  val aboutTitle = "About"
  val archiveTitle = "Posts"
  val monthlyDigestsTitle = "Monthly Digests"
  val allPosts = "All Posts →"
}
