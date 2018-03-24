package com.salcedo.rapbot.serialization

import com.google.gson.Gson

object JSON {
  private val json = new Gson

  def read[T](serialized: String, classOf: Class[T]): T = {
    json.fromJson(serialized, classOf)
  }

  def write(serializable: AnyRef): String = {
    json.toJson(serializable)
  }
}
