import $ivy.`com.atlassian.commonmark:commonmark:0.8.0`

import ammonite.ops._
import org.commonmark.node._
import org.commonmark.parser.Parser
import org.commonmark.renderer.html._
import org.commonmark.renderer.NodeRenderer

import $file.^.github.Gists

def mdFilenameToHtmlFilename(name: String): String = name.replace(" ", "-").toLowerCase + ".html"

def mdFilenameToTitle(name: String): String = name.replace("_", " ")

def mdFileToHtml(path: Path): String = mdToHtml(read ! path)

def mdFileFirst25WordsToHtml(path: Path): String = {
  val line = (read.lines ! path).filter(isReadable)(0)

  val lineFirst25Words = getFirst25Words(line)

  mdToHtml(s"$lineFirst25Words...")
}

private[this] def mdToHtml(content: String): String = {
  val parser = Parser.builder().build()
  val document = parser.parse(content)
  val renderer = HtmlRenderer.builder().nodeRendererFactory({ context => new GistScriptNodeRenderer(context) }).build()
  renderer.render(document)
}

private[this] def isReadable: String => Boolean =
  (line) => !line.isEmpty && line.charAt(0).toString.matches("[a-zA-Z]")

private[this] def getFirst25Words(line: String): String =
  line.split("\\s").take(25) mkString " "

private[this] class GistScriptNodeRenderer(val context: HtmlNodeRendererContext) extends NodeRenderer {

  override def getNodeTypes(): java.util.Set[java.lang.Class[_ <: Node]] =
    java.util.Collections.singleton(classOf[HtmlBlock])

  override def render(node: Node): Unit = {
    val html = context.getWriter();
    val inlineHtml = node.asInstanceOf[HtmlBlock].getLiteral

    html.line()
    html.raw(inlineHtml)

    Gists.getGist(inlineHtml) foreach { gist =>
      html.line()
      html.tag("figcaption")
      html.raw(s"""<a href="${gist.url}">${gist.url}</a>""")
      html.tag("/figcaption")
    }

    html.line()
  }
}
