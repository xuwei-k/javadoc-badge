package javadoc_badge

sealed abstract class JavadocHost extends Product with Serializable

object JavadocHost {
  case object Sonatype extends JavadocHost
  case object JavadocIO extends JavadocHost
}
