package io.pedro.util

import com.typesafe.config.ConfigFactory

object ConfigUtil {

  private val config = ConfigFactory.load()

  def getHttpServerHost: Option[String] = Option(config.getString("http.host"))

  def getHttpServerPort: Option[Int] = Option(config.getInt("http.port"))

  def getRequestTimeout: Option[String] = Option(config.getString("akka.http.server.request-timeout"))

  def getTeamsCsvPath: Option[String] = Option(config.getString("appConfig.teamsCsvPath"))

  def getMongoDatabase: Option[String] = Option(config.getString("mongoConfig.database"))

  def getMongoCollection: Option[String] = Option(config.getString("mongoConfig.collection"))

  def getMongoDBUri: Option[String] = {
    (
      Option(config.getString("mongoConfig.host")),
      Option(config.getInt("mongoConfig.port")),
      Option(config.getString("mongoConfig.user")),
      Option(config.getString("mongoConfig.password"))

    ) match {
      case (Some(host), Some(port), Some(user), Some(password)) => {
        Some(s"mongodb://$user:$password@$host:$port/?authMechanism=SCRAM-SHA-1")
      }
      case _ => None
    }
  }

}
