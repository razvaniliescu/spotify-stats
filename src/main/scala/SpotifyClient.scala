import sttp.client4._

class SpotifyClient(accessToken: String) {
  val backend: WebSocketSyncBackend = DefaultSyncBackend()

  def getTopArtists(timeRange: String = "medium_term"): List[Artist] = {
    val request = basicRequest
      .get(uri"https://api.spotify.com/v1/me/top/artists?time_range=$timeRange&limit=10")
      .header("Authorization", s"Bearer $accessToken")
      .response(asStringAlways)

    val response = request.send(backend)
    JsonParser.parseArtist(response.body)
  }

  def getTopTracks(timeRange: String = "medium_term"): List[Track] = {
    val request = basicRequest
      .get(uri"https://api.spotify.com/v1/me/top/tracks?time_range=$timeRange&limit=10")
      .header("Authorization", s"Bearer $accessToken")
      .response(asStringAlways)

    val response = request.send(backend)
    JsonParser.parseTrack(response.body)
  }
}
