/*
 * Copyright © 2021 Dick Lieber, WA9NNN
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
 */

package org.wa9nnn.fdcluster.contest

import org.wa9nnn.fdcluster.FileManager
import org.wa9nnn.fdcluster.javafx.GridOfControls
import _root_.scalafx.scene.control.{Button, Hyperlink, Label, TitledPane}
import _root_.scalafx.scene.layout.VBox
import _root_.scalafx.Includes._
import scalafx.geometry.Insets

import java.awt.Desktop
import javax.inject.Inject

class JournalDialogPane @Inject()(journalProperty: JournalProperty, fileManager: FileManager) {
  val gridOfControls = new GridOfControls()
  private val desktop = Desktop.getDesktop

  gridOfControls.add("Current", journalProperty.fileName)
  private val newJournalButton = new Button("New Journal")
  gridOfControls.add(newJournalButton,
    1, gridOfControls.row.getAndIncrement())

  val lastGoc = new GridOfControls(5 -> 5, Insets(5.0))
  journalProperty.maybeJournal.foreach{journal =>
    lastGoc.add("From", journal.nodeAddress.display)
    lastGoc.add("At", journal.stamp)
    gridOfControls.add("Last Changed", lastGoc)
  }

  gridOfControls.addControl("Journal Files", new Hyperlink(fileManager.journalDir.toString) {
    onAction = event => {
      desktop.open(fileManager.directory.toFile)
    }
  })

  newJournalButton.onAction = () =>
    journalProperty.newJournal()

  val pane: TitledPane = new TitledPane() {
    text = "Journal"
    content = new VBox(new Label(
      """QSOs are stored in a journal on each node. FdCluser synchronizes the journal
        |on each node with others.
        |Changing the name of the journal starts new journals on all nodes.
        |Before the contest starts you can make start new journals to practice; but once
        |the contest starts do not start a new journal!
        |Journals contain JSON data. A header line and one line per QSO. You
        |can viw them """.stripMargin) {
      styleClass += "helpPane"
    }
      , gridOfControls)
    collapsible = false
  }


}