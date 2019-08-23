
package org.wa9nnn.fdlog.javafx.entry


import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control._
import scalafx.stage.Stage

import scala.collection.JavaConverters._

class FdLogMenu(stage: Stage) {
  private val environmentMenuItem = new MenuItem {
    text = "Environment"
    onAction = { ae: ActionEvent =>
      val d: Dialog[Nothing] = new Dialog() {
        title = "Information Dialog"
        private val keys = System.getProperties.keySet().asScala.map(_.toString)
        contentText =
          keys.toList
            .sorted
            .map(key ⇒
              s"${key}: \t${System.getProperty(key).take(35)}").mkString("\n")


      }
      d.dialogPane().buttonTypes = Seq( ButtonType.Close)
      d.showAndWait()

      //      new Alert(AlertType.Information) {
      //        initOwner(stage)
      //        title = "Information Dialog"
      //        headerText = "Look, an Information Dialog."
      //        contentText = "I have a great message for you!"
      //      }.showAndWait()
    }
  }
  TextInputDialog
  val menuBar: MenuBar = new MenuBar {
    menus = List(
      new Menu("_File") {
        mnemonicParsing = true
        items = List(
          new MenuItem("New..."),
          new MenuItem("Save")
        )
      },
      new Menu("_Edit") {
        mnemonicParsing = true
        items = List(
          new MenuItem("Cut"),
          new MenuItem("Copy"),
          new MenuItem("Paste")
        )
      },
      new Menu("_Help") {
        mnemonicParsing = true
        items = List(
          environmentMenuItem,
          new MenuItem("About"),
        )
      }
    )
  }
}
