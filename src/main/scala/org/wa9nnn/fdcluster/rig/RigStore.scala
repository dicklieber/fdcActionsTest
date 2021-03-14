
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

import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.{StructuredLogging, Persistence}
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}

import javax.inject.Inject

/**
 * Manages Rig information
 *
 * @param preferences default or runtime, override with a mock Preferences for unit testing
 */
class RigStore @Inject()(persistence: Persistence) extends StructuredLogging {

  val rigFrequencyDisplay: StringProperty = new StringProperty()
  val band:StringProperty = new StringProperty()
  val rigFrequency = new IntegerProperty()

  rigFrequency.onChange { (ov, was, tobo) =>
    rigFrequencyDisplay.set(f"${tobo.intValue() / 1000000 % .04}")
  }

  val rigMode: StringProperty = StringProperty("None")

  val rigSettings: ObjectProperty[RigSettings] = new ObjectProperty[RigSettings]()

  private val prefsKey = "rigSettings"
  rigSettings.value = {
    persistence.loadFromFile[RigSettings].getOrElse(RigSettings())
  }
  rigSettings.onChange { (_, _, newSettings) =>
    persistence.saveToFile(rigSettings.value)
  }

}



