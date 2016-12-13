sealed trait Flag
final case object Enabled extends Flag
final case object Disabled extends Flag

case class Configuration(
  val gitHubIntegration: Flag
)
