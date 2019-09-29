package io.pedro.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import StatusCodes._
import io.pedro.messages.TeamMessages._
import io.pedro.actors.TeamActor
import io.pedro.messages.{ErrorResponse, Marshaller, Response}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class Api(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout
  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  def createTeamActor(): ActorRef = system.actorOf(TeamActor.props)
}


trait RestRoutes extends TeamApi with Marshaller {

  val service = "api"
  val version = "v1"

  // Rota responsável por trazer todos os times
  protected val getAllTeamsRoute: Route = {
    pathPrefix(service / version / "teams") {
      get {
        // GET api/v1/teams
        pathEndOrSingleSlash {
          onSuccess(getTeams) {
            case error: ErrorResponse => complete(error.code, error.message)
            case teams: Teams => complete(OK, teams)
          }
        }
      }
    }
  }

  // Rota responsável por trazer um time pelo Nome
  protected val getTeamRoute: Route = {
    pathPrefix(service / version / "teams" / Segment) { team =>
      get {
        // GET api/v1/teams/:team
        pathEndOrSingleSlash {
          onSuccess(getTeam(team)) {
            case error: ErrorResponse => complete(error.code, error.message)
            case teamResponse: Team => complete(OK, teamResponse)
          }
        }
      }
    }
  }

  // Rota responsável por inserir um novo time
  protected val insertTeamRoute: Route = {
    pathPrefix(service / version / "teams") {
      post {
        // POST api/v1/teams
        pathEndOrSingleSlash {
          entity(as[Team]) {
            team => {
              onSuccess(insertTeam(team)) {
                case error: ErrorResponse => complete(error.code, error.message)
                case teamResponse: Team => complete(OK, teamResponse)
              }
            }
          }
        }
      }
    }
  }

  // Rota responsável por deletar um time pelo nome
  protected val deleteTeamRoute: Route = {
    pathPrefix(service / version / "teams" / Segment) { team =>
      delete {
        // DELETE api/v1/teams/:team
        pathEndOrSingleSlash {
          onSuccess(deleteTeam(team)) {
            case error: ErrorResponse => complete(error.code, error.message)
            case deleted: SuccessOperation => complete(OK, deleted)
          }
        }
      }
    }
  }

  // Rota responsável por alterar um time
  protected val updateTeamRoute: Route = {
    pathPrefix(service / version / "teams" / Segment) { teamName =>
      put {
        // PUT api/v1/teams/:team
        pathEndOrSingleSlash {
          entity(as[Team]) {
            team => {
              onSuccess(updateTeam(teamName, team)) {
                case error: ErrorResponse => complete(error.code, error.message)
                case teamResponse: Team => complete(OK, teamResponse)
              }
            }
          }
        }
      }
    }
  }

  val routes: Route = getAllTeamsRoute ~ getTeamRoute ~ insertTeamRoute ~ deleteTeamRoute ~ updateTeamRoute

}

trait TeamApi {

  def createTeamActor(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val teamActor: ActorRef = createTeamActor()

  def getTeams: Future[Response] = teamActor.ask(GetTeams).mapTo[Response]

  def getTeam(team: String): Future[Response] = teamActor.ask(GetTeam(team)).mapTo[Response]

  def deleteTeam(team: String): Future[Response] = teamActor.ask(DeleteTeam(team)).mapTo[Response]

  def updateTeam(name: String, team: Team): Future[Response] = teamActor.ask(UpdateTeam(name, team)).mapTo[Response]

  def insertTeam(team: Team): Future[Response] = teamActor.ask(InsertTeam(team)).mapTo[Response]

}
