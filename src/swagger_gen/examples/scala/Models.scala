package com.google.service.models

import spray.json.DefaultJsonProtocol

case object Pet

object Pet extends DefaultJsonProtocol {
  implicit val format = jsonFormat0(Pet.apply)
}

case class NewPet(name: String, tag: String)

object NewPet extends DefaultJsonProtocol {
  implicit val format = jsonFormat2(NewPet.apply)
}

case class Error(code: Int, message: String)

object Error extends DefaultJsonProtocol {
  implicit val format = jsonFormat2(Error.apply)
}
