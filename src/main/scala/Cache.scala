package javadoc_badge

import java.util.{Map => JMap}
import javadoc_badge.Cache.Entry
import java.time.LocalDateTime

object Cache {

  private final class Entry[A](val value: A, expiresAt: LocalDateTime){
    def expired(): Boolean = expiresAt.isBefore(LocalDateTime.now)
    def notExpired(): Boolean = ! expired
  }

  def create[A, B](maxSize: Int): Cache[A, B] =
    new Cache[A, B](
      java.util.Collections.synchronizedMap(new java.util.LinkedHashMap[A, Entry[B]](maxSize + 1, 1.0f, false){
        override def removeEldestEntry(eldest: JMap.Entry[A, Entry[B]]) = {
          size() > maxSize || eldest.getValue.expired
        }
      })
    )
}

final class Cache[A, B] private(underlying: JMap[A, Entry[B]]) {

  def get(key: A): Option[B] = {
    underlying.get(key) match {
      case null => None
      case entry =>
        if(entry.expired()){
          underlying.remove(key)
          None
        }else{
          Some(entry.value)
        }
    }
  }

  def put(key: A, value: B, expire: LocalDateTime): Boolean = {
    if(expire.isAfter(LocalDateTime.now())){
      val entry = new Entry(value, expire)
      underlying.put(key, entry)
      true
    } else {
      false
    }
  }

  def getOrElseUpdate(key: A, orElse: => Option[B], expire: LocalDateTime): Option[B] = {
    get(key) match {
      case None =>
        orElse match {
          case result @ Some(value) =>
            if(put(key, value, expire)){
              result
            }else{
              None
            }
          case other => other
        }
      case entry =>
        entry
    }
  }

}
