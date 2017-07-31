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
package org.wa9nnn.fdlog.model

import java.time.Instant
import java.util.UUID
import NodeInfo.Node

/**
  * Ids on a node.
  *
  * @param contactIds ids on the node.
  * @param node       where this came from.
  * @param stamp      as of.
  *
  */
case class NodeUuids(contactIds: Set[UUID], node: Node, stamp: Instant)

object NodeUuids {
  def apply(uuids: Set[UUID] = Set.empty[UUID])(implicit node: Node): NodeUuids = {
   new  NodeUuids(uuids, node, Instant.now())
  }
}


case class ContactRequest(contactIds: Set[UUID], requestingNode: Node)

object NodeIuids {
  def apply(contactIds: Set[UUID] = Set.empty[UUID])(implicit node: Node): ContactRequest = {
    ContactRequest(contactIds, node)
  }
}

/**
  * Usually this will be in response to a ContactRequest request.
  *
  * @param stamp    as of.
  * @param node     where this came from.
  * @param contacts on node.
  */
case class Contacts(contacts: Seq[Contact], node: Node, stamp: Instant = Instant.now())

object Contacts {
  def apply(contacts: Seq[Contact])(implicit node: Node): Contacts = Contacts(contacts, node)
}
