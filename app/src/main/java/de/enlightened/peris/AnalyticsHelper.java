package de.enlightened.peris;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("checkstyle:requirethis")
public class AnalyticsHelper {

  private String analyticsId;
  private String appName;

  private Context context;

  private String uniqueID;

  public AnalyticsHelper(final Context c, final String analytics, final String name) {
    this.context = c;
    this.analyticsId = analytics;
    this.appName = name;

    final SharedPreferences appPreferences = this.context.getSharedPreferences("prefs", 0);
    this.uniqueID = appPreferences.getString("analytics_uuid", "0");

    if (this.uniqueID.contentEquals("0")) {
      this.uniqueID = UUID.randomUUID().toString();
      final SharedPreferences.Editor editor = appPreferences.edit();
      editor.putString("analytics_uuid", this.uniqueID);
      editor.commit();
    }


  }

  public final void trackScreen(final String name, final boolean global) {
    new LogAnalyticsViewTask().execute(name);
  }

  public final void trackCustomScreen(final String analytics, final String name) {
    new LogAnalyticsViewTask().execute(name);

  }

  public final void trackCustomEvent(final String analytics, final String cat, final String act, final String lab) {
    new LogAnalyticsEvent().execute(cat, act, lab, analytics);
  }

  public final void trackEvent(final String cat, final String act, final String lab, final boolean global) {
    new LogAnalyticsEvent().execute(cat, act, lab);
  }

  private class LogAnalyticsViewTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(final String... params) {
      String rv;
      //TODO: re-enable or remove
      if (false) {
        final HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost;

        final String viewName = params[0];

        String viewId = null;

        if (params.length > 1) {
          viewId = params[1];
        }

        if (viewId == null) {
          viewId = analyticsId;
        }

        httppost = new HttpPost("http://www.google-analytics.com/collect");

        try {
          final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
          nameValuePairs.add(new BasicNameValuePair("v", "1"));
          nameValuePairs.add(new BasicNameValuePair("tid", viewId));
          nameValuePairs.add(new BasicNameValuePair("cid", uniqueID));
          nameValuePairs.add(new BasicNameValuePair("t", "appview"));
          nameValuePairs.add(new BasicNameValuePair("an", appName));
          nameValuePairs.add(new BasicNameValuePair("av", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName));
          nameValuePairs.add(new BasicNameValuePair("cd", viewName));
          httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

          final ResponseHandler<String> responseHandler = new BasicResponseHandler();
          final String response = httpclient.execute(httppost, responseHandler);
          rv = response;

        } catch (Exception e) {
          rv = "fail";
        }
      }
      rv = "fail";
      return rv;
    }

    protected void onPostExecute(final String result) {

      // yay
    }

  }

  private class LogAnalyticsEvent extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(final String... params) {
      final HttpClient httpclient = new DefaultHttpClient();
      HttpPost httppost;

      final String eventCat = params[0];
      final String eventAct = params[1];
      final String eventLab = params[2];
      String eventId = null;

      if (params.length > 3) {
        eventId = params[3];
      }

      if (eventId == null) {
        eventId = analyticsId;
      }

      httppost = new HttpPost("http://www.google-analytics.com/collect");

      try {
        final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("v", "1"));
        nameValuePairs.add(new BasicNameValuePair("tid", eventId));
        nameValuePairs.add(new BasicNameValuePair("cid", uniqueID));
        nameValuePairs.add(new BasicNameValuePair("t", "event"));
        nameValuePairs.add(new BasicNameValuePair("ec", eventCat));
        nameValuePairs.add(new BasicNameValuePair("ea", eventAct));
        nameValuePairs.add(new BasicNameValuePair("el", eventLab));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        final ResponseHandler<String> responseHandler = new BasicResponseHandler();
        final String response = httpclient.execute(httppost, responseHandler);
        return response;

      } catch (Exception e) {
        return "fail";
      }

    }

    protected void onPostExecute(final String result) {

      // yay
    }

  }
}
