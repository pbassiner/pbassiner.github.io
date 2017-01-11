import java.net.URLEncoder

def tweetPostUrl(text: String, url: String): String =
  return s"https://twitter.com/intent/tweet?text=${URLEncoder.encode(text, "UTF-8")}&url=${URLEncoder.encode(url, "UTF-8")}&via=polbassiner"
