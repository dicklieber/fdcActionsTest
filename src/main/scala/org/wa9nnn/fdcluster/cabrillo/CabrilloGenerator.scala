
package org.wa9nnn.fdcluster.cabrillo


import com.google.inject.name.Named
import com.wa9nnn.cabrillo.model.{SimpleTagValue, TagValue}
import com.wa9nnn.cabrillo.{Builder, CabrilloWriter}
import org.wa9nnn.fdcluster.BuildInfo
import org.wa9nnn.fdcluster.cabrillo.QsoFD.cabrilloDtFormat
import org.wa9nnn.fdcluster.javafx.entry.RunningTaskInfoConsumer
import org.wa9nnn.fdcluster.javafx.runningtask.RunningTask
import org.wa9nnn.fdcluster.model.{Contest, OurStation, OurStationStore, QsoRecord, Qso => fdQso}
import org.wa9nnn.util.TimeHelpers
import scalafx.collections.ObservableBuffer

import java.io.PrintWriter
import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.util.{Failure, Success, Using}

class CabrilloGenerator @Inject()(@Named("allQsos") allQsos: ObservableBuffer[QsoRecord],
                                  val runningTaskInfoConsumer: RunningTaskInfoConsumer,
                                  ourStationStore: OurStationStore,
                                  contest: Contest) extends RunningTask {
  override def taskName: String = "Cabrillo Generator"

  implicit val ourStation: OurStation = ourStationStore.value

  def apply(cabrilloExportRequest: CabrilloExportRequest): Unit = {
    val builder = new Builder()
    builder + ("CREATED-BY", s"${BuildInfo.name} ${BuildInfo.version}")
    builder + ("CALLSIGN", ourStation.ourCallsign)
    builder + ("CONTEST", contest.event)
//    builder + ("CATEGORY-ASSISTED", "NON-ASSISTED")
//    builder + ("CATEGORY-BAND", "ALL")
//    builder + ("CATEGORY-OPERATOR", "SINGLE-OP")
//    builder + ("CATEGORY-POWER", "LOW-OP")
//    builder + ("CATEGORY-STATION", "FIXED")
//    builder + ("CATEGORY-TIME", "24-HOURS")
//    builder + ("CATEGORY-TRANSMITTER", "ONE")
//    builder + ("CATEGORY-OVERLAY", "OVER-50")
//    builder + ("CATEGORY-OVERLAY", "OVER-50")
//    builder + ("CLAIMED-SCORE", "OVER-50") //todo
//    builder + ("CLUB", "WM9W") //todo
//    builder + ("EMAIL", "WM9W@u505.com") //todo
//    builder + ("LOCATION", ourStation.exchange.section) //todo
//    builder + ("NAME", "Dick") //todo
//    builder + ("ADDRESS", "3940 N. Ridgeway Ave") //todo
//    builder + ("ADDRESS-CITY", "Chicago") //todo
//    builder + ("ADDRESS-STATE-PROVINCE", "IL") //todo
//    builder + ("ADDRESS-POSTALCODE", "60632") //todo
//    builder + ("ADDRESS-COUNTRY", "USA") //todo
//    builder + ("OPERATORS", "WA9NNN NEA9A KD9BYW") //todo

    cabrilloExportRequest.cabrilloValues.fieldValues.foreach{cv =>
      builder.+(cv.tagValue)
    }
    allQsos.foreach(qsoRecord => {
      builder + qso(qsoRecord.qso)
      addOne()
    }
    )
    val data = builder.toCabrilloData

    val path = Paths.get(cabrilloExportRequest.directory).resolve(cabrilloExportRequest.fileName)
    Using(new PrintWriter(Files.newBufferedWriter(path))) { printWriter =>
      CabrilloWriter.write(data, printWriter)
      data.lineCount
    } match {
      case Failure(exception) =>
        logger.error("Failed to write Cabrillo file!", exception)
      case Success(count) =>
        logger.info(f"Wrote $count%,d lines to $path.")
    }

    done()
  }


  def qso(q: fdQso): TagValue = {
    val band = q.bandMode.bandName //todo map to cabrillo
    val mode = q.bandMode.modeName //todo map to cabrillo?
    val datetime = ZonedDateTime.ofInstant(q.stamp, TimeHelpers.utcZoneId).format(cabrilloDtFormat)
    val ourCallSign = ourStation.ourCallsign
    val entryClass = ourStation.exchange.entryClass
    val section = ourStation.exchange.section
    //QSO: 14000 PH 2020-06-28 1647 WA9NNN        1D  IL     WA0ARM        1D  KS
    val body = f"$band%-6s $mode $datetime $ourCallSign%-13s $entryClass%3s $section%3s     ${q.callsign}%-13s ${q.exchange.entryClass}%3s ${q.exchange.section}%3s"
    SimpleTagValue("QSO", 0, body)
  }
}

object QsoFD {
  val cabrilloDtFormat: DateTimeFormatter = DateTimeFormatter ofPattern "yyyy-MM-dd HHmm"

}
  
