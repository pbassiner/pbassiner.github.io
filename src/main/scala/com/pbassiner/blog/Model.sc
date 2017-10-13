import $ivy.`com.lihaoyi::scalatags:0.6.7`

import scalatags.Text.TypedTag

final case class Blog(
  posts: Iterable[(String, TypedTag[String])],
  index: TypedTag[String],
  archive: TypedTag[String],
  monthlyDigests: TypedTag[String],
  about: TypedTag[String],
  rssFeed: String
)

final case class Post(
  title: String,
  date: String,
  excerpt: String,
  content: String,
  htmlFilename: String,
  relUrl: String,
  url: String,
  filename: String
)
