package io.pedro.actors

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.StatusCodes
import io.pedro.messages.{ErrorResponse, TeamMessages}
import io.pedro.messages.TeamMessages.{GetTeams, GetTeam, InsertTeam, DeleteTeam, Team}
import io.pedro.wrappers.MongoWrapper
import org.mongodb.scala.{Completed, Observer}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Updates._


import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import io.pedro.messages.TeamMessages.UpdateTeam
import org.mongodb.scala.Subscription
import com.mongodb.client.result.UpdateResult
import org.bson.conversions.Bson
import org.bson.Document

object TeamActor {
  def props = Props(new TeamActor)
}

class TeamActor extends Actor {

  def receive: PartialFunction[Any, Unit] = {

    // Retorna lista de times
    case GetTeams => {

      val actorToReturn = sender()

      MongoWrapper.teamCollection match {
        case Some(collection) =>
          collection.find().toFuture() onComplete  {
            case Success(teams) => actorToReturn ! TeamMessages.Teams(teams.toList)

            case Failure(_) => actorToReturn ! ErrorResponse(StatusCodes.InternalServerError, "An internal error has occurred. Contact the system administrator.")

          }
        case None => actorToReturn ! ErrorResponse(StatusCodes.InternalServerError, "An internal error has occurred. Contact the system administrator.")
      }
    }

    // Retorna time pelo nome
    case GetTeam(team) => {
      val actorToReturn = sender()

      MongoWrapper.teamCollection match {
        case Some(collection) =>
          collection.find(equal("name", team)).toFuture() onComplete {
            case Success(teamObject) =>  teamObject match {
              case head :: next => actorToReturn ! TeamMessages.Team(teamObject.head.name, teamObject.head.city)
              case _ => actorToReturn ! ErrorResponse(StatusCodes.NotFound, "No Results")
            }

            case Failure(_) => actorToReturn ! ErrorResponse(StatusCodes.InternalServerError, "An internal error has occurred. Contact the system administrator.")

          }
        case None => actorToReturn ! ErrorResponse(StatusCodes.InternalServerError, "An internal error has occurred. Contact the system administrator.")
      }

    }

    // Insere um novo time
    case InsertTeam(team) =>

      val actorToReturn = sender()

      MongoWrapper.teamCollection match {
        case Some(collection) =>
          collection.insertOne(team).subscribe(new Observer[Completed] {

            override def onNext(result: Completed): Unit = println("Inserted",result)

            override def onError(e: Throwable): Unit = println("Failed", e)

            override def onComplete(): Unit = actorToReturn ! TeamMessages.Team(team.name, team.city)
          })
        case None => actorToReturn ! ErrorResponse(StatusCodes.InternalServerError, "An internal error has occurred. Contact the system administrator.")
      }

      // Deleta time pelo nome
      case DeleteTeam(team) =>

      val actorToReturn = sender()

      MongoWrapper.teamCollection match {
        case Some(collection) =>
          collection.findOneAndDelete(equal("name", team)).toFuture() onComplete {
            case Success(value) => actorToReturn ! TeamMessages.SuccessOperation(true)
            case Failure(exception) => actorToReturn ! ErrorResponse(StatusCodes.InternalServerError, "An internal error has occurred. Contact the system administrator.")
          }
        case None => actorToReturn ! ErrorResponse(StatusCodes.InternalServerError, "An internal error has occurred. Contact the system administrator.")
      }

      // Atualiza time
      case UpdateTeam(name, team) => {

      val actorToReturn = sender()
      val newValues = Map("name" -> team.name, "city" -> team.city)

      MongoWrapper.teamCollection match {
        case Some(collection) =>
          collection.updateOne(equal("name", name), new Document("$set", newValues)).subscribe(new Observer[UpdateResult] {
            override def onNext(result: UpdateResult): Unit = println("Inserted",result)

            override def onError(e: Throwable): Unit = println("Failed", e)

            override def onComplete: Unit = actorToReturn ! TeamMessages.Team(team.name, team.city)
          })
        case None => actorToReturn ! ErrorResponse(StatusCodes.InternalServerError, "An internal error has occurred. Contact the system administrator.")
      }
    }
  }


}
