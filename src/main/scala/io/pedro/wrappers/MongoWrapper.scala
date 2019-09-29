package io.pedro.wrappers

import akka.event.slf4j.Logger
import io.pedro.messages.TeamMessages.Team
import io.pedro.util.ConfigUtil
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.{MongoClient, MongoCollection}
import org.mongodb.scala.bson.codecs.{DEFAULT_CODEC_REGISTRY, Macros}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success, Try}

object MongoWrapper {

  private val log = Logger(this.getClass.getCanonicalName)

  // Codecs
  private val teamCodecProvider = Macros.createCodecProvider[Team]()

  // Registro de codecs
  private val codecRegistry = fromRegistries( fromProviders(teamCodecProvider), DEFAULT_CODEC_REGISTRY )

  private val client: Try[MongoClient] = ConfigUtil.getMongoDBUri match {
    case Some(uri) => Try(MongoClient(uri.toString))
    case None => Failure(new IllegalArgumentException("The MongoDB connection config is wrong."))
  }

  val teamCollection: Option[MongoCollection[Team]] = {

    client match {

      case Success(_client) =>

        (ConfigUtil.getMongoDatabase, ConfigUtil.getMongoCollection) match {
          case (Some(databaseName), Some(collectionName)) =>

            val database = _client.getDatabase(databaseName).withCodecRegistry(codecRegistry)
            Some(database.getCollection(collectionName))

          case _ => None
        }

      case Failure(ex) =>
        log.error("The MongoDB client is down.", ex)
        None
    }

  }

}
