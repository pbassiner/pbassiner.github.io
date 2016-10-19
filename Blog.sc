import $ivy.`com.lihaoyi::scalatags:0.6.0`
import $ivy.`com.atlassian.commonmark:commonmark:0.5.1`

import ammonite.ops._

// Cleanup
rm! cwd/"index.html"
rm! cwd/'blog

val blogTitle = "Blog"
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

val bootstrapCss = {
  import scalatags.Text.all._
  link(
    rel := "stylesheet",
    href := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
  )
}

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
  import scalatags.Text.all._
  write(
    cwd/'blog/mdNameToHtml(suffix),
    html(
      head(scalatags.Text.tags2.title(postName), bootstrapCss),
      body(
        h1(a(blogTitle, href := "../index.html")),
        h1(postName),
        raw(output)
      )
    ).render
  )
}

val HTML = {
  import scalatags.Text.all._

  html(
    head(scalatags.Text.tags2.title(blogTitle), bootstrapCss),
    body(
      h1(blogTitle),
      for((_, suffix, _) <- sortedPosts)
      yield h2(a(mdNameToTitle(suffix), href := ("blog/" + mdNameToHtml(suffix))))
    )
  ).render
}

write(cwd/"index.html", HTML)
