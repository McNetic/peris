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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchSubforumIconTask extends AsyncTask<Object, Void, String> {

  private static final String TAG = FetchSubforumIconTask.class.getName();
  private static final int JPEG_QUALITY = 80;

  private Bitmap bmImg;
  private InputStream is;
  private ImageView secureHolder;
  private String cacheName;
  private String imageLocation;
  private PerisApp app;

  @Override
  protected String doInBackground(final Object... params) {
    this.secureHolder = (ImageView) params[1];
    this.cacheName = (String) params[0];
    this.imageLocation = (String) params[2];
    this.app = (PerisApp) params[3];

    try {
      final BasicCookieStore cStore = new BasicCookieStore();
      final CookieManager cookiemanager = new CookieManager();
      cookiemanager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
      CookieHandler.setDefault(cookiemanager);
      final URL myFileUrl = new URL(this.imageLocation);
      String cookieString = "";

      for (String s : this.app.getSession().getApi().getCookies().keySet()) {
        try {
          final BasicClientCookie aCookie = new BasicClientCookie(s, this.app.getSession().getApi().getCookies().get(s));
          cStore.addCookie(aCookie);
          cookieString = cookieString + s + "=" + this.app.getSession().getApi().getCookies().get(s) + ";";
        } catch (Exception ex) {
          Log.d(TAG, ex.getMessage());
        }
      }

      final HttpContext localContext = new BasicHttpContext();
      localContext.setAttribute(ClientContext.COOKIE_STORE, cStore);
      final HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
      conn.setDoInput(true);
      conn.setRequestProperty("Cookie", cookieString);
      conn.setRequestProperty("Content-Type", "image/*");
      conn.connect();

      this.is = conn.getInputStream();
      this.bmImg = BitmapFactory.decodeStream(this.is);
      return "web";
    } catch (Exception e) {
      if (e.getMessage() != null) {
        Log.e(TAG, "ApeImageCacher: Connection Exception: " + e.getMessage());
      } else {
        Log.e(TAG, "ApeImageCacher: exNull Error Downloading Image!");
      }
    }
    return "fail";
  }

  protected void onPostExecute(final String result) {
    if (result.contentEquals("fail")) {
      //we will just use the default icon, get out of here.
      return;
    }
    //If it it web or cache, we have what we need, set the bitmap
    //Save the image to cache.
    try {
      final File saveDirectory = new File(Environment.getExternalStorageDirectory(), ApeImageCacher.CACHE_DIRECTORY);
      final File file = new File(saveDirectory.getPath() + File.separator + this.cacheName);
      final OutputStream os = new FileOutputStream(file);
      this.bmImg.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, os);
      os.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (this.secureHolder != null && this.bmImg != null) {
      this.secureHolder.setImageBitmap(this.bmImg);
    }
    return;
  }
}
