import io.circe.*
import io.circe.generic.semiauto.*

case class Artist(name: String, imageUrl: Option[String], spotifyUrl: Option[String])
case class Track(name: String, artist: String, imageUrl: Option[String], spotifyUrl: Option[String])

object JsonParser {
  implicit val artistDecoder: Decoder[Artist] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    images <- c.downField("images").as[Vector[Json]]
    imageUrl = images.headOption.flatMap(_.hcursor.get[String]("url").toOption)
    spotifyUrl <- c.downField("external_urls").downField("spotify").as[Option[String]]
  } yield Artist(name, imageUrl, spotifyUrl)

  implicit val trackDecoder: Decoder[Track] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    artists <- c.downField("artists").as[List[Json]]
    firstArtist = artists.headOption.flatMap(_.hcursor.get[String]("name").toOption).getOrElse("Unknown Artist")
    images <- c.downField("album").downField("images").as[Vector[Json]]
    imageUrl = images.headOption.flatMap(_.hcursor.get[String]("url").toOption)
    spotifyUrl <- c.downField("external_urls").downField("spotify").as[Option[String]]
  } yield Track(name, firstArtist, imageUrl, spotifyUrl)

  def parseArtist(json: String): List[Artist] = {
    io.circe.parser.parse(json) match {
      case Left(error) => throw new Exception("Invalid JSON: " + error)
      case Right(json) =>
        val cursor = json.hcursor
        cursor.downField("items").as[List[Artist]].getOrElse(List())
    }
  }

  def parseTrack(json: String): List[Track] = {
    io.circe.parser.parse(json) match {
      case Left(error) => throw new Exception("Invalid JSON: " + error)
      case Right(json) =>
        val cursor = json.hcursor
        cursor.downField("items").as[List[Track]].getOrElse(List())
    }
  }
}
