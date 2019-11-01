
package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.MessageFormats.Digest
import org.wa9nnn.fdcluster.model.NodeAddress
import org.wa9nnn.fdcluster.model.sync.QsoHourDigest
import org.wa9nnn.fdcluster.store.network.FdHour
import org.wa9nnn.fdcluster.store.network.cluster.NodeStateContainer
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{TableColumn, TableView}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

class ClusterTable extends LazyLogging {
  private val data = ObservableBuffer[Row](Seq.empty)
  val tableView = new TableView[Row](data)

  def refresh(nodes: Iterable[NodeStateContainer]): Unit = {
    implicit val byAddress = new TrieMap[NodeAddress, NodeStateContainer]()
    val hours: mutable.Set[FdHour] = mutable.Set.empty
    nodes.foreach { nodeStateContainer ⇒
      byAddress.put(nodeStateContainer.nodeAddress, nodeStateContainer)
      nodeStateContainer.nodeStatus.qsoHourDigests.foreach(qsoHourDigest ⇒
        hours add qsoHourDigest.startOfHour
      )
    }

    val orderedNodes: List[NodeAddress] = byAddress.keySet.toList.sorted

    /**
     *
     * @param rowHeader string for 1st column
     * @param callback  how to extract body cell from a NodeStateContainer. Will be called for each [[NodeStateContainer]]
     * @return a row for the table
     */
    def buildRow(rowHeader: String, callback: NodeStateContainer ⇒ Any): Row = {
      MetadataRow(rowHeader, orderedNodes.map(nodeAddress ⇒ {
        val maybeContainer = byAddress.get(nodeAddress)
        callback(maybeContainer.get)
      }))
    }

    def buildHours: List[Row] = {
      hours.toList.sorted.map { fdHour: FdHour ⇒
        val digests: List[QsoHourDigest] = orderedNodes.map { nodeAddress ⇒
          val maybeDigest: Option[QsoHourDigest] = byAddress(nodeAddress).digestForHour(fdHour)
          maybeDigest match {
            case Some(qhd: QsoHourDigest) ⇒
              qhd
            case None ⇒
              QsoHourDigest(fdHour, "--", 0)
          }
        }

        HourRow(fdHour, digests)
      }
    }

    val rows: List[Row] = List(
      buildRow("Started", _.firstContact),
      buildRow("Last", _.nodeStatus.stamp),
      buildRow("QSOs", _.nodeStatus.qsoCount),
      buildRow("QSO/Minute", _.nodeStatus.qsoRate),
      buildRow("Digest", _.nodeStatus.digest),
      buildRow("Band", _.nodeStatus.currentStation.bandMode.band),
      buildRow("Mode", _.nodeStatus.currentStation.bandMode.mode),
      buildRow("Operator", _.nodeStatus.currentStation.ourStation.operator),
      buildRow("Rig", _.nodeStatus.currentStation.ourStation.rig),
      buildRow("Antenna", _.nodeStatus.currentStation.ourStation.antenna),
    ) ++ buildHours

    data.clear()
    data.addAll(rows: _*)

    def buildColumns = {
      val colTexts: List[String] = orderedNodes.map(_.display)

      colTexts.zipWithIndex.map(e ⇒
        new TableColumn[Row, Any] {
          sortable = false
          val colIndex = e._2
          text = e._1
          cellValueFactory = { x: TableColumn.CellDataFeatures[Row, Any] ⇒
            val row = x.value
            val r = row.cells(colIndex)
            new ObjectProperty(row, "row", r)
          }

          cellFactory = { _ =>
            new FdClusterTableCell[Row, Any]
          }
        }
      )
    }

    val rowHeaderCol = new TableColumn[Row, Any] {
      text = "Node"
      cellFactory = { _ =>
        new FdClusterTableCell[Row, Any]
      }
      cellValueFactory = { q ⇒
        new ObjectProperty(q.value, name = "rowHeader", q.value.rowHeader)
      }
      sortable = false
    }

    tableView.columns.clear()
    tableView.columns += rowHeaderCol
    buildColumns.foreach(tc ⇒
      tableView.columns += tc
    )
  }
}


