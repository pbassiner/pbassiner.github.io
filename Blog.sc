import $ivy.`com.lihaoyi::scalatags:0.6.0`
import $ivy.`com.atlassian.commonmark:commonmark:0.5.1`

import $file.GitHubClient, GitHubClient._

import ammonite.ops._

import java.text.SimpleDateFormat
import java.util.Calendar

// Cleanup
rm ! pwd / "index.html"
rm ! pwd / 'blog

val utcFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
val monthYearDateFormatter = new SimpleDateFormat("MMMM yyyy")
val commentDateFormatter = new SimpleDateFormat("MMM dd, yyyy")

val currentDate = dateFormatter.format(Calendar.getInstance().getTime())

val postFiles = ls ! pwd / 'posts

val unsortedPosts = for (
  path <- postFiles
) yield {
  val Array(date, postFilename, _) = path.last.split("\\.")
  (date, postFilename, path)
}

val sortedPosts = unsortedPosts.sortBy(_._1).reverse

def mdNameToHtml(name: String): String = {
  name.replace(" ", "-").toLowerCase + ".html"
}

def mdNameToTitle(name: String): String = {
  name.replace("_", " ")
}

def mdToHtml(path: Path): String = {
  import org.commonmark.html.HtmlRenderer
  import org.commonmark.parser.Parser

  val parser = Parser.builder().build()
  val document = parser.parse(read ! path)
  val renderer = HtmlRenderer.builder().build()
  renderer.render(document)
}

object htmlContent {

  import scalatags.Text.all._

  val blogTitle = "Blog"

  val bootstrapCss = List(
    link(
      rel := "stylesheet",
      href := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
    ),
    link(
      rel := "stylesheet",
      href := "https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css"
    )
  )

  val metaViewport = meta(name := "viewport", content := "width=device-width, initial-scale=1.0")

  val sidebar =
    div(`class` := "col-sm-3 col-sm-offset-1 blog-sidebar")(
      div(`class` := "sidebar-module sidebar-module-inset")(
        h4("About"),
        p("This is a personal blog. The opinions expressed here represent my own and not those of my employer."),
        p(strong("Pol Bassiner"), br, "Software Engineer", br, "Java & Scala developer", br, "CTO @ Netquest"),
        ul(`class` := "list-unstyled about-social",
          li(a(i(`class` := "fa fa-twitter-square"), " Twitter", href := "https://twitter.com/polbassiner", target := "_blank")),
          li(a(i(`class` := "fa fa-linkedin-square"), " LinkedIn", href := "https://es.linkedin.com/in/polbassiner", target := "_blank")),
          li(a(i(`class` := "fa fa-github-square"), " GitHub", href := "https://github.com/pbassiner", target := "_blank"))
        )
      )
    )

  val footerContent = {
    val footerPath = pwd / 'common / "footer.md"
    val footerHtml = mdToHtml(footerPath)

    footer(`class` := "blog-footer")(
      raw(footerHtml.replace("CURRENT_DATE", currentDate))
    )
  }

  val postCommentsFooter = {
    val postCommentsFooterPath = pwd / 'common / "postCommentsFooter.md"
    mdToHtml(postCommentsFooterPath)
  }

  println("POSTS")
  sortedPosts.foreach(println)

  for ((postDate, postFilename, path) <- sortedPosts) {
    import org.commonmark.node._

    val postName = mdNameToTitle(postFilename)
    val gitHubIssue = issueHtmlUrl(postFilename)
    val postComments = commentsByPost(postFilename)
    val postContent = mdToHtml(path)

    val comments: scalatags.Text.Modifier = postComments.length match {
      case 0 => blockquote(p(`class` := "blog-comment")("There are no comments yet."))
      case _ => for (
        (author, comment, date) <- postComments
      ) yield {
        val commentDate = commentDateFormatter.format(utcFormatter.parse(date))
        div(
          h4(`class` := "blog-comment-author")(author),
          p(`class` := "blog-comment-meta")(commentDate),
          blockquote(p(`class` := "blog-comment")(comment))
        )
      }
    }

    write(
      pwd / 'blog / mdNameToHtml(postFilename),
      html(
        head(scalatags.Text.tags2.title(postName), bootstrapCss, link(rel := "stylesheet", href := "../blog.css"), metaViewport),
        body(
          div(`class` := "container")(
            div(`class` := "blog-header")(
              h1(`class` := "blog-title")(a(blogTitle, href := "../index.html"))
            ),
            div(`class` := "row")(
              div(`class` := "col-sm-8 blog-main")(
                div(`class` := "blog-post")(
                  h2(`class` := "blog-post-title")(postName),
                  p(`class` := "blog-post-meta")(postDate),
                  div(`class` := "blog-post-body")(
                    raw(postContent),
                    raw(postCommentsFooter.replace("ISSUE_LINK", gitHubIssue)),
                    comments
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

  val HTML = {
    var currIndexMonth = ""
    html(
      head(scalatags.Text.tags2.title(blogTitle), bootstrapCss, link(rel := "stylesheet", href := "blog.css"), metaViewport),
      body(
        div(`class` := "container")(
          div(`class` := "blog-header")(
            h1(`class` := "blog-title")(blogTitle)
          ),
          div(`class` := "row")(
            div(`class` := "col-sm-8 blog-main")(
              for (
                (postDate, postFilename, _) <- sortedPosts
              ) yield {
                val monthYearHeader = monthYearDateFormatter.format(dateFormatter.parse(postDate))
                val monthYearStyle = monthYearHeader match {
                  case s: String if s != currIndexMonth => "margin-bottom: 10em;"
                  case _ => "display: none;"
                }

                currIndexMonth = monthYearHeader

                div(
                  span(`class` := "blog-post-meta", style := monthYearStyle)(monthYearHeader),
                  h2(a(mdNameToTitle(postFilename), href := ("blog/" + mdNameToHtml(postFilename))))
                )
              }
            ),
            sidebar
          )
        ),
        footerContent
      )
    ).render
  }

}

write(pwd / "index.html", htmlContent.HTML)
