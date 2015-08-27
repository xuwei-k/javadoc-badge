package javadoc_badge

import org.joda.time.DateTime
import unfiltered.request._
import unfiltered.response._
import scala.util.control.NonFatal
import scala.xml.XML

final class App extends unfiltered.filter.Plan {

  def intent = {
    case GET(Path(Seg(Nil))) =>
      Ok ~> Html5(
        <p><a href="https://github.com/xuwei-k/javadoc-badge">https://github.com/xuwei-k/javadoc-badge</a></p>
      )
    case GET(Path(Seg(org :: name :: Nil)) & Params(p)) =>
      val label = App.param(p, "label").getOrElse("javadoc")
      val baseUrl = App.param(p, "base").getOrElse("https://oss.sonatype.org/content/repositories/releases/")
      name.split('.').toSeq match {
        case init :+ "svg" =>
          val latest = App.latestVersion(baseUrl, org, init.mkString("."))
          App.NoCacheHeader ~> App.view(latest, label)
        case init :+ "md" =>
          val n = init.mkString(".")
          val base = s"http://javadoc-badge.appspot.com/$org/$n"
          Ok ~> ResponseString(
            s"""[![$label](${base}.svg?label=$label)]($base)"""
          )
        case _ =>
          App.latestVersion(baseUrl, org, name) match {
            case Some(version) =>
              Redirect(App.javadocUrl(org, name, version))
            case None =>
              NotFound ~> ResponseString("not found")
          }
      }
  }

}

final case class SVG(nodes: scala.xml.NodeSeq) extends
  ComposeResponse(CharContentType("image/svg+xml") ~> ResponseString(nodes.toString))

object App {
  private val NoCacheHeader = CacheControl("no-cache,no-store,must-revalidate,private") ~> Pragma("no-cache")

  private def javadocUrl(org: String, name: String, version: String): String =
    s"https://oss.sonatype.org/service/local/repositories/releases/archive/${org.replace('.', '/')}/$name/$version/$name-$version-javadoc.jar/!/index.html"

  private def param(params: Params.Map, key: String): Option[String] =
    params.get(key).toList.flatten.find(_.trim.nonEmpty)

  private def view(latest: Option[String], label: String) =
    latest match {
      case Some(version) =>
        Ok ~> SVG(svg(label, version))
      case None =>
        Ok ~> SVG(notFound(label))
    }

  private final case class CacheKey(baseUrl: String, org: String, name: String)

  private[this] val cache = Cache.create[CacheKey, String](1024)

  private def latestVersion(baseUrl: String, org: String, name: String): Option[String] = {
    val key = CacheKey(baseUrl, org, name)
    cache.getOrElseUpdate(key, latestVersion0(baseUrl, org, name), DateTime.now.plusMinutes(10))
  }

  private[this] def latestVersion0(baseUrl: String, org: String, name: String): Option[String] =
    try {
      val url = s"$baseUrl/${org.replace('.', '/')}/$name/maven-metadata.xml"
      (XML.load(url) \ "versioning" \ "latest").headOption.map(_.text)
    }catch{
      case _: _root_.org.xml.sax.SAXParseException => // ignore
        None
      case NonFatal(e) =>
        e.printStackTrace()
        None
    }

  // TODO calculate the width from character count
  private def svg(label: String, version: String) = {
    <svg xmlns="http://www.w3.org/2000/svg" width="131" height="20">
      <linearGradient id="a" x2="0" y2="100%">
        <stop offset="0" stop-color="#fff" stop-opacity=".7"/>
        <stop offset=".1" stop-color="#aaa" stop-opacity=".1"/>
        <stop offset=".9" stop-opacity=".3"/>
        <stop offset="1" stop-opacity=".5"/>
      </linearGradient>
      <rect rx="4" width="131" height="20" fill="#555"/>
      <rect rx="4" x="92" width="39" height="20" fill="#4c1"/>
      <path fill="#4c1" d="M92 0h4v18h-4z"/>
      <rect rx="4" width="131" height="20" fill="url(#a)"/>
      <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
        <text x="47" y="14" fill="#010101" fill-opacity=".3">{label}</text>
        <text x="47" y="13">{label}</text>
        <text x="110.5" y="14" fill="#010101" fill-opacity=".3">{version}</text>
        <text x="110.5" y="13">{version}</text>
      </g>
    </svg>
  }

  private def notFound(label: String) =
    <svg xmlns="http://www.w3.org/2000/svg" width="153" height="20">
      <linearGradient id="a" x2="0" y2="100%">
        <stop offset="0" stop-color="#fff" stop-opacity=".7"/>
        <stop offset=".1" stop-color="#aaa" stop-opacity=".1"/>
        <stop offset=".9" stop-opacity=".3"/>
        <stop offset="1" stop-opacity=".5"/>
      </linearGradient>
      <rect rx="4" width="153" height="20" fill="#555"/>
      <rect rx="4" x="92" width="61" height="20" fill="#9f9f9f"/>
      <path fill="#9f9f9f" d="M92 0h4v18h-4z"/>
      <rect rx="4" width="153" height="20" fill="url(#a)"/>
      <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
        <text x="47" y="14" fill="#010101" fill-opacity=".3">{label}</text>
        <text x="47" y="13">{label}</text>
        <text x="121.5" y="14" fill="#010101" fill-opacity=".3">unknown</text>
        <text x="121.5" y="13">unknown</text>
      </g>
    </svg>

}
