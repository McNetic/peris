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

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import de.enlightened.peris.db.PerisDBHelper;


public class PerisApp extends Application {

  private static final String TAG = PerisApp.class.getName();;
  private boolean active = false;
  private BackStackManager stackManager;
  private AnalyticsHelper ah;
  private Session session;
  private int backStackid;
  private boolean forceRefresh = false;
  private PerisDBHelper dbHelper;

  @Override
  public void onCreate() {
    super.onCreate();
    this.dbHelper = new PerisDBHelper(this);
    this.stackManager = new BackStackManager();
    this.backStackid = this.stackManager.createBackstack();
    final SharedPreferences appPreferences = getSharedPreferences("prefs", 0);
    final boolean cleanClose = appPreferences.getBoolean("ff_clean_close", true);
    this.ah = new AnalyticsHelper(this, getString(R.string.analytics_app_tracker), getString(R.string.app_name));

    final DisplayImageOptions options = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .bitmapConfig(Bitmap.Config.RGB_565)
        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
        .build();
    final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(options).build();
    ImageLoader.getInstance().init(config);

    if (!cleanClose) {
      Log.d(TAG, "Bad shutdown detected, clearing image cache.");
      ImageLoader.getInstance().clearDiskCache();
      ImageLoader.getInstance().clearMemoryCache();
    }

    final Editor editor = appPreferences.edit();
    editor.putBoolean("ff_clean_close", false);
    editor.commit();
  }

  @Override
  public void onTerminate() {
    this.dbHelper.close();
    super.onTerminate();
  }

  public boolean isActive() {
    return this.active;
  }

  public void setActive(final boolean active) {
    this.active = active;
  }

  public BackStackManager getStackManager() {
    return this.stackManager;
  }

  public AnalyticsHelper getAnalyticsHelper() {
    return this.ah;
  }

  public void freshBackstack() {
    this.stackManager.clearAllStacks();
    this.backStackid = this.stackManager.createBackstack();
  }

  public int getBackStackId() {
    return this.backStackid;
  }

  public void initSession() {
    this.session = new Session(this, this, this.dbHelper);
  }

  public Session getSession() {
    if (this.session == null) {
      this.session = new Session(this, this, this.dbHelper);
    }
    return this.session;
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    //TODO: onTrim
  }

  public void sendLoginStat(final String address) {
    //new SendLoginStatTask().execute(address);
  }

  public boolean getForceRefresh() {
    return this.forceRefresh;
  }

  public void setForceRefresh(final boolean value) {
    this.forceRefresh = value;
  }

  private class SendLoginStatTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(final String... params) {
      final HttpClient httpclient = new DefaultHttpClient();
      final HttpPost httppost = new HttpPost("http://loginstaturl");

      try {
        final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("server_address", params[0]));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        httpclient.execute(httppost);
      } catch (Exception e) {
        Log.d(TAG, e.getMessage());
      }

      return "";
    }

    protected void onPostExecute(final String result) {
      return;
    }
  }
}
