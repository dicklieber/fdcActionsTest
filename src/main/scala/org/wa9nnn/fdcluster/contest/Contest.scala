
package org.wa9nnn.fdcluster.contest

import com.wa9nnn.util.TimeConverters.fileStamp
import org.wa9nnn.fdcluster.model.{CallSign, Exchange, NodeAddress}

import java.time.{Instant, LocalDate}


/**
 * Information needed about the contest.
 * Should not change over the durtion of the contest.
 *
 * @param callSign    who we are. Usually the clubs callSign.
 * @param ourExchange what we will send to worked stations.
 * @param contestName which contest. We only support FD and Winter Field Day.
 * @param year        which one.
 */
case class Contest(callSign: CallSign = "",
                   ourExchange: Exchange = Exchange(),
                   contestName: String = "FieldDay",
                   nodeAddress: NodeAddress = NodeAddress(),
                   journalStart: Option[Instant] = None,
                   stamp: Instant = Instant.now()
                  ) {
  def checkValid: Unit = {
   if( callSign.isEmpty){
     throw new IllegalStateException(s"No CallSign!")
   }
  }

  def fileBase: String = {
    s"$contestName-${LocalDate.now().getYear.toString}"
  }

  val id: String = contestName.filter(_.isUpper)

  def qsoId: String = {
    f"$id$callSign"
  }
}

/**
 *
 * @param journalFileName     name of file contestname+YYYMMddHHmmssz.json, obtained from [[com.wa9nnn.util.TimeConverters#fileStamp(java.time.Instant]])
 * @param nodeAddress who started the instance.
 * @param stamp     when this was created. Newer always replaces older, anywhere in the cluster.
 */
case class Journal(journalFileName: String, nodeAddress: NodeAddress, stamp: Instant = Instant.now()) {

}

object Journal {
  def apply(contestName: String, nodeAddress: NodeAddress):Journal = {
    val instant = Instant.now()
    val fileName = contestName + fileStamp(instant) + ".json"
    new Journal(fileName, nodeAddress, instant)
  }

}


/**
 * JSON of this is the 1st line in the journal.
 *
 * @param journal        as sent around the cluster.
 * @param ourNodeAddress so we can tell whose journal this is.
 */
case class JournalHeader(journal: Journal, ourNodeAddress: NodeAddress)