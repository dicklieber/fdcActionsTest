/*
 * Copyright (C) 2017  Dick Lieber, WA9NNN
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wa9nnn.fdlog.util

import java.time.{Instant, ZonedDateTime}

import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue}

/**
  * Provides an slf4j [[Logger]]
  * By default uses the java class name as the LoggerName. Can be changed by invoking [[.loggerName]] before using any logger.
  * Can produce a [[LogJson]] that eaisly generates JSON log messages that are very friendly to  [[https://www.elastic.co LogStash and the ELK stack.]]
  */
trait StructuredLogging {

  /**
    * Change logger name from default package/class name to something better.
    * For example "AreaActor" can be more stable than "com.here.hdradio.servicearea.AreaActor" that might change with a refactoring.
    *
    * @param loggerName to be used instead of getclass.getname.
    * @throws IllegalStateException if invoked after access to [[Logger]] or if invoked more than once.
    */
  def loggerName(loggerName: String): Unit = {
    if (_loggerName.nonEmpty) {
      throw new IllegalStateException(s"LoggerName is already set for ${getClass.getName}!")
    }
    else {
      _loggerName = Some(loggerName)
    }
  }

  private var _loggerName: Option[String] = None

  /**
    * An slf4j logger.
    */
  lazy val logger = {
    if (_loggerName.isEmpty) {
      _loggerName = Some(getClass.getName)
    }
    LoggerFactory.getLogger(_loggerName.get)
  }



  /**
    * @param reason value for the reason field.
    * @return see [[LogJson]]
    */
  def logJson(reason: String): LogJson = {
    LogJson(logger)
      .field("reason", reason)
  }
}



/**
  * Convenience class to build structured, json, log messages.
  *
  * Instances of [[LogJson]] are usually created by invoking [[StructuredLogging.logJson]]
  * {{{
  *     logJson("statusChange")
  * .field("ServiceArea", serviceAreaKey)
  * .field("hdType", importerMessage.messageType)
  * .field("hdTid", importerMessage.hdTid)
  * .field("chunks", importerMessage.body.size)
  * .field("bytes", byteCount)
  * .field("origFile", importerMessage.productDestination.getFileName)
  * .field("origSize", importerMessage.originalSize)
  * .info()
  * }}}
  *
  * It's often helpful to use a curry-like idiom:
  * {{{
  * val logServiceAreaJson = logJson.field("ServiceArea", serviceAreaKey)
  * ...
  * logServiceAreaJson
  * .field("hdType", importerMessage.messageType)
  * .field("hdTid", importerMessage.hdTid)
  * .field("chunks", importerMessage.body.size)
  * .field("bytes", byteCount)
  * .field("origFile", importerMessage.productDestination.getFileName)
  * .field("origSize", importerMessage.originalSize)
  * .info()
  * }}}
  *
  * @param logger that will be used when one of the info, debug etc. methods are called.
  * @param fields all the fields added with [[.field]]
  */
class LogJson(logger: Logger, val fields: Seq[(String, JsValue)]) {

  def field(label: String, value: Any): LogJson = {
    new LogJson(logger, fields :+ LogJson.proc(label, value))
  }
  /**
    * Allows adding bulk fields.
    * @param newFields to be added to the [[LogJson]]
    * @return
    */
  def ++(newFields: Seq[(String, Any)]): LogJson = {
    val finalFields = newFields.foldLeft(fields) { case (assum, (label, value)) ⇒
      assum :+ LogJson.proc(label, value)
    }
    new LogJson(logger, finalFields)
  }

  def info(): Unit = logger.info(render)

  def debug(): Unit = logger.debug(render)

  def trace(): Unit = logger.trace(render)

  def error(): Unit = logger.error(render)

  def error(cause: Throwable): Unit = logger.error(render, cause)

  def warn(): Unit = logger.warn(render)

  def render: String = {
    val map: Seq[(String, JsValue)] = fields.map(t ⇒ t)
    val jsObject: JsObject = JsObject(map)
    jsObject.toString()
  }

}

object LogJson {

  def apply(logger: Logger): LogJson = {
    new LogJson(logger, Seq.empty)
  }

  def proc(label: String, value: Any): (String, JsValue) = {
    val jsValue = value match {
      case v: String ⇒ JsString(v)
      case v: Instant ⇒ JsString(v.toString)
      case v: ZonedDateTime ⇒ JsString(v.toString)
      case v: Int ⇒ JsNumber(v)
      case v: Long ⇒ JsNumber(v)
      case v: Double ⇒ JsNumber(v)
      case v: Float ⇒ JsNumber(v.toDouble)
      case v: JsObject ⇒ v
      case x ⇒ JsString(Option(x).fold("null")(_.toString))
    }
    (label, jsValue)
  }
}
