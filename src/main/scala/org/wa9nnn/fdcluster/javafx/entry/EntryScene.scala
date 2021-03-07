
package org.wa9nnn.fdcluster.javafx.entry

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import org.wa9nnn.fdcluster.javafx.{CallSignField, Section}
import org.wa9nnn.fdcluster.model
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.{AddResult, Added, Dup}
import org.wa9nnn.util.InputHelper._
import play.api.libs.json.Json
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, HBox, VBox}

import java.util.concurrent.TimeUnit
import scala.concurrent.Await

/**
 * Create ScalaFX UI for field day entry mode.
 */
class EntryScene @Inject()(@Inject()
                           bandModeStore: BandModeOperatorStore,
                           bandModeOpPanel: BandModeOpPanel,
                           @Inject() @Named("store") store: ActorRef) {
  private implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  val qsoCallsign = new CallSignField() {
    styleClass.append("sadQso")
  }
  val qsoClass: TextField = new TextField() {
    styleClass.append("sadQso")
  }
  val qsoSection: TextField = new TextField()
  qsoSection.getStyleClass.add("sadQso")

  var sectionPrompt = new TextArea()
  sectionPrompt.getStyleClass.add("sectionPrompt")
  sectionPrompt.disable
  var actionResult = new TextArea()
  actionResult.getStyleClass.add("dupPrompt")
  actionResult.disable

  val qsoSubmit = new Button("Log")
  qsoSubmit.disable = true
  qsoSubmit.getStyleClass.add("sadQso")
  val pane: BorderPane = new BorderPane {
    padding = Insets(25)
    center = new HBox(
      new VBox(
        new Label("Callsign"),
        qsoCallsign,
        actionResult
      ),
      new VBox(
        new Label("Class"),
        qsoClass,
        new VBox(
          qsoSubmit,
          bandModeOpPanel
        )
      ),
      new VBox(
        new Label("Section"),
        qsoSection,
        sectionPrompt
      )
    )
  }

  val scene: Scene = new Scene {
    root = pane
  }

  private val qsoCallsignText = qsoCallsign.textProperty()
  private val qsoClassText = qsoClass.textProperty()
  private val qsoSectionText = qsoSection.textProperty()


  private val allSections = Sections.sections.map { section: Section ⇒ f"${section.code}%-3s" }
    .grouped(7)
    .map(_.mkString(" "))
    .mkString("\n")

  sectionPrompt.setText(allSections)
  qsoSectionText.addListener { (_, _, newValue) =>
    val choices = if (newValue != "") {
      Sections
        .find(newValue)
        .map(section ⇒ section.code + ": " + section.name)
        .mkString("\n")
    } else {
      allSections
    }
    sectionPrompt.setText(choices)
  }

  forceCaps(qsoClass)
  forceCaps(qsoSection)

  qsoCallsign.onDone(nextChar =>
    nextField(nextChar, qsoClass)
  )
  //  qsoCallsign.addEventFilter(KeyEvent.KEY_TYPED,
  //    (event: KeyEvent) => {
  //      val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
  //      val character = event.getCharacter
  //      if (character.isDefinedAt(0) && character.charAt(0).isDigit && CallsignValidator.valid(current).isEmpty) {
  //        nextField(event, qsoClass)
  //      }
  //      if (current.isEmpty) {
  //        actionResult.clear()
  //      }
  //
  //      Platform.runLater {
  //        validateQso()
  //      }
  //    }
  //  )

  //  qsoClass.addEventFilter(KeyEvent.KEY_TYPED,
  //    (event: KeyEvent) => {
  //      val current = event.getSource.asInstanceOf[jfxsc.TextField].getText
  //      if (ContestClass.valid(current).isEmpty) {
  //        nextField(event, qsoSection)
  //      }
  //      Platform.runLater {
  //        validateQso()
  //      }
  //    }
  //  )

  //  qsoSection.addEventFilter(KeyEvent.KEY_TYPED,
  //    (event: KeyEvent) ⇒ {
  //      if (event.getCharacter == "\r" && !qsoSubmit.isDisabled) {
  //        save()
  //      }
  //      Platform.runLater {
  //        validateQso()
  //      }
  //    }
  //  )

  qsoSubmit.onAction = (_: ActionEvent) => {
    save()
  }

//  def validateQso(): Unit = {

    //    def validateField(destination: WithDisposition, validField: FieldValidator): Boolean = {
    //      if (validField.valid(destination).isEmpty) {
    //        destination.happy()
    //      } else {
    //        destination.sad()
    //      }
    //    }
    //
    //    if (validateField(qsoCallsign, ContestCallsignValidator) &&
    //      validateField(qsoClass, ContestClass) &&
    //      validateField(qsoSection, ContestSection)) {
    //      qsoSubmit.disable = false
    //      makeHappy(qsoSubmit)
    //    }
    //    else {
    //      qsoSubmit.disable = true
    //      makeSad(qsoSubmit)
    //    }
    //  }


    //  def showSad(destination: TextInputControl, message: String): Unit = {
    //    makeSad(destination)
    //    destination.setText(message)
    //  }
    //
    //  def showHappy(destination: TextInputControl, message: String): Unit = {
    //    makeHappy(destination)
    //    destination.setText(message)
    //  }

    def save(): Unit = {
      import org.wa9nnn.fdcluster.model.MessageFormats._
      val potentialQso: Qso = readQso()

      val future = store ? potentialQso
      Await.result(future, timeout.duration).asInstanceOf[AddResult] match {
        case Dup(dupQso) ⇒
          val pretty = Json.prettyPrint(Json.toJson(dupQso.qso))
        //        showSad(actionResult, s"Duplicate:\n$pretty")
        case Added(qsoRecord) ⇒
        //        showHappy(actionResult, s"Added:\n${qsoRecord.qso.callsign} ${qsoRecord.qso.exchange}")
      }

      qsoCallsign.clear()
      qsoClass.clear()
      //    qsoSection.value = ""
      qsoSection.clear()
      qsoCallsign.requestFocus()
    }

    def readQso(): Qso = {
      val exchange = Exchange(qsoClassText.get(), qsoSectionText.get())

      model.Qso(qsoCallsignText.get(), bandModeStore.bandModeOperator, exchange)
    }

    def nextField(nextChar: Char, destination: TextField): Unit = {
      destination.requestFocus()
      destination.setText(nextChar.toString())
      destination.positionCaret(1)
    }


  }

