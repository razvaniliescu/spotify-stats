import java.awt.Desktop
import java.net.URI

object Main extends App {
  val clientId = sys.env.getOrElse("SPOTIFY_CLIENT_ID", "MISSING_CLIENT_ID")
  val clientSecret = sys.env.getOrElse("SPOTIFY_CLIENT_SECRET", "MISSING_CLIENT_SECRET")
  val redirectUri = "http://127.0.0.1:8080/callback"

  private val auth = new Auth(clientId, clientSecret)

  // 1. Start server pentru callback

  // 2. Generează URL-ul de autorizare
  private val authUrl = auth.getAuthorizationUrl(redirectUri, List("user-top-read"))

  println("Opening browser for authentication...")

  if (Desktop.isDesktopSupported) {
    Desktop.getDesktop.browse(new URI(authUrl))
  } else {
    println(s"Please manually open the following URL in your browser: $authUrl")
  }

  SimpleServer.startServer(8080)

  // 3. Aștepți codul automat
  val code = SimpleServer.getCodeBlocking
  println(s"Received code: $code")

  SimpleServer.stopServer()

  // 4. Schimbi codul pe token
  val token = auth.exchangeAccessToken(code, redirectUri)

  private val spotifyClient = new SpotifyClient(token)

  private var running = true
  while (running) {
    println("\nSelect an option:")
    println("[1] Top Artists")
    println("[2] Top Tracks")
    println("[q] Quit")

    val option = scala.io.StdIn.readLine().trim.toLowerCase()

    option match {
      case "1" | "2" =>
        println("\nSelect time range:")
        println("[1] Short Term (last 4 weeks)")
        println("[2] Medium Term (last 6 months)")
        println("[3] Long Term (years)")

        val timeOption = scala.io.StdIn.readLine().trim
        val timeRange = timeOption match {
          case "1" => "short_term"
          case "2" => "medium_term"
          case "3" => "long_term"
          case _ => "medium_term"
        }

        option match {
          case "1" =>
            val artists = spotifyClient.getTopArtists(timeRange)
            println(s"\nTop Artists ($timeRange):")
            artists.zipWithIndex.foreach { case (artist, idx) =>
              println(s"${idx + 1}. ${artist.name}")
            }
          case "2" =>
            val tracks = spotifyClient.getTopTracks(timeRange)
            println(s"\nTop Tracks ($timeRange):")
            tracks.zipWithIndex.foreach { case (track, idx) =>
              println(s"${idx + 1}. ${track.name} - ${track.artist}")
            }
        }

      case "q" | "exit" =>
        println("Exiting...")
        running = false

      case _ =>
        println("Invalid option. Try again.")
    }
  }
}
