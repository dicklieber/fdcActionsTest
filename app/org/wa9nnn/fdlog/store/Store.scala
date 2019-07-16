/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import org.wa9nnn.fdlog.model.Contact.CallSign
import org.wa9nnn.fdlog.model._

trait Store {

  /**
    * Add this qso iof not a dup.
    *
    * @param potentialQso that may be added.
    * @return None if added, otherwise [[Contact]] that this is a dup of.
    */
  def add(potentialQso: Qso)(implicit stationContext: StationContext): Option[QsoRecord]

  /**
    * find potential matchs by callsign
    * @param in some or all of a callsign.
    * @return potneital matches.
    */
  def search(in: CallSign): Seq[QsoRecord]

  /**
    *
    * @return ids of all [[NodeDatabase]] known to this node.
    */
  def contactIds: NodeUuids

  def dump: Seq[QsoRecord]
}
