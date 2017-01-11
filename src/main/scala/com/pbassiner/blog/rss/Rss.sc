import java.text.SimpleDateFormat
import java.util.Date
import scala.xml.{ Elem, PCData }

import $file.^.Config, Config._

final case class Entry(title: String, date: Date, url: String, content: String)

private[this] val dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

def buildFeed(updated: Date, entries: Iterable[Entry]): String = {
  val feed =
    <feed xmlns="http://www.w3.org/2005/Atom" xml:lang="en">
      <title>Pol Bassiner</title>
        <link rel="self" type="application/atom+xml" href={ Metadata.feedUrl }/>
        <link rel="alternate" type="text/html" href={ Metadata.url }/>
        <updated>{ dateFormatter.format(updated) }</updated>
        <id>{ Metadata.url }</id>
        <author>
          <name>{ Metadata.author }</name>
          <uri>{ Metadata.url }</uri>
        </author>
        { entries.map(toEntry(_)) }
    </feed>

  s"""<?xml version="1.0" encoding="utf-8"?>${feed}"""
}

private[this] def toEntry(entry: Entry): Elem =
  <entry>
    <title type="html">
      { PCData(entry.title) }
    </title>
    <link rel="alternate" type="text/html" href={ entry.url }/>
    <id>{ entry.url }</id>
    <published>{ dateFormatter.format(entry.date) }</published>
    <updated>{ dateFormatter.format(entry.date) }</updated>
    <author>
      <name>{ Metadata.author }</name>
      <uri>{ Metadata.url }</uri>
    </author>
    <content type="html">
      { PCData(entry.content) }
    </content>
  </entry>
