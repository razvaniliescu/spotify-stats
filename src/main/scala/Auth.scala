import sttp.client4._
import sttp.model.Uri
import java.util.Base64

class Auth(clientId: String, clientSecret: String) {
  private val backend: WebSocketSyncBackend = DefaultSyncBackend()
  val credentials = Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes("UTF-8"))

  def getAuthorizationUrl(redirectUri: String, scopes: List[String]): String = {
    val scopeStr = scopes.mkString(" ")
    s"https://accounts.spotify.com/authorize?client_id=$clientId&response_type=code&redirect_uri=$redirectUri&scope=$scopeStr"
  }

  def exchangeAccessToken(code: String, redirectUri: String): String = {
    val request = basicRequest
      .post(uri"https://accounts.spotify.com/api/token")
      .header("Authorization", s"Basic $credentials")
      .body(Map(
        "grant_type" -> "authorization_code",
        "code" -> code,
        "redirect_uri" -> redirectUri
      ))
      .response(asStringAlways)

    val response = request.send(backend)

    io.circe.parser.parse(response.body)
      .flatMap(_.hcursor.get[String]("access_token"))
      .getOrElse(throw new Exception("Failed to get access token"))
  }
  
  def getAccessToken: String = {

    val request = basicRequest
      .post(uri"https://accounts.spotify.com/api/token")
      .header("Authorization", s"Basic $credentials")
      .body(Map("grant_type" -> "client_credentials"))
      .response(asStringAlways)

    val response = request.send(backend)

    io.circe.parser.parse(response.body)
      .flatMap(_.hcursor.get[String]("access_token"))
      .getOrElse(throw new Exception("Failed to get access token"))
  }
}
