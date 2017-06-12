[![Website](https://img.shields.io/website-up-down-green-red/https/pbassiner.github.io.svg)](https://pbassiner.github.io/)
[![Travis](https://img.shields.io/travis/pbassiner/pbassiner.github.io/master.svg)](https://travis-ci.org/pbassiner/pbassiner.github.io)
[![License: MIT](https://img.shields.io/badge/license-MIT%20License-blue.svg)](https://raw.githubusercontent.com/pbassiner/pbassiner.github.io/master/LICENSE)

# Disclaimer
This is a personal blog. The opinions expressed here represent my own and not those of my employer.

# About
This blog is hosted on [GitHub](https://github.com/) and it's built using [Scala](http://www.scala-lang.org/), [Ammonite](https://github.com/lihaoyi/Ammonite) and [Bootstrap](http://getbootstrap.com) (with a customized theme based on the [Clean Blog Theme](https://github.com/BlackrockDigital/startbootstrap-clean-blog) by [Start Bootstrap](http://startbootstrap.com/)).

The strategy on building this blog was heavily inspired by [Li Haoyi](https://twitter.com/li_haoyi)'s blog post [Scala Scripting and the 15 Minute Blog Engine](http://www.lihaoyi.com/post/ScalaScriptingandthe15MinuteBlogEngine.html).

## Features
* Mobile responsive
* Comments stored in GitHub issues, one issue per post
* Live fetching of comments from GitHub issues through Javascript
* Twitter sharing through [Tweet Web Intent](https://dev.twitter.com/web/tweet-button/web-intent)
* Post estimated reading time calculation
* RSS/Atom feed

## Strategy
* Generation scripts written in Scala
* Each post written in `Markdown`, filename being `yyyy-MM-dd.Post_Name.md`
* `HTML` content generated from `Markdown` files
* Blog about and post comments section in its own `Markdown` file
* Blog index shows the latest 4 posts, displaying the first 25 words
* Blog posts archive shows reverse sorted list of posts, grouped by month, displaying the first 25 words

## Dependencies used
* [Scala](http://www.scala-lang.org/) `2.12.2`
* [Ammonite](https://github.com/lihaoyi/Ammonite) `1.0.0-RC6`
* [scalatags](https://github.com/lihaoyi/scalatags) `0.6.3`
* [commonmark](https://github.com/atlassian/commonmark-java) `0.9.0`

# License
[MIT License](LICENSE)
