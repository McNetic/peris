package de.enlightened.peris.support;

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
      final URL url = new URL(urlString);
      final HttpURLConnection huc = (HttpURLConnection) url.openConnection();
      huc.setRequestMethod("GET");
      huc.setInstanceFollowRedirects(false);
      huc.connect();
      if (huc.getResponseCode() == HttpURLConnection.HTTP_OK) {
        return true;
      }
    } catch (MalformedURLException e) {
      Log.d("Peris", "Bad URL " + urlString);
    } catch (Exception e) {
      if (e.getMessage() != null) {
        Log.d("Peris", e.getMessage());
      } else {
        Log.d("Peris", "Header connection error");
      }
    }
    return false;
  }
}
