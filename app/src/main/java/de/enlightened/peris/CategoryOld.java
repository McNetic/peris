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

import android.annotation.SuppressLint;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;

@SuppressLint("NewApi")
@SuppressWarnings("checkstyle:visibilitymodifier")
public class CategoryOld {
  public String description = "Category Description";
  public String name = "Category Name";
  public String id = "0";
  public String subforumId = "0";
  public Date lastUpdate = null;
  public String lastThread = "Thread Name";
  public String threadCount = "0";
  public String viewCount = "0";
  public View subforumSeperator;
  public String moderator;
  public String color = "#000000";
  public String icon = "n/a";
  public String mature = "N";
  public String type = "C";
  public String onUnified = "Y";
  public boolean canSticky = false;
  public boolean canLock = false;
  public boolean canDelete = false;
  public boolean canSubscribe = false;
  public boolean isSubscribed = false;
  public boolean isLocked = false;
  public String url = "n/a";
  public boolean hasNewTopic = false;
  public boolean hasChildren = false;
  public String topicSticky = "N";
  public ArrayList<CategoryOld> children;
}
