
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

package org.wa9nnn.fdcluster.rig

import com.github.racc.tscg.TypesafeConfig
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.text.StringSubstitutor

import javax.inject.Inject
import scala.jdk.CollectionConverters._
import scala.sys.process._

/**
 * invokes hamlib  rigctld.
 *
 * @param rigConfig part from application.conf.
 */
class Rigctld @Inject()(@TypesafeConfig("fdcluster.rig") rigConfig: Config, rigStore: RigStore) extends LazyLogging {
  private val pl: ProcessLogger = new ProcessLogger {
    override def err(s: => String): Unit = {
      logger.trace(s)
    }

    override def out(s: => String): Unit = {
      throw new NotImplementedError() //todo
    }

    override def buffer[T](f: => T) = {
      throw new NotImplementedError() //todo
    }
  }
  private val map: Map[String, Seq[RigModel]] = {

    val args: Seq[String] = rigConfig.getString("rigList").split("""\s+""")
    args.lazyLines_!(pl)
      .drop(1) // get rid of header
      .map { l =>
        val tokens: Array[String] = l.split("""\s+""")
        RigModel(tokens(1).toInt, tokens(2), tokens(3))
      }
      .groupBy(_.mfg)
  }

  val rigManufacturers: Seq[String] = map.keySet.toList.sorted

  /**
   *
   * @param mfg one of the values returned by [[rigManufacturers]]
   * @return
   */
  def modelsForMfg(mfg: String): Seq[RigModel] = {
    map(mfg).sorted
  }

  val version: String = {
    val str = rigConfig.getString("rigctldVersion")
    val process: Process = str.run()
    val value: LazyList[String] = str lazyLines pl
    value.head
  }

  def start(rigSettings: RigSettings): Unit = {
    if(rigSettings.serialPortSettings.port.isEmpty){
      throw new IllegalStateException("No serial port selected!")
    }
    val valuesMap: Map[String, Any] = Seq(
      "modelId" -> rigSettings.rigModel.number,
      "speed" -> rigSettings.serialPortSettings.baudRate,
      "deviceName" ->  ( rigSettings.serialPortSettings.port.get.port)
    ).toMap
    val ss = new StringSubstitutor(valuesMap.asJava, "<", ">")

    // ${fdcluster.rig.rigctldApp} -m <modelId> -r <deviceName>
    val str = rigConfig.getString("launchRigctld")
    val commandLine = ss.replace(str)
    logger.info(s"""Starting rigctld with "$commandLine" """)
    val process: Process = commandLine.run()
    rigctldProcess = Option(process)
    logger.info(s"Started rigctld with process: $process")

  }

  private var rigctldProcess: Option[Process] = None

  def stop(): Unit = {
    rigctldProcess.foreach(p =>
    p.destroy())
  }
  rigStore.rigSettings.onChange{(_,_,newRigSettings) =>
    stop()
    start(newRigSettings)
  }
}


case class RigModel(number: Int, mfg: String = "", model: String = "") extends Ordered[RigModel] {
  def choice: String = s"$model ($number)"

  override def toString: String = {
    s"$mfg \t$model \t($number)"
  }

  override def compare(that: RigModel): Int = {
    val ret = this.mfg.compareToIgnoreCase(that.mfg)
    if (ret == 0)
      this.model.compareToIgnoreCase(that.model)
    else
      ret
  }
}

object RigModel {
  def apply(): RigModel = RigModel(-1, "None", "-")
}



