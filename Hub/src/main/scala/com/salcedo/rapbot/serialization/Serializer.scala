package com.salcedo.rapbot.serialization

trait Serializer {
  def read[T](serialized: String, classOf: Class[T]): T

  def write(serializable: AnyRef): String
}
