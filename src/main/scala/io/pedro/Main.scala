package io.pedro

import java.util.concurrent.TimeUnit

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.pedro.routes.Api
import io.pedro.util.ConfigUtil

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object Main extends App with RequestTimeout {

  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer.create(system)

  val api = new Api(system, requestTimeout()).routes

  val log = Logging(system.eventStream, "api")


  (ConfigUtil.getHttpServerHost, ConfigUtil.getHttpServerPort) match {
    case (Some(host), Some(port)) =>

      val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(api, host, port)

      Try {
        bindingFuture.map { serverBinding =>
          log.info(s"Api bound to ${serverBinding.localAddress}")
        }

      }
    case _ => Failure(new IllegalArgumentException("The HTTP config is wrong."))
  }

}

trait RequestTimeout {

  def requestTimeout(): Timeout = {
    ConfigUtil.getRequestTimeout match {
      case Some(reqTimeout) =>

        val d = Duration(reqTimeout)
        FiniteDuration(d.length, d.unit)

      case None => FiniteDuration(3000, TimeUnit.MILLISECONDS)
    }
  }
}


// DESAFIO
// Finalizar o servidor REST. Implementar as seguintes funcionalidades
//  1) Metodo para adicionar um novo time no MongoDB
//  2) Método para recuperar um time pelo nome
//  3) Método para deletar um time
//  4) Método para atualizar um time