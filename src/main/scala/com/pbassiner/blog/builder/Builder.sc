import $ivy.`com.lihaoyi::scalatags:0.6.3`

import $file.DateUtils
import $file.^.Config, Config._
import $file.^.Model, Model._
import $file.^.github.GitHubClient, GitHubClient._
import $file.^.rss.Rss
import $file.^.twitter.Twitter, Twitter._

import scalatags.Text.all._
import scalatags.Text.TypedTag
import scala.collection.immutable.TreeMap

def build(
  config: Configuration,
  posts: Iterable[Post],
  postCommentsFooterHtml: String,
  aboutMeHtml: String,
  aboutBlogHtml: String): Blog = {

  val postsHtml = buildSortedPostsHtml(posts)

  Blog(
    buildPostsHtml(posts, postCommentsFooterHtml, config),
    buildIndex(postsHtml._1),
    buildArchive(postsHtml._2),
    buildAbout(aboutMeHtml, aboutBlogHtml),
    buildRssFeed(posts)
  )
}

import Common._

private[this] def buildPostsHtml(sortedPosts: Iterable[Post], postCommentsFooterHtml: String, config: Configuration) =
  sortedPosts map { post =>
    val gitHubIssue = config.gitHubIntegration match {
      case Enabled => getGitHubIssueByPost(post.filename)
      case Disabled => GitHubIssue.empty
    }

    val postHtml = html(
      headContent(
        post.title,
        Some("../"),
        Some(gitHubIssue.fetchCommentsAndAppendJs)
      ),
      body(
        navbarContent(Some("../")),
        postHeaderContent(
          post.title,
          postMeta(post),
          tweetPostUrl(post.title, post.url)
        ),
        div(`class` := "container post-container")(
          div(`class` := "row")(
            div(`class` := "col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1")(
              raw(post.content),
              raw(postCommentsFooterHtml.replace("ISSUE_LINK", gitHubIssue.htmlUrl)),
              div(id := "comments")
            )
          )
        ),
        footerContent
      )
    )

    (post.relUrl, postHtml)
  }

private[this] def buildSortedPostsHtml(sortedPosts: Iterable[Post]) = {
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
      case post: Post =>
        div(`class` := "post-preview")(
          a(href := post.relUrl)(
            h2(`class` := "post-title", post.title),
            h3(`class` := "post-subtitle", raw(post.excerpt))
          ),
          p(`class` := "post-meta", postMeta(post)),
          twitterShare(tweetPostUrl(post.title, post.url)),
          hr
        )
    })
  }

  val reversedGroupedPostsHtmlByYear = TreeMap(groupedPostsHtmlByYear.toArray: _*)(implicitly[Ordering[String]].reverse)

  val indexPostsHtml = reversedGroupedPostsHtmlByYear.values.flatten.take(4).toList

  val allPostsHtml = reversedGroupedPostsHtmlByYear.map {
    case (year, postList) => div(
      span(`class` := "post-year-meta", year),
      postList.toList
    )
  }.toList

  (indexPostsHtml, allPostsHtml)
}

private[this] def buildIndex(posts: List[TypedTag[String]]) =
  html(
    headContent(Copys.blogTitle),
    body(
      navbarContent(),
      defaultHeaderContent(Copys.blogTitle, Copys.blogSubtitle1, Copys.blogSubtitle2),
      div(`class` := "container")(
        div(`class` := "row")(
          div(`class` := "col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1 index-post-list")(
            posts,
            ul(`class` := "pager")(
              li(`class` := "next", a(href := s"${Files.Html.archiveFilename}", Copys.allPosts))
            )
          )
        )
      ),
      footerContent
    )
  )

private[this] def buildArchive(posts: List[TypedTag[String]]) =
  html(
    headContent(Copys.archiveTitle),
    body(
      navbarContent(),
      defaultHeaderContent(Copys.archiveTitle, Copys.blogSubtitle1, Copys.blogSubtitle2),
      div(`class` := "container")(
        div(`class` := "row")(
          div(`class` := "col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1 archive-post-list")(
            posts
          )
        )
      ),
      footerContent
    )
  )

private[this] def buildAbout(aboutMeHtml: String, aboutBlogHtml: String) =
  html(
    headContent(Copys.aboutTitle),
    body(
      navbarContent(),
      defaultHeaderContent(Copys.aboutTitle, Copys.blogSubtitle1, Copys.blogSubtitle2),
      div(`class` := "container")(
        div(`class` := "row")(
          div(`class` := "col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1 about")(
            div(`class` := "about-me")(raw(aboutMeHtml)),
            br,
            div(`class` := "about-blog")(raw(aboutBlogHtml))
          )
        )
      ),
      footerContent
    )
  )

private[this] def buildRssFeed(posts: Iterable[Post]) = {
  val rssEntries = posts map { post =>
    Rss.Entry(
      post.title,
      DateUtils.toTimestamp(post.date),
      post.url,
      post.content
    )
  }

  Rss.buildFeed(DateUtils.toTimestamp(DateUtils.currentDate), rssEntries)
}

private[this] object Common {

  import scalatags.Text.all._

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
      link(rel := "stylesheet", href := s"${path.getOrElse("")}${Files.Html.cssFilename}"),
      scripts,
      script(`type` := "text/javascript", src := s"${path.getOrElse("")}${Files.Html.jsFilename}"),
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
          a(`class` := "navbar-brand", href := s"${path.getOrElse("")}${Files.Html.indexFilename}", Metadata.author)
        ),
        div(`class` := "collapse navbar-collapse", id := "bs-example-navbar-collapse-1")(
          ul(`class` := "nav navbar-nav navbar-right")(
            li(a(`href` := s"${path.getOrElse("")}${Files.Html.indexFilename}", Copys.homeTitle)),
            li(a(`href` := s"${path.getOrElse("")}${Files.Html.archiveFilename}", Copys.archiveTitle)),
            li(a(`href` := s"${path.getOrElse("")}${Files.Html.aboutFilename}", Copys.aboutTitle))
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
                  a(href := s"https://twitter.com/${Metadata.twitterUser}", target := "_blank")(
                    span(`class` := "fa-stack fa-lg")(
                      i(`class` := "fa fa-circle fa-stack-2x"),
                      i(`class` := "fa fa-twitter fa-stack-1x fa-inverse")
                    )
                  )
                ),
                li(
                  a(href := s"https://es.linkedin.com/in/${Metadata.linkedinUser}", target := "_blank")(
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
              p(`class` := "copyright text-muted", s"Last published on ${DateUtils.currentDate}", br, s"Copyright © ${Metadata.author} ${DateUtils.currentYear}")
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

  def postMeta(post: Post) =
    s"Posted on ${DateUtils.toPostedDate(post.date)} · ${readingTime(post.content)} min read"

  private[this] def readingTime(content: String) =
    Math.round(content.split("\\s").size / 180.0)
}
