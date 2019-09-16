package org.wa9nnn.fdlog.model

import org.specs2.mutable.Specification

class QsoRecordSpec extends Specification {

  "Binary" should {
    "round trip" in {
      val qsoRecord = QsoRecord(Contest("FD", 2019),
        OurStation("WM9W"),
        Qso("WA9NNN", BandMode(Band("20m"), Mode.digital), Exchange("1A", "IL")),
        FdLogId(1, NodeAddress()))
      val dso = DistributedQsoRecord(qsoRecord, NodeAddress(), 42)
      val byteString = dso.toByteString
      val backAgain = DistributedQsoRecord(byteString)
      backAgain must beEqualTo(dso)
    }


  }
}
