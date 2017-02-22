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
import android.util.Log;

import java.io.File;

public class TrimCache {

  private static final String TAG = TrimCache.class.getName();

  private Context context;

  public TrimCache(final Context context) {
    this.context = context;
  }

  public static boolean deleteDir(final File dir) {
    if (dir != null && dir.isDirectory()) {
      final String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        final boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }
    return dir.delete();
  }

  public void trim() {
    final File cache = this.context.getCacheDir();
    final File appDir = new File(cache.getParent());
    if (appDir.exists()) {
      final String[] children = appDir.list();
      for (String s : children) {
        if (!"lib".equals(s)) {
          deleteDir(new File(appDir, s));
          Log.i(TAG, "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
        }
      }
    }
  }
}
