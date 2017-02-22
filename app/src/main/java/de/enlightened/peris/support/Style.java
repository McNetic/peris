/*
 * Copyright (C) 2017 Nicolai Ehemann
 *
 * This file is part of Peris.
 *
 * Peris is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Peris is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Peris.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.enlightened.peris.support;

import android.graphics.Color;

public final class Style {
  private Style() {
  }

  public static String colorToColorString(final int color) {
    return String.format("%x%x%x", Color.red(color), Color.green(color), Color.blue(color));
  }

  public static String colorToAColorString(final int color) {
    return String.format("%x%s", Color.alpha(color), colorToColorString(color));
  }
}
