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

package org.wa9nnn.fdcluster.http

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
import org.wa9nnn.fdcluster.javafx.sync.{RequestUuidsForHour, UuidsAtHost}
import org.wa9nnn.fdcluster.model.QsosFromNode
import org.wa9nnn.fdcluster.model.sync.QsoHour
import org.wa9nnn.fdcluster.store.{DumpQsos, NodeInfo}
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.JsValue
import org.wa9nnn.fdcluster.model.MessageFormats._

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
