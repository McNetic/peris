package de.enlightened.peris.support;

import android.net.Uri;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Nicolai Ehemann on 02.02.2017.
 */

public final class Net {
  private Net() {
  }

  public static boolean checkURL(final String urlString) {
    try {
      return checkURL(new URL(urlString));
    } catch (MalformedURLException e) {
      Log.d("Peris", "Bad URL " + urlString);
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
        Log.d("Peris", e.getMessage());
      } else {
        Log.d("Peris", "Header connection error");
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
