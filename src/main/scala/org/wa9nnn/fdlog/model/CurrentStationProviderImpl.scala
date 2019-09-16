
package org.wa9nnn.fdlog.model

import org.wa9nnn.fdlog.model.MessageFormats.CallSign

case class CurrentStation(ourStation: OurStation = OurStation("WA9NNN", "IC-7300", "endfed"),
                           bandMode: BandMode = BandMode(Band("20m"), Mode.digital)){
}

trait CurrentStationProvider {
  /**
   * Currently configured
   *
   * @return
   */
  def currentStation: CurrentStation
  def bandMode:BandMode = currentStation.bandMode
  def ourStation:OurStation = currentStation.ourStation
}

class CurrentStationProviderImpl extends CurrentStationProvider {
  /**
   * Currently configured
   *
   * @return
   */
  override val currentStation: CurrentStation = CurrentStation()
}

case class BandMode(band: Band, mode: Mode)

case class OurStation(operator: CallSign, rig: String = "", antenna: String = "")


