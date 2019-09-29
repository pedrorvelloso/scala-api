package io.pedro.util

import java.io.FileNotFoundException
import scala.util.Try

object FileUtil {

  def getTeamsCsv: Try[List[String]] = {

    Try {

      ConfigUtil.getTeamsCsvPath match {

        case Some(path) =>
          val cursor =  scala.io.Source.fromFile(path)
          val data = cursor.getLines().toList

          cursor.close()

          data.tail

        case None => throw new FileNotFoundException(s"The TeamsCsvPath was not found.")

      }
    }
  }





}
