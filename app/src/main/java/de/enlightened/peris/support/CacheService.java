/**
 * Copyright (C) 2017 Nicolai Ehemann
 * <p>
 * This file is part of Peris.
 * <p>
 * Peris is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Peris is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Peris.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.enlightened.peris.support;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class CacheService {

  private static final String TAG = CacheService.class.getName();

  private CacheService() { }

  public static void writeObject(
      final Context context,
      final String key,
      final Serializable object) throws IOException {
    final File file = new File(context.getCacheDir(), key);
    final File directory = new File(file.getParent());
    if (!directory.exists()) {
      directory.mkdirs();
    }
    final FileOutputStream fos = new FileOutputStream(file);
    final ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(object);
    oos.close();
    fos.close();
  }

  public static Object readObject(final Context context, final String key) {
    FileInputStream fis = null;
    ObjectInputStream ois = null;
    Object object;

    try {
      fis = new FileInputStream(new File(context.getCacheDir(), key));
      ois = new ObjectInputStream(fis);
      object = ois.readObject();
    } catch (ClassNotFoundException | IOException e) {
      object = null;
    } finally {
      if (ois != null) {
        try {
          ois.close();
        } catch (IOException e) {
          Log.d(TAG, e.getMessage());
        }
      }
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          Log.d(TAG, e.getMessage());
        }
      }
    }
    return object;
  }
}
