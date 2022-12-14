
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

package org.wa9nnn.fdcluster.javafx.entry

import _root_.scalafx.Includes._
import _root_.scalafx.beans.binding.{Bindings, ObjectBinding}
import _root_.scalafx.event.ActionEvent
import _root_.scalafx.geometry.{Insets, Pos}
import _root_.scalafx.scene.control._
import _root_.scalafx.scene.layout.{BorderPane, HBox, VBox}
import akka.util.Timeout
import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.contest.OkGate
import org.wa9nnn.fdcluster.javafx.entry.section.SectionField
import org.wa9nnn.fdcluster.javafx.{CallSignField, ClassField, StatusMessage, StatusPane}
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.{AddResult, StoreSender}
import org.wa9nnn.util.WithDisposition
import org.wa9nnn.webclient.{Session, SessionManagerSender, UpdateStation}

import java.lang
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
 * Create ScalaFX UI for field day entry mode.
 */
@Singleton
class EntryTab @Inject()(
                         currentStationPanel: StationPanel,
                         contestProperty: ContestProperty,
                         nodeAddress: NodeAddress,
                         classField: ClassField,
                         stationProperty: StationProperty,
                         qsoMetadata: OsoMetadataProperty,
                         statsPane: StatsPane,
                         statusPane: StatusPane,
                         storeSender: StoreSender,
                         callSignField: CallSignField,
                         actionResult: ActionResult,
                         sessionManagerSender: SessionManagerSender
                        ) extends Tab with LazyLogging {
  private implicit val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  text = "Entry"
  closable = false

  val qsoSection: SectionField = new SectionField() {
    styleClass += "qsoSection"
  }

  val qsoSubmit = new Button("Log") with WithDisposition
  val clearButton = new Button("Clear") with WithDisposition
  clearButton.onAction = _ => {
    clear()
  }
  qsoSubmit.disable = true
  qsoSubmit.sad()
  private val initialExchange = contestProperty.ourExchangeProperty
  val ourExchangeLabel: Label = new Label(initialExchange.value.display) {
    styleClass += "exchange"
  }
  val ourExchangeMnomicLabel: Label = new Label(initialExchange.value.mnomonics) {
    styleClass += "exchangeMnemonics"
  }
  contestProperty.ourExchangeProperty.onChange { (_, _, ex) =>
    onFX {
      ourExchangeLabel.text = ex.display
      ourExchangeMnomicLabel.text = ex.mnomonics
    }
  }

  var session: Session = Await.result[Session](sessionManagerSender ?[Session] stationProperty.value, 2.seconds)

  stationProperty.onChange { (_, _, station) =>
    sessionManagerSender ! UpdateStation(session.sessionKey,station)
  }


  val entryPane: BorderPane = new BorderPane {
    padding = Insets(25)
    private val buttons = new HBox() {
      alignment = Pos.BottomCenter
      spacing = 8
      padding = Insets(10)
      children = List(qsoSubmit, clearButton)
    }
    center = new HBox(
      new VBox(
        new Label("CallSign"),
        callSignField,
        actionResult,
        statsPane.pane,
        OkGate.pane
      ),
      new VBox(
        new Label("Class"),
        classField,
        new VBox(
          buttons,
          currentStationPanel.pane
        )
      ),
      new VBox(
        new Label("Section"),
        new HBox(qsoSection, new Label("We are: "), ourExchangeLabel, ourExchangeMnomicLabel),
        qsoSection.sectionPrompt
      )
    )
  }

  callSignField.onDone { next =>
    if (classField.text.value.isEmpty) {
      nextField(next, classField)
    }
  }
  classField.onDone { _ =>
    qsoSection.requestFocus()
    qsoSection.clear()
  }
  qsoSection.onDone { _ =>
    qsoSubmit.disable = false
    qsoSubmit.happy()
    save()
  }
  qsoSubmit.onAction = (_: ActionEvent) => {
    save()
  }

  val allFields = new Compositor(callSignField.validProperty, classField.validProperty, qsoSection.validProperty)
  allFields.onChange { (_, _, state) =>
    if (state) {
      qsoSubmit.disable = false
      qsoSubmit.happy()
    } else {
      qsoSubmit.disable = true
      qsoSubmit.sad()
    }
  }

  OkGate.onChange { (_, _, nv: lang.Boolean) =>
    OkGate.allOkItems
  }

//    content = setOk(OkGate.value)
    OkGate.onChange { (_, _, nv) =>
//      onFX {
//        content = setOk(nv)
//      }
    }

    def setOk(ok: Boolean): Unit = {
//      if (ok)
//        entryPane
//      else
//        injector.instance[NotReadyPane]
    }
  content = entryPane

  def save(): Unit = {
    val potentialQso: Qso = qsoMetadata.qso(

      callSign = callSignField.text.value,
      exchange = Exchange(classField.text.value, qsoSection.text.value),
      bandMode = stationProperty.bandMode
    )
    if (potentialQso.callSign == contestProperty.callSign) {
      actionResult.sadMessage(s"Can't work our own station: \n${potentialQso.callSign}!")
    }
    else {
      val future: Future[AddResult] = storeSender ?[AddResult] potentialQso
      future onComplete { tr: Try[AddResult] =>
        clear()
        val triedQso = tr.get.triedQso
        actionResult(triedQso)

        if (triedQso.isSuccess && triedQso.get.callSign == "WA9NNN") {
          onFX {
            statusPane.message(StatusMessage("Thanks for using fdcluster, from Dick Lieber WA9NNN", styleClasses = Seq("hiDick")))
          }
        }
      }
    }
  }

  private def clear(): Unit = {
    onFX {
      callSignField.reset()
      classField.reset()
      qsoSection.reset()
      callSignField.requestFocus()
    }
  }

  /**
   *
   * @param nextText    what start off next field with.
   * @param destination the next field.
   */
  def nextField(nextText: String, destination: TextField): Unit = {
    destination.requestFocus()
    destination.positionCaret(1)
  }


  clear()

  val qsoMetadataBinding: ObjectBinding[QsoMetadata] = Bindings.createObjectBinding(() => {
    val cs = stationProperty.value
    QsoMetadata(operator = cs.operator,
      rig = cs.rig,
      ant = cs.antenna,
      node = nodeAddress.qsoNode,
      journal = contestProperty.contest.qsoId
    )
  }, stationProperty, contestProperty)


}

