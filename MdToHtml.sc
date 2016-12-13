import $ivy.`com.atlassian.commonmark:commonmark:0.5.1`

import ammonite.ops._

def mdFilenameToHtmlFilename(name: String): String = name.replace(" ", "-").toLowerCase + ".html"

def mdFilenameToTitle(name: String): String = name.replace("_", " ")

def mdFileToHtml(path: Path): String = mdToHtml(read ! path)

def mdFileFirst25WordsToHtml(path: Path): String = {
  val line = (read.lines ! path).filter(isReadable)(0)

  val lineFirst25Words = getFirst25Words(line)

  mdToHtml(s"$lineFirst25Words...")
}

private[this] def mdToHtml(content: String): String = {
  import org.commonmark.html.HtmlRenderer
  import org.commonmark.parser.Parser

  val parser = Parser.builder().build()
  val document = parser.parse(content)
  val renderer = HtmlRenderer.builder().build()
  renderer.render(document)
}

private[this] def isReadable: String => Boolean =
  (line) => !line.isEmpty && line.charAt(0).toString.matches("[a-zA-Z]")

private[this] def getFirst25Words(line: String): String =
  line.split("\\s").take(25) mkString " "
