package io.pedro.messages

import play.api.libs.json._
import de.heikoseeberger.akkahttpplayjson._
import io.pedro.messages.TeamMessages.{Team, Teams}
import io.pedro.messages.TeamMessages.SuccessOperation

case class Error(message: String)

trait Marshaller extends PlayJsonSupport {

  implicit val errorFormat: OFormat[Error] = Json.format[Error]
  implicit val teamFormat: OFormat[Team]   = Json.format[Team]
  implicit val teamsFormat: OFormat[Teams] = Json.format[Teams]
  implicit val OperationsFormat: OFormat[SuccessOperation] = Json.format[SuccessOperation]

}
