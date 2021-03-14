
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

package org.wa9nnn.util

import scalafx.css.Styleable

/**
 * A Control (actually any [[Styleable]]) that can be happy or sad.
 * This manipulate the style class making it happy, sad or removing either happy or sad for neutral.
 */
trait WithDisposition extends Styleable {
  neutral()

  def disposition(boolean: Boolean): Unit = {
    if (boolean)
      happy()
    else
      sad()
  }

  def happy(): Unit = disposition(Disposition.happy)

  def sad(): Unit = disposition(Disposition.sad)

  def neutral(): Unit = disposition(Disposition.neutral)

  def disposition(happySadNeutral: Disposition): Unit = {
    styleClass.remove("sad")
    styleClass.remove("happy")
    if (happySadNeutral != Disposition.neutral)
      styleClass.addOne(happySadNeutral.getStyle)
  }
}
