import $file.Config, Config._
import $file.Model, Model._
import $file.builder.Builder
import $file.markdown.MdToHtml, MdToHtml._

import ammonite.ops._
import ammonite.ops.Internals.Writable

def generate(config: Configuration): Unit = {
  cleanup

  val blog = Builder.build(
    config,
    getSortedPosts(ls ! pwd / 'posts),
    mdFileToHtml(pwd / 'common / Files.Md.postCommentsFooterFilename),
    mdFileToHtml(pwd / 'common / Files.Md.aboutMeFilename),
    mdFileToHtml(pwd / 'common / Files.Md.aboutBlogFilename)
  )

  writeContent(blog)
}

private[this] def getSortedPosts(paths: Seq[Path]) = {
  val unsortedPosts = for (
    path <- paths
  ) yield {
    val Array(date, filename, _) = path.last.split("\\.")

    val title = mdFilenameToTitle(filename)
    val excerpt = mdFileFirst25WordsToHtmlWithoutAnchors(path)
    val content = mdFileToHtml(path)
    val htmlFilename = mdFilenameToHtmlFilename(filename)
    val relUrl = Files.Html.generatedBlogPostsFolder + "/" + htmlFilename
    val url = s"${Metadata.url}${relUrl}"
    Post(title, date, excerpt, content, htmlFilename, relUrl, url, filename)
  }

  unsortedPosts.sortBy(_.date).reverse
}

private[this] def cleanup = {
  rm ! pwd / Files.Html.indexFilename
  rm ! pwd / Files.Html.archiveFilename
  rm ! pwd / Files.Html.monthlyDigestsFilename
  rm ! pwd / Files.Html.aboutFilename
  rm ! pwd / Files.Html.generatedBlogPostsFolder
  rm ! pwd / Files.rssFeedFilename
}

private[this] def writeContent(blog: Blog) = {
  blog.posts.foreach {
    case (path, postHtml) => write(pwd / RelPath(path), postHtml.render)
  }
  write(pwd / Files.Html.indexFilename, blog.index.render)
  write(pwd / Files.Html.archiveFilename, blog.archive.render)
  write(pwd / Files.Html.monthlyDigestsFilename, blog.monthlyDigests.render)
  write(pwd / Files.Html.aboutFilename, blog.about.render)
  write(pwd / Files.rssFeedFilename, blog.rssFeed)
}
