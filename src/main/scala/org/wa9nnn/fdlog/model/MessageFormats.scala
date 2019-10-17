
package org.wa9nnn.fdlog.model

import java.net.URL
import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.util.Locale

import org.wa9nnn.fdlog.model.sync.{NodeStatus, QsoHour, QsoHourDigest, QsoHourIds}
import org.wa9nnn.fdlog.store.JsonContainer
import org.wa9nnn.fdlog.store.network.FdHour
import play.api.libs.json.{Format, Json}

import scala.language.implicitConversions

/**
 * Creates [[play.api.libs.json.Format]] needed by Play JSon to parse and render JSON for case classes.
 * Usually includes with {{import org.wa9nnn.fdlog.model.MessageFormats._}}
 * Which makes all implicits available when invoking [[Json.parse]] and [[Json.prettyPrint()]] or [[Json.toBytes()]].
 */
object MessageFormats {

  import org.wa9nnn.fdlog.model.UrlFormt.urlFormat
//  import org.wa9nnn.fdlog.store.network.FdHour.fdHourFormat

  implicit val fdHourFormat: Format[FdHour] = Json.format[FdHour]
  implicit val transmitterFormat: Format[OurStation] = Json.format[OurStation]
  implicit val bandModeFormat: Format[BandMode] = Json.format[BandMode]
  implicit val currentStationFormat: Format[CurrentStation] = Json.format[CurrentStation]
  implicit val qsoFormat: Format[Qso] = Json.format[Qso]
  implicit val nodeAddressFormat: Format[NodeAddress] = Json.format[NodeAddress]
  implicit val fdLogIdFormat: Format[FdLogId] = Json.format[FdLogId]
  implicit val qsoRecordFormat: Format[QsoRecord] = Json.format[QsoRecord]
  implicit val distributedQsoRecordFormat: Format[DistributedQsoRecord] = Json.format[DistributedQsoRecord]
  implicit val qsosFormat: Format[QsoHourIds] = Json.format[QsoHourIds]
  implicit val qsoHourDigestFormat: Format[QsoHourDigest] = Json.format[QsoHourDigest]
  implicit val qsoPeriodFormat: Format[QsoHour] = Json.format[QsoHour]
  implicit val nodeStatsFormat: Format[NodeStatus] = Json.format[NodeStatus]
  implicit val jsonContainerFormat: Format[JsonContainer] = Json.format[JsonContainer]
  type CallSign = String
  type Uuid = String
  type Digest = String

}

object TimeFormat {
  implicit def formatLocalDateTime(ldt: LocalDateTime): String = {
    ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
  }


}