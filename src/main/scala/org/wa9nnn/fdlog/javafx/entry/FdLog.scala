
package org.wa9nnn.fdlog.javafx.entry

import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions._
import org.wa9nnn.fdlog.http.Server
import org.wa9nnn.fdlog.javafx.cluster.ClusterScene
import org.wa9nnn.fdlog.javafx.data.DataScene
import org.wa9nnn.fdlog.store.NodeInfo
import org.wa9nnn.fdlog.{Module, model}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.event.Event
import scalafx.scene.Scene
import scalafx.scene.control.{Tab, TabPane}
import scalafx.scene.layout.BorderPane

/**
 * Main for FDLog
 */
object FdLog extends JFXApp {
//  val system: ActorSystem = ActorSystem("FdLogAkka")

  private val injector = Guice.createInjector(new Module())
  private val contest = model.Contest("WFD", 2019)
//  implicit val nodeInfo: NodeInfo = new NodeInfoImpl(contest)
//  private val storeActorRef: ActorRef = injector.getInstance(Key.get(classOf[ActorRef], Names.named("store")))
  private val entryScene = injector.instance[EntryScene]
  private val dataScene = injector.instance[DataScene]
  private val clusterScene = injector.instance[ClusterScene]
  private val nodeInfo: NodeInfo = injector.instance[NodeInfo]
  try {
    injector.instance[Server]
  } catch {
    case e:Throwable ⇒
      e.printStackTrace()
  }
  val fdlogmenu: FdLogMenu = injector.instance[FdLogMenu]

  private val dataTab: Tab = new Tab {
    text = "Data"
    content = dataScene.pane
    closable = false
  }
  private val entryTab: Tab = new Tab {
    text = "Entry"
    content = entryScene.pane
    closable = false
  }
  private val clusterTab: Tab = new Tab {
    text = "Cluster"
    content = clusterScene.pane
    closable = false
  }
  val tabPane: TabPane = new TabPane {
    tabs = Seq(entryTab, dataTab, clusterTab)
  }

  dataTab.onSelectionChanged = (_: Event) => {
    if (dataTab.isSelected) {
      dataScene.refresh()
    }
  }
  clusterTab.onSelectionChanged = (_: Event) => {
    if (clusterTab.isSelected) {
      clusterScene.refresh()
    }
  }
//  private val statsHeader = new HBox(Label(f"QSOs:  todo "))
  private val rootPane = new BorderPane {
    top = fdlogmenu.menuBar
    center = tabPane
  }
  val ourScene = new Scene()

  ourScene.getStylesheets.add(getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm)

  private val cssUrl: String = getClass.getResource("/fdlog.css").toExternalForm
  ourScene.getStylesheets.add(cssUrl)

  ourScene.root = rootPane

  stage = new PrimaryStage() {
    title = "FDLog @ " + nodeInfo.nodeAddress.display
    scene = ourScene
  }

}
