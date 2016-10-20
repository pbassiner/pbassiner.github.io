import $ivy.`com.lihaoyi::scalatags:0.6.0`
import $ivy.`com.atlassian.commonmark:commonmark:0.5.1`

import ammonite.ops._

// Cleanup
rm! cwd/"index.html"
rm! cwd/'blog

val currentDate = {
  import java.text.SimpleDateFormat
  import java.util.Calendar

  new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime())
}

val postFiles = ls! cwd/'posts
val unsortedPosts = for(path <- postFiles) yield {
  val Array(prefix, suffix) = path.last.split("-")
  (prefix.toInt, suffix, path)
}
def mdNameToHtml(name: String) = {
  name.stripSuffix(".md").replace(" ", "-").toLowerCase + ".html"
}
def mdNameToTitle(name: String) = {
  name.stripSuffix(".md").replace("_", " ")
}
val sortedPosts = unsortedPosts.sortBy(_._1)

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

  val sidebar =
    div(`class` := "col-sm-3 col-sm-offset-1 blog-sidebar")(
      div(`class` := "sidebar-module sidebar-module-inset")(
        h4("About"),
        p("This is a personal blog. The opinions expressed here represent my own and not those of my employer."),
        ul(`class` := "list-unstyled", style := "color: #000000; font-weight: bold",
          li(a(i(`class` := "fa fa-twitter-square"), " Twitter", href := "https://twitter.com/polbassiner", target := "_blank")),
          li(a(i(`class` := "fa fa-linkedin-square"), " LinkedIn", href := "https://es.linkedin.com/in/polbassiner", target := "_blank")),
          li(a(i(`class` := "fa fa-github-square"), " GitHub", href := "https://github.com/pbassiner", target := "_blank"))
        )
      )
    )

  val footerContent =
    footer(`class` := "blog-footer")(
      p("Last build on ", currentDate),
      p("This blog is hosted on ", a("GitHub", href := "https://github.com/"),
        ", built using ", a("Scala", href := "http://www.scala-lang.org/"), ", ",
        a("Ammonite", href := "https://github.com/lihaoyi/Ammonite"), ", and ",
        a("Bootstrap", href := "http://getbootstrap.com"),
        " (with ", a("Blog Theme", href := "http://getbootstrap.com/examples/blog/"),
        " by ", a("@mdo", href := "https://twitter.com/mdo"), ")."
      ),
      p("The strategy on building this blog was heavily inspired by ",
        a("Li Haoyi", href := "https://twitter.com/li_haoyi"),
        "'s blog post ", a("Scala Scripting and the 15 Minute Blog Engine",
        href := "http://www.lihaoyi.com/post/ScalaScriptingandthe15MinuteBlogEngine.html"), "."
      )
    )

  println("POSTS")
  sortedPosts.foreach(println)

  for((_, suffix, path) <- sortedPosts) {
    import org.commonmark.html.HtmlRenderer
    import org.commonmark.node._
    import org.commonmark.parser.Parser

    val postName = mdNameToTitle(suffix)
    val parser = Parser.builder().build()
    val document = parser.parse(read! path)
    val renderer = HtmlRenderer.builder().build()
    val output = renderer.render(document)

    write(
      cwd/'blog/mdNameToHtml(suffix),
      html(
        head(scalatags.Text.tags2.title(postName), bootstrapCss, link(rel := "stylesheet",  href := "../blog.css")),
        body(
          div(`class` := "container")(
            div(`class` := "blog-header")(
              h1(`class` := "blog-title")(a(blogTitle, href := "../index.html"))
            ),
            div(`class` := "row")(
              div(`class` := "col-sm-8 blog-main")(
                div(`class` := "blog-post")(
                  h2(`class` := "blog-post-title")(postName),
                  raw(output)
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

    html(
      head(scalatags.Text.tags2.title(blogTitle), bootstrapCss, link(rel := "stylesheet",  href := "blog.css")),
      body(
        div(`class` := "container")(
          div(`class` := "blog-header")(
            h1(`class` := "blog-title")(blogTitle)
          ),
          div(`class` := "row")(
            div(`class` := "col-sm-8 blog-main")(
              for((_, suffix, _) <- sortedPosts)
              yield h2(a(mdNameToTitle(suffix), href := ("blog/" + mdNameToHtml(suffix))))
            ),
            sidebar
          )
        ),
        footerContent
      )
    ).render
  }

}

write(cwd/"index.html", htmlContent.HTML)
