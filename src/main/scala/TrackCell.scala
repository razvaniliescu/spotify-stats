import scalafx.scene.control.{ListCell, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.HBox
import scalafx.geometry.Pos

class TrackCell extends ListCell[Track] {
  item.onChange { (_, _, newTrack) =>
    if (newTrack != null) {
      val img = newTrack.imageUrl match {
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

      val nameLabel = new Label(s"${newTrack.artist} - ${newTrack.name}")

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
