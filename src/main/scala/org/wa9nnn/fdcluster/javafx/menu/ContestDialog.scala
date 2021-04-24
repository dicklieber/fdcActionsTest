
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

package org.wa9nnn.fdcluster.javafx.menu

import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.javafx.GridOfControls
import org.wa9nnn.fdcluster.javafx.entry.Sections
import org.wa9nnn.fdcluster.javafx.entry.section.Section
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.station.StationDialogLogic
import org.wa9nnn.util.StructuredLogging
import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.layout.VBox

import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UI for things that need to be setup for the contest.
 *
 */
class ContestDialog @Inject()(contestProperty: ContestProperty,
                              contestRules: AllContestRules,
                              nodeAddress: NodeAddress) extends Dialog[Contest] with StructuredLogging {
  val dp: DialogPane = dialogPane()

  private val saveButton = new ButtonType("Save", ButtonData.OKDone)
  private val cancelButton = ButtonType.Cancel

  private val gridOfControls = new GridOfControls
  private val callSignProperty: StringProperty = gridOfControls.addText("CallSign",
    tooltip = Some("""CallSign of the club or individual entrant."""),
    defValue = contestProperty.callSign,
    forceCaps = true)

  private val contestCB: ObjectProperty[String] = gridOfControls.addCombo[String](
    labelText = "Contest",
    choices = ObservableBuffer(contestRules.contestNames),
    defValue = Some(contestProperty.contestName)
  )


  private val currentExchange: Exchange = contestProperty.ourExchange

  private val transmitters = new {
  } with Spinner[Integer](1, 30, currentExchange.transmitters) {
    valueFactory().value = currentExchange.transmitters
    tooltip = "Number of simultaneous transmitters. Combined with category to produce exchange class."
  }
  gridOfControls.addControl("Transmitters", transmitters)

  val categoryCB: ComboBox[EntryCategory] = new ComboBox[EntryCategory]()
  gridOfControls.addControl("Category", categoryCB)

  contestCB.onChange { (_, _, contestName: String) =>
    setup(contestName)
  }
  setup(contestProperty.contestName)

  private def setup(contestName: String): Unit = {
    //    contestProperty.contestNameProperty.value = contestName
    val rules = contestRules.byContestName(contestName)
    categoryCB.items = rules.categories.categories
    categoryCB.value = {
      rules.categories.entryCategoryForDesignator(currentExchange.category)
    }
  }

  val sectionProperty: ObjectProperty[Section] = gridOfControls.addCombo[Section](
    labelText = "Section",
    defValue = Some(Sections.byCode(currentExchange.sectionCode)),
    tooltip = Option("ARRL sectionCode for exchange sent."),
    choices = Sections.forChoice())


  val exchangeText: Label = new Label()
  val exchangeMnemonics: Label = new Label() {
    styleClass += "exchangeMnemonics"
  }
  private val exchangeDisplay: VBox = new VBox(
    exchangeText, exchangeMnemonics
  )
  private val exchangePane = new VBox(
    new Label("Exchange:"),
    exchangeDisplay
  ) {
    alignmentInParent = Pos.Center
    styleClass += "exchangeDisplay"
    styleClass += "exchangeBlock"
  }


  title = "Station Settings"
  headerText = s"This node: ${nodeAddress.display}"

  // Build the result
  resultConverter = {
    button: ButtonType ⇒
      if (button == saveButton) {
        stationDialogLogic.exchange.foreach {
          exchange =>
            val newContest = Contest(callSign = callSignProperty.value,
              ourExchange = exchange,
              contestName = contestCB.value,
              nodeAddress = nodeAddress)

            contestProperty.save(newContest)
        }
      }
      null
  }


  dp.getButtonTypes.addAll(saveButton, cancelButton)
  dp.getStylesheets.addAll(
    getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm,
    getClass.getResource("/fdcluster.css").toExternalForm
  )

  val stationDialogLogic = new StationDialogLogic(
    callSignProperty,
    transmitters.valueFactory.value,
    categoryCB.value,
    sectionProperty,
    exchangeMnemonics.text,
    exchangeText.text,
    dp.lookupButton(saveButton).disableProperty()
  )
  //todo don't bind with acutal properties until save
  gridOfControls.add("Exchange", exchangePane)
  private val contest: Contest = contestProperty.contest

  import com.wa9nnn.util.tableui.Cell

  val lastGoc = new GridOfControls(5->5,  Insets(5.0))
  lastGoc.add("From", contest.nodeAddress.display)
  lastGoc.add("At", contest.stamp)
  gridOfControls.add("Last Changed", lastGoc)
  dialogPane().setContent(gridOfControls)
}

