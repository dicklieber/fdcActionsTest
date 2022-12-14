/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.model

import akka.util.ByteString
import net.logstash.logback.argument.StructuredArguments.kv
import org.wa9nnn.fdcluster.logging.Loggable
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.sync.StoreMessage
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.Json

import java.time.Instant
import java.util.UUID


/**
 * This is what's in the store and journal.log.
 *
 * @param callSign      of the worked station.
 * @param exchange      from the worked station.
 * @param bandMode      that was used.
 * @param mHz           may not be known if not connectd to a rig
 * @param stamp         when QSO occurred.
 * @param uuid          id unique QSO id in time & space.
 * @param qsoMetadata   info about ur station.
 */
case class Qso(callSign: CallSign, exchange: Exchange, bandMode: BandMode, qsoMetadata: QsoMetadata, mHz: Option[Float] = None, stamp: Instant = Instant.now(), uuid: Uuid = UUID.randomUUID) extends Ordered[Qso] with Loggable {
  def isDup(that: Qso): Boolean = {
    this.callSign == that.callSign &&
      this.bandMode == that.bandMode
  }

  lazy val display: String = s"$callSign on $bandMode in $fdHour"

  override def hashCode: Int = uuid.hashCode()


  override def compare(that: Qso): Int = this.callSign compareTo that.callSign

  lazy val fdHour: FdHour = {
    FdHour(stamp)
  }

  /**
   * @see https://wwrof.org/cabrillo/cabrillo-qso-data/
   * @return frequency for a cab file.
   *         As integer KHz
   */
  def cabFreq: String = {
    mHz.map { mHz =>
      val kHz = mHz * 1000.0F
      f"${kHz}%.0f"
    }.getOrElse(bandMode.cabFreq)
  }


  def toByteString: ByteString = {
    ByteString(Json.toBytes(Json.toJson(this)))
  }

  def toJsonLine: String = {
    Json.toJson(this).toString()
  }

  def toJsonPretty: String = {
    Json.prettyPrint(Json.toJson(this))
  }

  override def log(): Unit = {

    logger.info("newqso {} {} {} {} {} {}",
      kv("callsign", callSign),
      kv("operator", qsoMetadata.operator),
      kv("class", exchange.entryClass),
      kv("section", exchange.sectionCode),
      kv("band", bandMode.bandName),
      kv("mode", bandMode.modeName),
      kv("frequency", mHz),
    )
  }
}

object Qso {
  def apply(callSign: CallSign,
            exchange: Exchange,
            bandMode: BandMode
           )(implicit qsoMetadata: QsoMetadata): Qso = {
    new Qso(callSign = callSign.toUpperCase, exchange = exchange, bandMode = bandMode, qsoMetadata = qsoMetadata)
  }

  def apply(json: String): Qso = {
    Json.parse(json).as[Qso]
  }
}


/**
 * This is what gets multi-casted to cluster.
 *
 * @param qso         the new QSO
 * @param nodeAddress where this came from.
 */
case class DistributedQso(qso: Qso, nodeAddress: NodeAddress) extends StoreMessage with Loggable{
  override def log(): Unit = {
    qso.log()
  }
}





