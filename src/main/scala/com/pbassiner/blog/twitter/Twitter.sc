import java.net.URLEncoder

import $file.^.builder.MdToHtml, MdToHtml._

def tweetPostUrl(postFilename: String): String = {
  val text = mdFilenameToTitle(postFilename)
  val url = s"https://pbassiner.github.io/blog/${mdFilenameToHtmlFilename(postFilename)}"
  return s"https://twitter.com/intent/tweet?text=${URLEncoder.encode(text, "UTF-8")}&url=${URLEncoder.encode(url, "UTF-8")}&via=polbassiner"
}
