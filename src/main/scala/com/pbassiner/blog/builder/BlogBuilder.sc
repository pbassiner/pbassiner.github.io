import $ivy.`com.lihaoyi::scalatags:0.6.2`

import ammonite.ops._
import ammonite.ops.Internals.Writable
import java.text.SimpleDateFormat
import java.util.Calendar
import scala.collection.immutable.TreeMap
import scalatags.Text.all._

import $file.MdToHtml, MdToHtml._
import $file.^.Config, Config._
import $file.^.github.GitHubClient, GitHubClient._
import $file.^.rss.Rss
import $file.^.twitter.Twitter, Twitter._

private[this] object DateUtils {
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  val monthYearDateFormatter = new SimpleDateFormat("MMMM yyyy")
  val yearMonthDateFormatter = new SimpleDateFormat("yyyy-MM")
  val commentDateFormatter = new SimpleDateFormat("MMM dd, yyyy")

  val currentDate = dateFormatter.format(Calendar.getInstance().getTime())

  def yearMonthDayToYearMonth(date: String): String =
    yearMonthDateFormatter.format(dateFormatter.parse(date))
  def yearMonthToMonthYear(date: String): String =
    monthYearDateFormatter.format(yearMonthDateFormatter.parse(date))
}

private[this] object Common {
  import DateUtils._

  val blogTitle = "Blog"
  val breadcrumbs = "Back"

  val bootstrapCss = List(
    link(rel := "stylesheet", href := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"),
    link(rel := "stylesheet", href := "https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css")
  )

  val metaTags = List(
    meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
    meta(charset := "utf-8")
  )

  val jQuery = script(`type` := "text/javascript", src := "https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js")

  val sidebar =
    div(`class` := "col-sm-3 col-sm-offset-1 blog-sidebar")(
      div(`class` := "sidebar-module sidebar-module-inset")(
        h4("About"),
        p("This is a personal blog. The opinions expressed here represent my own and not those of my employer."),
        p(strong("Pol Bassiner"), br, "Software Engineer", br, "Java & Scala developer", br, "CTO @ Netquest"),
        ul(`class` := "list-unstyled about-social",
          li(a(i(`class` := "fa fa-twitter-square"), " Twitter", href := "https://twitter.com/polbassiner", target := "_blank")),
          li(a(i(`class` := "fa fa-linkedin-square"), " LinkedIn", href := "https://es.linkedin.com/in/polbassiner", target := "_blank")),
          li(a(i(`class` := "fa fa-github-square"), " GitHub", href := s"https://github.com/${Metadata.githubUser}", target := "_blank")),
          li(a(i(`class` := "fa fa-rss-square"), " RSS", href := s"${Metadata.feedUrl}", target := "_blank"))
        )
      )
    )

  val footerContent = {
    val footerPath = pwd / 'common / "footer.md"
    val footerHtml = mdFileToHtml(footerPath)

    footer(`class` := "blog-footer")(
      raw(footerHtml.replace("CURRENT_DATE", currentDate))
    )
  }

}

object Builder {

  import DateUtils._, Common._
  import scalatags.Text.all._

  private[this] final case class Post(
    title: String,
    date: String,
    excerpt: String,
    content: String,
    htmlFilename: String,
    relUrl: String,
    url: String,
    filename: String)

  private[this] def cleanup = {
    rm ! pwd / Files.indexFilename
    rm ! pwd / Files.rssFeedFilename
    rm ! pwd / Files.generatedBlogPostsFolder
  }

  private[this] val sortedPosts = {
    val postFiles = ls ! pwd / 'posts
    val unsortedPosts = for (
      path <- postFiles
    ) yield {
      val Array(date, filename, _) = path.last.split("\\.")

      val title = mdFilenameToTitle(filename)
      val excerpt = mdFileFirst25WordsToHtml(path)
      val content = mdFileToHtml(path)
      val htmlFilename = mdFilenameToHtmlFilename(filename)
      val relUrl = Files.generatedBlogPostsFolder + "/" + htmlFilename
      val url = s"${Metadata.url}${relUrl}"
      Post(title, date, excerpt, content, htmlFilename, relUrl, url, filename)
    }

    unsortedPosts.sortBy(_.date).reverse
  }

  private[this] def postCommentsFooter(gitHubIssueHtmlUrl: String) = {
    val postCommentsFooterPath = pwd / 'common / "postCommentsFooter.md"
    mdFileToHtml(postCommentsFooterPath).replace("ISSUE_LINK", gitHubIssueHtmlUrl)
  }

  private[this] def writePosts(config: Configuration) = {
    for (post <- sortedPosts) {
      val gitHubIssue = config.gitHubIntegration match {
        case Enabled => getGitHubIssueByPost(post.filename)
        case Disabled => GitHubIssue.empty
      }

      write(
        pwd / RelPath(post.relUrl),
        html(
          head(
            scalatags.Text.tags2.title(post.title),
            bootstrapCss,
            link(rel := "stylesheet", href := "../blog.css"),
            metaTags,
            jQuery,
            raw(gitHubIssue.fetchCommentsAndAppendJs)
          ),
          body(
            div(`class` := "container")(
              div(`class` := "blog-header")(
                h1(`class` := "blog-title")(
                  a(
                    raw("&nbsp;"), breadcrumbs,
                    `class` := "breadcrumbs fa fa-angle-left",
                    href := "../" + Files.indexFilename
                  )
                )
              ),
              div(`class` := "row")(
                div(`class` := "col-sm-8 blog-main")(
                  div(`class` := "blog-post")(
                    h2(
                      a(
                        span(`class` := "blog-post-title")(post.title),
                        span(`class` := "fa fa-twitter"),
                        `class` := "share-title",
                        href := tweetPostUrl(post.title, post.url),
                        title := "Share",
                        target := "_blank"
                      )
                    ),
                    p(`class` := "blog-post-meta")(post.date),
                    div(`class` := "blog-post-body")(
                      raw(post.content),
                      raw(postCommentsFooter(gitHubIssue.htmlUrl)),
                      div(id := "comments")
                    )
                  )
                ),
                sidebar
              )
            ),
            footerContent
          )
        ).render
      )
    }
  }

  private[this] val indexSortedPostsList = {
    def logPosts(groupedPostsByMonth: Map[String, Iterable[Post]]): Unit = {
      println("POSTS")
      TreeMap(groupedPostsByMonth.toArray: _*)(implicitly[Ordering[String]].reverse).foreach {
        case (yearMonth, postList) => {
          println(yearMonth)
          postList foreach {
            case post: Post => println(s"\t${post.date}\t${post.title}")
          }
        }
      }
    }

    val groupedPostsByMonth = sortedPosts.groupBy {
      case post: Post => yearMonthDayToYearMonth(post.date)
    }

    logPosts(groupedPostsByMonth)

    val groupedPostsHtmlByMonth = groupedPostsByMonth.map {
      case (yearMonth, postList) => (yearMonth, postList map {
        case post: Post => {
          div(`class` := "row")(
            div(`class` := "col-sm-6 col-md-12")(
              div(`class` := "thumbnail")(
                div(`class` := "caption")(
                  h3(a(post.title, href := post.relUrl)),
                  raw(post.excerpt),
                  a(`class` := "btn btn-primary btn-sm", "Read more", href := post.relUrl),
                  a(
                    span(`class` := "fa fa-twitter"),
                    `class` := "share",
                    style := "float: right;",
                    href := tweetPostUrl(post.title, post.url),
                    title := "Share",
                    target := "_blank"
                  )
                )
              )
            )
          )
        }
      })
    }

    TreeMap(groupedPostsHtmlByMonth.toArray: _*)(implicitly[Ordering[String]].reverse).map {
      case (yearMonth, postList) => div(
        span(`class` := "blog-post-meta")(yearMonthToMonthYear(yearMonth)),
        postList
      )
    }.toList
  }

  private[this] val index =
    html(
      head(
        scalatags.Text.tags2.title(blogTitle),
        bootstrapCss,
        link(rel := "stylesheet", href := "blog.css"),
        metaTags
      ),
      body(
        div(`class` := "container")(
          div(`class` := "blog-header")(
            h1(`class` := "blog-title")(blogTitle)
          ),
          div(`class` := "row")(
            div(`class` := "col-sm-8 blog-main")(
              indexSortedPostsList
            ),
            sidebar
          )
        ),
        footerContent
      )
    )

  private[this] def writeRssFeed(posts: Iterable[Post]) = {
    val rssEntries = posts map { post =>
      Rss.Entry(
        post.title,
        dateFormatter.parse(post.date),
        post.url,
        post.content
      )
    }

    val feed = Rss.buildFeed(dateFormatter.parse(currentDate), rssEntries)
    write(pwd / Files.rssFeedFilename, feed)
  }

  def apply(config: Configuration): Unit = {
    cleanup
    writePosts(config)
    write(pwd / Files.indexFilename, index.render)
    writeRssFeed(sortedPosts)
  }

}
