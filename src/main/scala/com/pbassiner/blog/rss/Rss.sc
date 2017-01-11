import java.text.SimpleDateFormat
import java.util.Date
import scala.xml.{ Elem, PCData }

final case class Entry(title: String, date: Date, relUrl: String, content: String)

private[this] val dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
private[this] object Metadata {
  val url = "https://pbassiner.github.io/"
  val feedUrl = "https://pbassiner.github.io/feed.xml"
  val author = "Pol Bassiner"
}

def buildFeed(updated: Date, entries: Iterable[Entry]): Elem = {
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
}

private[this] def toEntry(entry: Entry): Elem = {
  val url = s"${Metadata.url}${entry.relUrl}"
  <entry>
    <title type="html">
      { PCData(entry.title) }
    </title>
    <link rel="alternate" type="text/html" href={ url }/>
    <id>{ url }</id>
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
}
