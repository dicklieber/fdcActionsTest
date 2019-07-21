
package org.wa9nnn.fdlog.javafx

import org.wa9nnn.fdlog.model
import org.wa9nnn.fdlog.model.{Band, BandMode, NodeInfo, NodeInfoImpl, OurStation, StationContext}
import org.wa9nnn.fdlog.store.StoreMapImpl
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import org.wa9nnn.fdlog.model.Mode

object FdLog extends JFXApp {


  private val contest = model.Contest("WFD", 2019)
  implicit val nodeInfo: NodeInfo = new NodeInfoImpl(contest)

  private val store = new StoreMapImpl(nodeInfo)
  private val entryScene = new FDLogEntryScene()

  private val stationContext = StationContext(
    store = store,
    operator = OurStation("WA9NNN", "IC-7300", "Endfed"),
    bandMode = BandMode(Band("20m"), Mode.phone))


  new FDLogEntryController(entryScene, stationContext)
  private val ourScene: Scene = entryScene.scene

  private val cssUrl: String = getClass.getResource("/org/wa9nnn/fdlog/javafx/fdlog.css").toExternalForm
  ourScene.getStylesheets.add(cssUrl)


  stage = new PrimaryStage() {
    title = "FDLog"
    scene = ourScene
  }
}