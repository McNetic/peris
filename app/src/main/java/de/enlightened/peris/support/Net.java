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

import android.net.Uri;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public final class Net {

  private static final String TAG = Net.class.getName();;

  private Net() {
  }

  public static boolean checkURL(final String urlString) {
    try {
      return checkURL(new URL(urlString));
    } catch (MalformedURLException e) {
      Log.d(TAG, "Bad URL " + urlString);
    }
    return false;
  }

  public static boolean checkURL(final URL url) {
    try {
      final HttpURLConnection huc = (HttpURLConnection) url.openConnection();
      huc.setRequestMethod("GET");
      huc.setInstanceFollowRedirects(false);
      huc.connect();
      if (huc.getResponseCode() == HttpURLConnection.HTTP_OK) {
        return true;
      }
    } catch (Exception e) {
      if (e.getMessage() != null) {
        Log.d(TAG, e.getMessage());
      } else {
        Log.d(TAG, "Header connection error");
      }
    }
    return false;
  }

  public static boolean isUrl(final String urlString) {
    return urlString != null && urlString.startsWith("http://") || urlString.startsWith("https://");
  }

  public static String removeProtocol(final String urlString) {
    final String rv;

    if (urlString.startsWith("http://")) {
      rv = urlString.substring(7);
    } else if (urlString.startsWith("https://")) {
      rv = urlString.substring(8);
    } else {
      rv = urlString;
    }
    return rv;
  }

  public static Uri uriFromURL(final URL url) {
    return new Uri.Builder()
        .scheme(url.getProtocol())
        .authority(url.getAuthority())
        .appendPath(url.getPath())
        .build();
  }
}
