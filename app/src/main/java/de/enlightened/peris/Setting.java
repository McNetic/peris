/*
 * Copyright (C) 2014 - 2015 Initial Author
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

package de.enlightened.peris;

@SuppressWarnings("checkstyle:visibilitymodifier")
public class Setting {
  public String settingName = "Setting";
  public String settingColor = "0";
  public int settingIcon = R.drawable.drawer_favorites;
  public int counterItem = 0;

  public Setting(final String settingName, final int settingIcon, final int counterItem) {
    this.settingName = settingName;
    this.settingIcon = settingIcon;
    this.counterItem = counterItem;
  }

  public Setting(final String settingName, final int settingIcon) {
    this.settingName = settingName;
    this.settingIcon = settingIcon;
  }
}
