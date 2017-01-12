import $file.^.Config, Config._

private[this] val gistUrl = s"https://gist.github.com/${Metadata.githubUser}/"
private[this] val embeddedScriptPattern =
  s"""<script src="$gistUrl((.*)\\.js\\?(file=.*\\..*))"></script>""".r

final case class Gist(url: String)

def getGist(embeddedScript: String): Option[Gist] =
  embeddedScriptPattern.findFirstMatchIn(embeddedScript) map { matchData =>
    Gist(s"$gistUrl${matchData.group(2)}#${matchData.group(3).replaceAll("[=\\.]", "-").toLowerCase}")
  }
