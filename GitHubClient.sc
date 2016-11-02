import scalaj.http._

case class Comment(author: String, body: String, date: String)

def issueHtmlUrl(post: String) = {
  val issuesJson = getIssuesByLabel(post)
  issuesJson.arr.head.obj.get("html_url").fold("")(_.str)
}

def commentsByPost(post: String) = {
  val issuesJson = getIssuesByLabel(post)

  val issueCommentsUrl = issuesJson.arr.head.obj.get("comments_url").fold("")(_.str)

  val commentsJson = upickle.json.read(
    Http(issueCommentsUrl)
      .asString
      .body
  )

  val comments = for {
    item <- commentsJson.arr
    user <- item.obj.get("user")
    login <- user.obj.get("login")
    body <- item.obj.get("body")
    date <- item.obj.get("updated_at")
  } yield (login.str, body.str, date.str)

  comments.sortBy(_._3).reverse
}

private[this] def getIssuesByLabel(label: String) = {
  upickle.json.read(
    Http(s"https://api.github.com/repos/pbassiner/pbassiner.github.io/issues").param("labels", label)
      .asString
      .body
  )
}
