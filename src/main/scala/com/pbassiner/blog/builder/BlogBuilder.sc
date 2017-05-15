import $ivy.`com.lihaoyi::scalatags:0.6.3`

import ammonite.ops._
import ammonite.ops.Internals.Writable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import scala.collection.immutable.TreeMap
import scalatags.Text.all._

import $file.MdToHtml, MdToHtml._
import $file.^.Config, Config._
import $file.^.github.GitHubClient, GitHubClient._
import $file.^.rss.Rss
import $file.^.twitter.Twitter, Twitter._

private[this] object DateUtils {
  private[this] val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  private[this] val yearDateFormatter = new SimpleDateFormat("yyyy")
  private[this] val postIndexDateFormatter = new SimpleDateFormat("MMMM dd, yyyy")

  val currentDate = dateFormatter.format(Calendar.getInstance().getTime())

  def toTimestamp(date: String): Date =
    dateFormatter.parse(date)
  def toYear(date: String): String =
    yearDateFormatter.format(dateFormatter.parse(date))
  def toPostedDate(date: String): String =
    postIndexDateFormatter.format(dateFormatter.parse(date))
}

private[this] object Common {

  val blogTitle = "Blog"
  val blogSubtitle1 = "This is a personal blog."
  val blogSubtitle2 = "The opinions expressed here represent my own and not those of my employer."
  val homeTitle = "Home"
  val aboutTitle = "About"
  val archiveTitle = "Posts"

  private[this] val metaTags = List(
    meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
    meta(charset := "utf-8")
  )

  private[this] val cssLinks = List(
    link(`type` := "text/css", rel := "stylesheet", href := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css")
  )

  private[this] val scripts = List(
    script(`type` := "text/javascript", src := "https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"),
    script(`type` := "text/javascript", src := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js")
  )

  private[this] val fonts = List(
    link(`type` := "text/css", rel := "stylesheet", href := "https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css"),
    link(`type` := "text/css", rel := "stylesheet", href := "https://fonts.googleapis.com/css?family=Lora:400,700,400italic,700italic"),
    link(`type` := "text/css", rel := "stylesheet", href := "https://fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,800italic,400,300,600,700,800")
  )

  def headContent(title: String, path: Option[String] = None, rawHead: Option[String] = None) =
    head(
      scalatags.Text.tags2.title(title),
      metaTags,
      cssLinks,
      link(rel := "stylesheet", href := s"${path.getOrElse("")}blog.css"),
      scripts,
      script(`type` := "text/javascript", src := s"${path.getOrElse("")}blog.js"),
      fonts,
      raw(rawHead.getOrElse(""))
    )

  def navbarContent(path: Option[String] = None) =
    scalatags.Text.tags2.nav(`class` := "navbar navbar-default navbar-custom navbar-fixed-top")(
      div(`class` := "container")(
        div(`class` := "navbar-header page-scroll")(
          button(`type` := "button", `class` := "navbar-toggle", attr("data-toggle") := "collapse", attr("data-target") := "#bs-example-navbar-collapse-1")(
            span(`class` := "sr-only", "Toggle navigation"),
            "Menu",
            i(`class` := "fa fa-bars")
          ),
          a(`class` := "navbar-brand", href := s"${path.getOrElse("")}${Files.indexFilename}", Metadata.author)
        ),
        div(`class` := "collapse navbar-collapse", id := "bs-example-navbar-collapse-1")(
          ul(`class` := "nav navbar-nav navbar-right")(
            li(a(`href` := s"${path.getOrElse("")}${Files.indexFilename}", homeTitle)),
            li(a(`href` := s"${path.getOrElse("")}${Files.archiveFilename}", archiveTitle)),
            li(a(`href` := s"${path.getOrElse("")}${Files.aboutFilename}", aboutTitle))
          )
        )
      )
    )

  def defaultHeaderContent(headerTitle: String, headerSubtitle1: String, headerSubtitle2: String) =
    header(`class` := "intro-header")(
      div(`class` := "container")(
        div(`class` := "row")(
          div(`class` := "col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1")(
            div(`class` := "site-heading")(
              h1(headerTitle),
              hr(`class` := "small"),
              span(`class` := "subheading")(headerSubtitle1),
              span(`class` := "subheading")(headerSubtitle2)
            )
          )
        )
      )
    )

  def postHeaderContent(headerTitle: String, headerMeta: String, headerShare: String) =
    header(`class` := "intro-header")(
      div(`class` := "container")(
        div(`class` := "row")(
          div(`class` := "col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1")(
            div(`class` := "post-heading")(
              h1(headerTitle),
              span(`class` := "meta")(headerMeta),
              span(`class` := "meta share")(twitterShare(headerShare))
            )
          )
        )
      )
    )

  val footerContent =
    List(
      hr,
      footer(
        div(`class` := "container")(
          div(`class` := "row")(
            div(`class` := "col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1")(
              ul(`class` := "list-inline text-center")(
                li(
                  a(href := "https://twitter.com/polbassiner", target := "_blank")(
                    span(`class` := "fa-stack fa-lg")(
                      i(`class` := "fa fa-circle fa-stack-2x"),
                      i(`class` := "fa fa-twitter fa-stack-1x fa-inverse")
                    )
                  )
                ),
                li(
                  a(href := "https://es.linkedin.com/in/polbassiner", target := "_blank")(
                    span(`class` := "fa-stack fa-lg")(
                      i(`class` := "fa fa-circle fa-stack-2x"),
                      i(`class` := "fa fa-linkedin fa-stack-1x fa-inverse")
                    )
                  )
                ),
                li(
                  a(href := s"https://github.com/${Metadata.githubUser}", target := "_blank")(
                    span(`class` := "fa-stack fa-lg")(
                      i(`class` := "fa fa-circle fa-stack-2x"),
                      i(`class` := "fa fa-github fa-stack-1x fa-inverse")
                    )
                  )
                ),
                li(
                  a(href := s"${Metadata.feedUrl}", target := "_blank")(
                    span(`class` := "fa-stack fa-lg")(
                      i(`class` := "fa fa-circle fa-stack-2x"),
                      i(`class` := "fa fa-rss fa-stack-1x fa-inverse")
                    )
                  )
                )
              ),
              p(`class` := "copyright text-muted", "Last published on ", DateUtils.currentDate, br, s"Copyright © ${Metadata.author} 2017")
            )
          )
        )
      )
    )

  def twitterShare(url: String) =
    a(
      span(`class` := "fa fa-twitter"),
      `class` := "share",
      href := url,
      title := "Share",
      target := "_blank"
    )

  def readingTime(content: String) = Math.round(content.split("\\s").size / 180.0)
}

object Builder {

  import Common._
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
    rm ! pwd / Files.aboutFilename
    rm ! pwd / Files.archiveFilename
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
      val excerpt = mdFileFirst25WordsToHtmlWithoutAnchors(path)
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
          headContent(
            post.title,
            Some("../"),
            Some(gitHubIssue.fetchCommentsAndAppendJs)
          ),
          body(
            navbarContent(Some("../")),
            postHeaderContent(
              post.title,
              s"Posted on ${DateUtils.toPostedDate(post.date)} · ${readingTime(post.content)} min read",
              tweetPostUrl(post.title, post.url)
            ),
            div(`class` := "container post-container")(
              div(`class` := "row")(
                div(`class` := "col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1")(
                  raw(post.content),
                  raw(postCommentsFooter(gitHubIssue.htmlUrl)),
                  div(id := "comments")
                )
              )
            ),
            footerContent
          )
        ).render
      )
    }
  }

  private[this] val sortedPostsHtml = {
    def logPosts(groupedPostsByYear: Map[String, Iterable[Post]]): Unit = {
      println("POSTS")
      TreeMap(groupedPostsByYear.toArray: _*)(implicitly[Ordering[String]].reverse).foreach {
        case (year, postList) => {
          println(year)
          postList foreach {
            case post: Post => println(s"\t${post.date}\t${post.title}")
          }
        }
      }
    }

    val groupedPostsByYear = sortedPosts.groupBy {
      case post: Post => DateUtils.toYear(post.date)
    }

    logPosts(groupedPostsByYear)

    val groupedPostsHtmlByYear = groupedPostsByYear.map {
      case (year, postList) => (year, postList map {
        case post: Post => {
          div(`class` := "post-preview")(
            a(href := post.relUrl)(
              h2(`class` := "post-title", post.title),
              h3(`class` := "post-subtitle", raw(post.excerpt))
            ),
            p(`class` := "post-meta", s"Posted on ${DateUtils.toPostedDate(post.date)} · ${readingTime(post.content)} min read"),
            twitterShare(tweetPostUrl(post.title, post.url)),
            hr
          )
        }
      })
    }

    val reversedGroupedPostsHtmlByYear = TreeMap(groupedPostsHtmlByYear.toArray: _*)(implicitly[Ordering[String]].reverse)

    val indexPostsHtml = reversedGroupedPostsHtmlByYear.values.flatten.take(4).toList

    val allPostsHtml = reversedGroupedPostsHtmlByYear.map {
      case (year, postList) => div(
        span(`class` := "post-year-meta", year),
        postList
      )
    }.toList

    (indexPostsHtml, allPostsHtml)
  }

  private[this] val index =
    html(
      headContent(blogTitle),
      body(
        navbarContent(),
        defaultHeaderContent(blogTitle, blogSubtitle1, blogSubtitle2),
        div(`class` := "container")(
          div(`class` := "row")(
            div(`class` := "col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1 index-post-list")(
              sortedPostsHtml._1,
              ul(`class` := "pager")(
                li(`class` := "next", a(href := s"${Files.archiveFilename}", "All Posts →"))
              )
            )
          )
        ),
        footerContent
      )
    )

  private[this] val archive =
    html(
      headContent(archiveTitle),
      body(
        navbarContent(),
        defaultHeaderContent(archiveTitle, blogSubtitle1, blogSubtitle2),
        div(`class` := "container")(
          div(`class` := "row")(
            div(`class` := "col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1 archive-post-list")(
              sortedPostsHtml._2
            )
          )
        ),
        footerContent
      )
    )

  private[this] val about = {
    val aboutHtml = mdFileToHtml(pwd / 'common / "about.md")

    html(
      headContent(aboutTitle),
      body(
        navbarContent(),
        defaultHeaderContent(aboutTitle, blogSubtitle1, blogSubtitle2),
        div(`class` := "container")(
          div(`class` := "row")(
            div(`class` := "col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1 about")(
              p(
                strong(Metadata.author), br,
                "Software Engineer", br,
                "Java & Scala developer", br,
                "CTO @ ", a(href := "https://www.netquest.com", target := "_blank", "Netquest")
              ),
              br,
              div(`class` := "about-blog")(raw(aboutHtml))
            )
          )
        ),
        footerContent
      )
    )
  }

  private[this] def writeRssFeed(posts: Iterable[Post]) = {
    val rssEntries = posts map { post =>
      Rss.Entry(
        post.title,
        DateUtils.toTimestamp(post.date),
        post.url,
        post.content
      )
    }

    val feed = Rss.buildFeed(DateUtils.toTimestamp(DateUtils.currentDate), rssEntries)
    write(pwd / Files.rssFeedFilename, feed)
  }

  def apply(config: Configuration): Unit = {
    cleanup
    writePosts(config)
    write(pwd / Files.indexFilename, index.render)
    write(pwd / Files.aboutFilename, about.render)
    write(pwd / Files.archiveFilename, archive.render)
    writeRssFeed(sortedPosts)
  }

}
