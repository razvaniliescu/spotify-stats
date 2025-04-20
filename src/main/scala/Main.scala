import java.awt.Desktop
import java.net.URI
import scalafx.application.{JFXApp3, Platform}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ComboBox, Label, ListCell, ListView}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.collections.ObservableBuffer
import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.animation.{FadeTransition, TranslateTransition}
import scalafx.geometry.{Insets, Pos}
import scalafx.util.Duration
import scalafx.scene.SceneIncludes.jfxNode2sfx
import scalafx.scene.text.TextAlignment.Center

object Main extends JFXApp3 {
  private var spotifyClient: SpotifyClient = _

  override def start(): Unit = {
    val clientId = sys.env.getOrElse("SPOTIFY_CLIENT_ID", "MISSING_CLIENT_ID")
    val clientSecret = sys.env.getOrElse("SPOTIFY_CLIENT_SECRET", "MISSING_CLIENT_SECRET")
    val redirectUri = "http://127.0.0.1:8080/callback"

    val auth = new Auth(clientId, clientSecret)

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

    val artistsList = new ListView[Artist]()
    artistsList.prefWidth = 375
    val tracksList = new ListView[Track]()
    tracksList.prefWidth = 375

    val timeRanges = Seq(
      "Last 4 Weeks" -> "short_term",
      "Last 6 Months" -> "medium_term",
      "All Time" -> "long_term"
    )

    val timeRangeCombo = new ComboBox[String](timeRanges.map(_._1)) {
      value = timeRanges.head._1
    }

    timeRangeCombo.showing.onChange { (_, _, isShowing) =>
      if (isShowing) {
        val popup = timeRangeCombo.skin().asInstanceOf[javafx.scene.control.skin.ComboBoxListViewSkin[_]].getPopupContent

        val fade = new FadeTransition(Duration(200), popup) {
          fromValue = 0.0
          toValue = 1.0
        }
        val slide = new TranslateTransition(Duration(200), popup) {
          fromY = -10
          toY = 0
        }

        fade.play()
        slide.play()
      }
    }

    val getArtistsButton = new Button("Get Top Artists")
    val getTracksButton = new Button("Get Top Tracks")

    def setupButtonAction[T](button: Button, listView: ListView[T], fetchItems: String => Seq[T]): Unit = {
      button.onAction = _ => {
        val selectedLabel = timeRangeCombo.value.value
        val selectedTimeRange = timeRanges.find(_._1 == selectedLabel).map(_._2).getOrElse("medium_term")
        val items = fetchItems(selectedTimeRange)
        listView.items = ObservableBuffer(items: _*)
      }
    }

    setupButtonAction(getArtistsButton, artistsList, spotifyClient.getTopArtists)
    setupButtonAction(getTracksButton, tracksList, spotifyClient.getTopTracks)

    artistsList.setCellFactory { _ =>
      new Cell[Artist](
        getImageUrl = _.imageUrl,
        getText = _.name
      )
    }

    tracksList.setCellFactory { _ =>
      new Cell[Track](
        getImageUrl = _.imageUrl,
        getText = track => s"${track.artist} - ${track.name}"
      )
    }

    def setupListViewOpen[T](listView: ListView[T], getUrl: T => Option[String]): Unit = {
      listView.onMouseClicked = mouseEvent => {
        if (mouseEvent.clickCount == 2) {
          val selectedItem = listView.selectionModel().getSelectedItem
          if (selectedItem != null) {
            getUrl(selectedItem).foreach { url =>
              if (Desktop.isDesktopSupported) {
                Desktop.getDesktop.browse(new URI(url))
              } else {
                println(s"Open manually: $url")
              }
            }
          }
        }
      }
    }

    setupListViewOpen(artistsList, (artist: Artist) => artist.spotifyUrl)
    setupListViewOpen(tracksList, (track: Track) => track.spotifyUrl)

    val titleBox = new VBox {
      alignment = Pos.Center
      children = List(new Label("Spotify Stats") {
        style = "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1db954;"
      })
    }

    val instructionBox = new VBox {
      alignment = Pos.Center
      children = List(new Label("Double click an item to see the Spotify Page!") {
        style = "-fx-font-size: 14px; -fx-text-fill: #1db954;"
        textAlignment = Center
        wrapText = true
        maxWidth = 200
      })
    }
    VBox.setMargin(instructionBox, Insets(0, 0, 80, 0))

    val quitBox: VBox = new VBox {
      alignment = Pos.Center
      children = List(new Button("Quit") {
        onAction = _ => {
          Platform.exit()
        }})
    }
    VBox.setMargin(quitBox, Insets(80, 0, 0, 0))

    stage = new JFXApp3.PrimaryStage {
      title = "Spotify Stats"
      width = 1050
      height = 660
      resizable = false
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
                instructionBox,
                timeRangeCombo,
                getArtistsButton,
                getTracksButton,
                quitBox
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
