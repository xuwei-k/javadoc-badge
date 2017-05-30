package javadoc_badge

import org.joda.time.DateTime
import unfiltered.request._
import unfiltered.response._

import scala.util.control.NonFatal
import scala.xml.{Elem, XML}
import scalaz.{-\/, \/-}

final class App extends unfiltered.filter.Plan {

  def intent = {
    case GET(Path(Seg(Nil))) =>
      Ok ~> Html5(
        <p><a href="https://github.com/xuwei-k/javadoc-badge">https://github.com/xuwei-k/javadoc-badge</a></p>
      )
    case GET(Path(Seg(org :: name :: path)) & Params(p)) =>
      val label = App.param(p, "label").getOrElse("javadoc")
      val baseUrl = App.param(p, "base").getOrElse("https://oss.sonatype.org/content/repositories/releases/")
      name.split('.').toSeq match {
        case init :+ "svg" =>
          val classic = App.param(p, "style").map(_ == "classic").getOrElse(false)
          val latest = App.latestVersion(baseUrl, org, init.mkString("."))
          App.NoCacheHeader ~> App.view(latest, label, classic)
        case init :+ "md" =>
          val n = init.mkString(".")
          val base = s"${App.JAVADOC_BADGE_URL}$org/$n"
          val redirect = if(path == Nil) {
            base
          } else {
            base + "/" + path.mkString("/")
          }
          Ok ~> ResponseString(
            s"""[![$label](${base}.svg?label=$label)]($redirect)"""
          )
        case _ =>
          App.latestVersion(baseUrl, org, name) match {
            case Some(version) =>
              val path0 = if(path == Nil) "index.html" else path.mkString("/")
              val sonatypeURL = App.javadocUrl(org, name, version, path0, JavadocHost.Sonatype)
              if(App.param(p, "javadocio").isDefined) {
                val javadocIO = App.javadocUrl(org, name, version, path0, JavadocHost.JavadocIO)
                val status = httpz.native.Http(javadocIO).asCodeHeaders._1
                println(s"status = $status. $javadocIO")
                if(status / 100 == 2) {
                  Redirect(javadocIO)
                } else {
                  try {
                    httpz.native.Http(s"https://javadoc.io/doc/${org}/${name}/${version}").asCodeHeaders
                  } catch {
                    case NonFatal(_) => // ignore
                  }
                  Redirect(sonatypeURL)
                }
              } else {
                Redirect(sonatypeURL)
              }
            case None =>
              NotFound ~> ResponseString("not found")
          }
      }
    case GET(Path(Seg(org :: Nil))) =>
      MavenSearch.searchByGroupId(org) match {
        case \/-(Nil) =>
          App.returnHtml(<p>{"Not found groupId=" + org}</p>) ~> NotFound
        case \/-(list) =>
          App.returnHtml(
            <div>{
              list.map{ name =>
                <li><a href={s"${App.JAVADOC_BADGE_URL}$org/${name}.md"}>{name}</a></li>
              }
            }</div>
          )
        case -\/(error) =>
          App.returnHtml(<p>{error}</p>)
      }
  }

}

final case class SVG(nodes: scala.xml.NodeSeq) extends
  ComposeResponse(CharContentType("image/svg+xml") ~> ResponseString(nodes.toString))

object App {
  private val JAVADOC_BADGE_URL = "http://javadoc-badge.appspot.com/"

  private val NoCacheHeader = CacheControl("no-cache,no-store,must-revalidate,private") ~> Pragma("no-cache")

  private def javadocUrl(org: String, name: String, version: String, path: String, host: JavadocHost): String = {
    host match {
      case JavadocHost.Sonatype =>
        s"https://oss.sonatype.org/service/local/repositories/releases/archive/${org.replace('.', '/')}/$name/$version/$name-$version-javadoc.jar/!/$path"
      case JavadocHost.JavadocIO =>
        s"https://static.javadoc.io/${org}/${name}/${version}/$path"
    }
  }

  private def param(params: Params.Map, key: String): Option[String] =
    params.get(key).toList.flatten.find(_.trim.nonEmpty)

  private def view(latest: Option[String], label: String, classic: Boolean) =
    latest match {
      case Some(version) =>
        Ok ~> SVG(svg(label, version, classic))
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

  private def svg(label: String, version: String, classic: Boolean) = {
    val width1 = (label.length * 8)
    val width2 = ((version.length + 1) * 8)
    val width = width1 + width2
    val w = width.toString
    val n1 = (width1 / 2).toString
    val n2 = (width1 + (width2 / 2)).toString

    if(classic) {
      <svg xmlns="http://www.w3.org/2000/svg" width={w} height="20">
        <linearGradient id="a" x2="0" y2="100%">
          <stop offset="0" stop-color="#fff" stop-opacity=".7"/>
          <stop offset=".1" stop-color="#aaa" stop-opacity=".1"/>
          <stop offset=".9" stop-opacity=".3"/>
          <stop offset="1" stop-opacity=".5"/>
        </linearGradient>
        <rect rx="4" width={w} height="20" fill="#555"/>
        <rect rx="4" x={width1.toString} width={width2.toString} height="20" fill="#4c1"/>
        <path fill="#4c1" d={"M" + width1.toString + " 0h4v18h-4z"} />
        <rect rx="4" width={w} height="20" fill="url(#a)"/>
        <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
          <text x={n1} y="14" fill="#010101" fill-opacity=".3">{label}</text>
          <text x={n1} y="13">{label}</text>
          <text x={n2} y="14" fill="#010101" fill-opacity=".3">{version}</text>
          <text x={n2} y="13">{version}</text>
        </g>
      </svg>
    } else {
      <svg xmlns="http://www.w3.org/2000/svg" width={w} height="20">
        <linearGradient id="b" x2="0" y2="100%">
          <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
          <stop offset="1" stop-opacity=".1"/>
        </linearGradient>
        <mask id="a">
          <rect width={w} height="20" rx="3" fill="#fff"/>
        </mask>
        <g mask="url(#a)">
          <path fill="#555" d={"M0 0h" + width1 + "v20H0z"}/>
          <path fill="#4c1" d={"M" + width1 + " 0h" + width2 + "v20H" + width1 + "z"}/>
          <path fill="url(#b)" d="M0 0h161v20H0z"/>
        </g>
        <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
          <text x={n1} y="15" fill="#010101" fill-opacity=".3">{label}</text>
          <text x={n1} y="14">{label}</text>
          <text x={n2} y="15" fill="#010101" fill-opacity=".3">{version}</text>
          <text x={n2} y="14">{version}</text>
        </g>
      </svg>
    }
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

  private def returnHtml(x: Elem) = Html5(
    <html>
      <head>
        <meta name="robots" content="noindex,nofollow" />
      </head>
      <body><div>{x}</div></body>
    </html>
  )

}
