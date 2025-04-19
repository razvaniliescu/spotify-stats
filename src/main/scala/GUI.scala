import java.awt.Desktop
import java.net.URI
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ComboBox, Label, ListCell, ListView}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.image.{Image, ImageView}
import scalafx.collections.ObservableBuffer
import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.geometry.{Insets, Pos}

object GUI extends JFXApp3 {
  var spotifyClient: SpotifyClient = _

  override def start(): Unit = {
    val clientId = sys.env.getOrElse("SPOTIFY_CLIENT_ID", "MISSING_CLIENT_ID")
    val clientSecret = sys.env.getOrElse("SPOTIFY_CLIENT_SECRET", "MISSING_CLIENT_SECRET")
    val redirectUri = "http://127.0.0.1:8080/callback"

    val auth = new Auth(clientId, clientSecret)

    // == AUTHENTICATION FLOW ==
    val authUrl = auth.getAuthorizationUrl(redirectUri, List("user-top-read"))

    println("Opening browser for authentication...")

    if (Desktop.isDesktopSupported) {
      Desktop.getDesktop.browse(new URI(authUrl))
    } else {
      println(s"Please manually open the following URL in your browser: $authUrl")
    }

    SimpleServer.startServer(8080)
    val code = SimpleServer.getCodeBlocking
    println(s"Received code: $code")
    SimpleServer.stopServer()

    val token = auth.exchangeAccessToken(code, redirectUri)

    spotifyClient = new SpotifyClient(token)

    // == GUI ==

    val artistsList = new ListView[Artist]()
    val tracksList = new ListView[Track]()

    val timeRanges = Seq(
      "Last 4 Weeks" -> "short_term",
      "Last 6 Months" -> "medium_term",
      "All Time" -> "long_term"
    )

    val timeRangeCombo = new ComboBox[String](timeRanges.map(_._1)) {
      value = timeRanges.head._1
    }

    val getArtistsButton = new Button("Get Top Artists")
    val getTracksButton = new Button("Get Top Tracks")

    getArtistsButton.onAction = _ => {
      val selectedLabel = timeRangeCombo.value.value
      val selectedTimeRange = timeRanges.find(_._1 == selectedLabel).map(_._2).getOrElse("medium_term")
      val artists = spotifyClient.getTopArtists(selectedTimeRange)

      artistsList.items = ObservableBuffer(artists: _*)
    }

    getTracksButton.onAction = _ => {
      val selectedLabel = timeRangeCombo.value.value
      val selectedTimeRange = timeRanges.find(_._1 == selectedLabel).map(_._2).getOrElse("medium_term")
      val tracks = spotifyClient.getTopTracks(selectedTimeRange)

      tracksList.items = ObservableBuffer(tracks: _*)
    }

    artistsList.setCellFactory { _ => new ArtistCell }
    tracksList.setCellFactory { _ => new TrackCell }

    artistsList.onMouseClicked = mouseEvent => {
      if (mouseEvent.clickCount == 2) {
        val selectedArtist = artistsList.selectionModel().getSelectedItem
        if (selectedArtist != null) {
          selectedArtist.spotifyUrl.foreach { url =>
            if (Desktop.isDesktopSupported) {
              Desktop.getDesktop.browse(new URI(url))
            } else {
              println(s"Open manually: $url")
            }
          }
        }
      }
    }

    tracksList.onMouseClicked = mouseEvent => {
      if (mouseEvent.clickCount == 2) {
        val selectedTrack = tracksList.selectionModel().getSelectedItem
        if (selectedTrack != null) {
          selectedTrack.spotifyUrl.foreach { url =>
            if (Desktop.isDesktopSupported) {
              Desktop.getDesktop.browse(new URI(url))
            } else {
              println(s"Open manually: $url")
            }
          }
        }
      }
    }

    val titleLabel = new Label("Spotify Stats") {
      style = "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1db954;"
    }
    val titleBox = new VBox {
      alignment = Pos.Center
      children = List(titleLabel)
    }
    VBox.setMargin(titleBox, Insets(0, 0, 40, 0))

    stage = new JFXApp3.PrimaryStage {
      title = "Spotify Stats"
      scene = new Scene {
        root = new HBox {
          spacing = 20
          children = List(
            new VBox {
              spacing = 10
              alignment = Pos.Center
              style = "-fx-padding: 20;"
              children = List(
                titleBox,
                timeRangeCombo,
                getArtistsButton,
                getTracksButton
              )
            },
            artistsList,
            tracksList
          )
        }
      }
    }

    val styleSheet = getClass.getResource("/style.css").toExternalForm
    stage.getScene.getStylesheets.add(styleSheet)
  }
}
