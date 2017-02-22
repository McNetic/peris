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

/**
 * Class: Eula
 * Author: Brandon A. Stecklein
 * Version: 1.0
 * Prerequisites: eula.txt in res/raw directory
 * Description: Displays an End User License Agreement.
 * <p>
 * Usage Example
 * -------------
 * Eula.showEula(this);
 * <p>
 * Change Log
 * ----------
 * v1.0
 * 1/13/2011
 * Initial Release
 * Modified from Eula.java class by Android Open Source Project
 */


/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Displays an EULA ("End User License Agreement") that the user has to accept before
 * using the application. Your application should call {@link Eula#showEula(android.app.Activity)}
 * in the onCreate() method of the first activity. If the user accepts the EULA, it will never
 * be shown again. If the user refuses, {@link android.app.Activity#finish()} is invoked
 * on your activity.
 */
final class Eula {
  private static final String TAG = Eula.class.getName();
  private static final String PREFERENCE_EULA_ACCEPTED = "eula.accepted";
  private static final String PREFERENCES_EULA = "eula";

  private Eula() {
  }

  /**
   * Displays the EULA if necessary. This method should be called from the onCreate()
   * method of your main Activity.
   *
   * @param activity The Activity to finish if the user rejects the EULA.
   */
  static void showEula(final Activity activity) {
    final SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES_EULA,
        Activity.MODE_PRIVATE);
    if (!preferences.getBoolean(PREFERENCE_EULA_ACCEPTED, false)) {
      final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder.setTitle("End User License Agreement");
      builder.setCancelable(true);
      builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
        public void onClick(final DialogInterface dialog, final int which) {
          accept(preferences);
        }
      });
      builder.setNegativeButton("Refuse", new DialogInterface.OnClickListener() {
        public void onClick(final DialogInterface dialog, final int which) {
          refuse(activity);
        }
      });
      builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
        public void onCancel(final DialogInterface dialog) {
          refuse(activity);
        }
      });
      // UNCOMMENT TO ENABLE EULA
      builder.setMessage(readFile(activity, R.raw.eula));
      builder.create().show();
    }
  }

  private static void accept(final SharedPreferences preferences) {
    preferences.edit().putBoolean(PREFERENCE_EULA_ACCEPTED, true).commit();
  }

  private static void refuse(final Activity activity) {
    activity.finish();
  }

  static void showDisclaimer(final Activity activity) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setMessage(readFile(activity, R.raw.eula));
    builder.setCancelable(true);
    builder.setTitle("End User License Agreement");
    builder.setPositiveButton("Accept", null);
    builder.create().show();
  }

  private static CharSequence readFile(final Activity activity, final int id) {
    BufferedReader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(
          activity.getResources().openRawResource(id)));
      String line;
      final StringBuilder buffer = new StringBuilder();
      while ((line = in.readLine()) != null) {
        buffer.append(line).append('\n');
      }
      return buffer;
    } catch (IOException e) {
      return "";
    } finally {
      closeStream(in);
    }
  }

  /**
   * Closes the specified stream.
   *
   * @param stream The stream to close.
   */
  private static void closeStream(final   Closeable stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (IOException e) {
        Log.d(TAG, e.getMessage());
      }
    }
  }
}
