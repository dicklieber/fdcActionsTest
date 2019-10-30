package org.wa9nnn.fdlog.http

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import org.wa9nnn.fdlog.javafx.sync.{RequestUuidsForHour, UuidsAtHost}
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model.QsosFromNode
import org.wa9nnn.fdlog.model.sync.QsoHour
import org.wa9nnn.fdlog.store.NodeInfo
import org.wa9nnn.fdlog.store.StoreActor.DumpQsos
import org.wa9nnn.fdlog.store.network.FdHour
import play.api.libs.json.JsValue

import scala.concurrent.duration._
import scala.language.postfixOps

trait UserRoutes extends LazyLogging {
  val nodeInfo: NodeInfo
  /**
   * Automatically applied to convert the JsValue, e.g. {{Json.toJson(qsoHours)}} to what complete() needs.
   * complete(Json.toJson(qsoHours))
   *
   * @param jsValue e.g. Json.toJson(x)
   * @return either a JSON string or a pretty-printed json string.
   */
  implicit def jsonToString(jsValue: JsValue): ToResponseMarshallable

  val store: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5 seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val userRoutes: Route =
    encodeResponse(

      concat(
        get {
          concat(
            pathSingleSlash {
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>todo add help for API!</body></html>"))
            },
            path("qsoHours") {
              val dummyFdHour = FdHour.allHours
              onSuccess((store ? dummyFdHour).mapTo[Seq[QsoHour]]) { qsoHours: Seq[QsoHour] ⇒
                complete(qsoHours)
              }
            },
            path("qsos") {
              onSuccess((
                store ? DumpQsos
                ).mapTo[QsosFromNode]) { qsos: QsosFromNode ⇒
                complete(qsos)
              }
            },

          )
        },

        path(FetchQsos.path) {
          post {
            entity(as[RequestUuidsForHour]) { uuidRequest ⇒
              onSuccess((
                store ? uuidRequest
                ).mapTo[UuidsAtHost]) { uuids: UuidsAtHost ⇒
                logger.debug(s"qsoUuids:  $uuids")
                complete {
                  uuids
                }
              }
            }
          }
        }
      )
    )

}
