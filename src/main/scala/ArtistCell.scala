import scalafx.scene.control.{ListCell, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.HBox
import scalafx.geometry.Pos

class ArtistCell extends ListCell[Artist] {
  item.onChange { (_, _, newArtist) =>
    if (newArtist != null) {
      val img = newArtist.imageUrl match {
        case Some(url) =>
          new ImageView(new Image(url, 50, 50, false, true)) {
            fitWidth = 50
            fitHeight = 50
            preserveRatio = false
            smooth = true
          }
        case None =>
          new ImageView(new Image("default-image-url.jpg", 50, 50, false, true)) {
            fitWidth = 50
            fitHeight = 50
            preserveRatio = false
            smooth = true
          }
      }

      val nameLabel = new Label(newArtist.name)

      val hbox = new HBox {
        spacing = 10
        children = Seq(img, nameLabel)
        alignment = Pos.CenterLeft
      }

      graphic = hbox
    } else {
      text = null
      graphic = null
    }
  }
}
