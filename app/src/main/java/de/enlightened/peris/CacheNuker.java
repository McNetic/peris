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

import android.content.Context;
import android.content.SharedPreferences;

import com.nostra13.universalimageloader.core.ImageLoader;

public final class CacheNuker {

  private CacheNuker() {
  }

  public static void nukeCache(final Context context) {
    final SharedPreferences appPreferences = context.getSharedPreferences("prefs", 0);
    final SharedPreferences.Editor editor = appPreferences.edit();
    editor.clear();
    editor.commit();
    //TrimCache trimmer = new TrimCache(c);
    //trimmer.trim();
    final ImageLoader imageLoader = ImageLoader.getInstance();
    imageLoader.clearDiskCache();
    imageLoader.clearMemoryCache();
  }
}
