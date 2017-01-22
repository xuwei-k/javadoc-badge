package javadoc_badge

import argonaut.CodecJson
import httpz.JsonToString
import scalaz.\/

final case class MavenSearch(response: MavenSearch.Response) extends JsonToString[MavenSearch]

object MavenSearch{
  implicit val codecJson: CodecJson[MavenSearch] =
    CodecJson.casecodec1(apply, unapply)(
      "response"
    )

  def searchByGroupId(groupId: String): httpz.Error \/ List[String] = {
    import httpz._
    import httpz.native._

    val req = Request(
      url = "http://search.maven.org/solrsearch/select",
      params = Map(
        "q" -> s"g:$groupId",
        "rows" -> "256",
        "wt" -> "json"
      )
    )

    Core.json[MavenSearch](req).interpret.map(
      _.response.docs.withFilter(_.hasJavadocJar).map(_.artifactId).sorted
    )
  }

  final case class Doc (
    artifactId: String,
    text: List[String]
  ) extends JsonToString[Doc] {
    def hasJavadocJar: Boolean = text.contains("-javadoc.jar")
  }

  object Doc {
    implicit val codecJson: CodecJson[Doc] =
      CodecJson.casecodec2(apply, unapply)(
        "a",
        "text"
      )
  }

  final case class Response (
    docs: List[Doc]
  ) extends JsonToString[Response]

  object Response {
    implicit val responseCodecJson: CodecJson[Response] =
      CodecJson.casecodec1(apply, unapply)(
        "docs"
      )
  }

}
