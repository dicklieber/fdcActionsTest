
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

import org.wa9nnn.fdcluster.BuildInfo
import org.wa9nnn.fdcluster.contest.JournalProperty
import org.wa9nnn.fdcluster.model.MessageFormats.CallSign
import scalafx.beans.binding.Bindings
import scalafx.beans.property.ObjectProperty

import java.time.Instant
import javax.inject.{Inject, Singleton}


/**
 *
 * @param operator  who is using app. callSign
 * @param rig       free form usually transceiver model.
 * @param ant       free form antenna description.
 * @param node      what node, in the cluster this came from.
 * @param contestId so old data can't accident be missed with current.
 * @param v         FDCLuster Version that built this so we can detect mismatched versions.
 */
case class QsoMetadata(operator: CallSign = "",
                       rig: String = "",
                       ant: String = "",
                       node: String = "localhost;1",
                       journal: String = "",
                       v: String = BuildInfo.canonicalVersion){
  def forStation(station: Station):QsoMetadata = {
    copy(operator =  station.operator, rig= station.rig, ant = station.antenna)
  }
}

@Singleton
class OsoMetadataProperty @Inject()(stationProperty: StationProperty, contestProperty: ContestProperty, nodeAddress: NodeAddress, journalProperty: JournalProperty)
  extends ObjectProperty[QsoMetadata](null, "StationTable") with QsoBuilder{


  def set(): QsoMetadata = {
    val station: Station = stationProperty.value
    QsoMetadata(
      operator = station.operator,
      rig = station.rig,
      ant = station.antenna,
      node = nodeAddress.displayWithIp,
      journal = journalProperty.value.journalFileName
    )
  }

  value = set()
  private val b = Bindings.createObjectBinding[QsoMetadata](
    () => set(),
    stationProperty, contestProperty
  )
  this <== b

   def qso(callSign: CallSign, exchange: Exchange, bandMode: BandMode, stamp:Instant = Instant.now): Qso = {
     Qso(callSign = callSign.toUpperCase, exchange = exchange, bandMode = bandMode, qsoMetadata = value, stamp = stamp)
   }

  def qso(callSign: CallSign, exchange: Exchange, station: Station) :Qso = {
    Qso(callSign = callSign.toUpperCase, exchange = exchange, bandMode = station.bandMode, qsoMetadata = value.forStation(station))
  }
}

trait QsoBuilder{
  def qso(callSign: CallSign, exchange: Exchange, bandMode: BandMode, stamp:Instant = Instant.now): Qso
}