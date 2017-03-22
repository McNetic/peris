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

public final class CategoryParser {

  private static final String TAG = CategoryParser.class.getName();

  private CategoryParser() {
  }

  /*@SuppressWarnings("rawtypes")
  public static ArrayList<CategoryOld> parseCategories(final Object[] data, final String subforumId, final String background) {
    final ArrayList<CategoryOld> categories = new ArrayList<CategoryOld>();

    for (Object o : data) {
      if (o != null) {
        final Map map = (Map) o;
        final CategoryOld ca = new CategoryOld();
        ca.name = new String((byte[]) map.get("forum_name"));
        ca.subforumId = subforumId;
        ca.id = (String) map.get("forum_id");
        ca.type = "S";
        ca.color = background;

        if (map.get("logo_url") != null) {
          ca.icon = (String) map.get("logo_url");
        }
        if (map.get("url") != null) {
          ca.url = (String) map.get("url");
        }
        if (map.get("is_subscribed") != null) {
          ca.isSubscribed = (Boolean) map.get("is_subscribed");
        }
        if (map.get("can_subscribe") != null) {
          ca.canSubscribe = (Boolean) map.get("can_subscribe");
        }
        if (map.get("new_post") != null) {
          ca.hasNewTopic = (Boolean) map.get("new_post");
        }

        Boolean subOnly = false;
        if (map.get("sub_only") != null) {
          subOnly = (Boolean) map.get("sub_only");
          ca.hasChildren = true;
          if (ca.hasChildren) {
            Log.v(TAG, "aaa sub only on " + ca.id);
          }
        }
        if (subOnly) {
          if (map.get("child") != null) {
            ca.id = subforumId + "###" + (String) map.get("forum_id");
            ca.children = parseCategories((Object[]) map.get("child"), ca.id, background);
          }
        }
        categories.add(ca);
      }
    }
    return categories;
  }*/
}
