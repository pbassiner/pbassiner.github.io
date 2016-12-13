import scalaj.http._

case class Comment(author: String, body: String, date: String)

def issueHtmlUrl(post: String): (String, String) = {
  val issuesJson = getIssuesByLabel(post)
  val first = issuesJson.arr.head.obj
  val htmlUrl = first.get("html_url").fold("")(_.str)
  val commentsUrl = first.get("comments_url").fold("")(_.str)
  (htmlUrl, commentsJsScript(commentsUrl))
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

private[this] def commentsJsScript(commentsUrl: String) = s"""
  <script type="text/javascript">
  $$(function() {
    $$.ajax({
      type: 'GET',
      url: "$commentsUrl",
      headers: {Accept: "application/vnd.github.full+json"},
      success: function(data) {
        if (data.length === 0) {
          $$("#comments").append("\\
            <blockquote>\\
              <p class='blog-comment'>There are no comments yet</p>\\
            </blockquote>\\
          ");
        } else {
          for (var i=0; i<data.length; i++) {
            $$("#comments").append("\\
              <div>\\
                <h4 class='blog-comment-author'>"+data[i].user.login+"</h4>\\
                <p class='blog-comment-meta'>"+data[i].updated_at+"</p>\\
                <blockquote><p class='blog-comment'>"+data[i].body_html+"</p></blockquote>\\
              </div>\\
            ");
          }
        }
      }
    });
  });
  </script>
  """
