
package org.wa9nnn.fdcluster.store

import java.time.{Duration, LocalDateTime}
import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.javafx.entry.Sections
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.util.DebugTimer

import scala.util.Random

object QsoGenerator extends DefaultInstrumented  with DebugTimer with LazyLogging{
  val bmf = new BandModeFactory()

  def apply(numberfOfQsos: Int, betweewnQsos: Duration, startOfContest: LocalDateTime): List[QsoRecord] = {
     debugTime[List[QsoRecord]]("QsoGenerator") {
      var iteration = 0
      val secondsBetween = betweewnQsos.toSeconds
       for {
        area ← (0 to 9).toList
        suffix1 ← 'A' to 'Z'
        suffix2 ← 'A' to 'Z'
        suffix3 ← 'A' to 'Z'
        if iteration < numberfOfQsos

      } yield {
        iteration = iteration + 1
        val callsign = s"WA$area$suffix1$suffix2$suffix3"
        val qso = Qso(callsign, bandMode, exchange, startOfContest.plusSeconds(secondsBetween * iteration))
        val fdLOgId = FdLogId(nodeSn = iteration,
          nodeAddress = NodeAddress(0, "10.10.10.1"),
          uuid = UUID.randomUUID().toString)
        QsoRecord(qso, contest, ourStation, fdLOgId)
      }
    }
  }

  private val random = new Random(42)

  private val contest = Contest("UT", 2019)

  private val ourStation = OurStation("N9VTB", Exchange("5O", "IL"), "IC-7300", "vdipole")

  private def exchange: Exchange = {
    val section = Sections.sections(random.nextInt(Sections.sections.size - 1)).code
    Exchange("1A", section)

  }

  private def bandMode: BandModeOperator = {


    BandModeOperator(
      bmf.avalableBands(random.nextInt(bmf.avalableBands.size)).band,
      bmf.modes(random.nextInt(bmf.modes.size)).mode
    )
  }
}