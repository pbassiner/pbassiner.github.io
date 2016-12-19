[![Website](https://img.shields.io/website-up-down-green-red/https/pbassiner.github.io.svg)](https://pbassiner.github.io/)

# Disclaimer
This is a personal blog. The opinions expressed here represent my own and not those of my employer.

# About
This blog is hosted on [GitHub](https://github.com/) and it's built using [Scala](http://www.scala-lang.org/), [Ammonite](https://github.com/lihaoyi/Ammonite) and [Bootstrap](http://getbootstrap.com) (with a customized theme based on the [Blog Theme](http://getbootstrap.com/examples/blog/) by [@mdo](https://twitter.com/mdo)).

The strategy on building this blog was heavily inspired by [Li Haoyi](https://twitter.com/li_haoyi)'s blog post [Scala Scripting and the 15 Minute Blog Engine](http://www.lihaoyi.com/post/ScalaScriptingandthe15MinuteBlogEngine.html).

## Features
* Mobile responsive
* Comments stored in GitHub issues, one issue per post
* Live fetching of comments from GitHub issues through Javascript
* Twitter sharing through [Tweet Web Intent](https://dev.twitter.com/web/tweet-button/web-intent)

## Dependencies used
* [Scala](http://www.scala-lang.org/) `2.12.0`
* [Ammonite](https://github.com/lihaoyi/Ammonite) `0.8.1`
* [scalatags](https://github.com/lihaoyi/scalatags) `0.6.2`
* [commonmark](https://github.com/atlassian/commonmark-java) `0.8.0`

## Strategy
* Each post written in `Markdown`, filename being `yyyy-MM-dd.Post_Name.md`
* Blog footer and post comments section in its own `Markdown` file
* Blog index shows reverse sorted list of posts, grouped by month, displaying the first 25 words
