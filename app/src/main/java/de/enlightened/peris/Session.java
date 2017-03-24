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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Vector;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.enlightened.peris.api.ApiResult;
import de.enlightened.peris.api.Tapatalk;
import de.enlightened.peris.db.PerisDBHelper;
import de.enlightened.peris.db.ServerRepository;
import de.enlightened.peris.site.Config;
import de.enlightened.peris.site.Identity;

@SuppressLint({"NewApi", "TrulyRandom"})
public class Session {

  private static final String TAG = Session.class.getName();
  private static final int MAX_ITEM_COUNT = 50;
  private final PerisDBHelper borrowedDbHelper;
  private final Tapatalk api;
  private long sessionId;
  // Install the all-trusting trust manager
  private SSLContext sc;
  // Create empty HostnameVerifier
  private HostnameVerifier hv = new HostnameVerifier() {
    public boolean verify(final String hostname, final SSLSession session) {
      return true;
    }
  };
  private TrustManager[] trustAllCerts = new TrustManager[] {
      new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
          return null;
        }
        public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
          // Trust always
        }
        public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
          // Trust always
        }
      },
  };
  private Context context;
  private Server currentServer;
  private PerisApp application;
  private SessionListener sessionListener = null;

  public Session(final Context c, final PerisApp app, final PerisDBHelper dbHelper) {
    this.context = c;
    this.borrowedDbHelper = dbHelper;
    this.api = new Tapatalk();
    this.application = app;
    this.sessionId = new Date().getTime();

    Log.i(TAG, "*** NEW SESSION (" + this.sessionId + ") ***");
  }

  public Tapatalk getApi() {
    return this.api;
  }

  public final Server getServer() {

    if (this.currentServer == null) {
      this.currentServer = new Server();
      this.currentServer.serverColor = this.context.getString(R.string.default_color);
      this.currentServer.serverTheme = this.context.getString(R.string.default_theme);
    }
    return this.currentServer;
  }

  public final void setServer(final Server server) {
    this.api.setServer(server);
    this.currentServer = server;

    if (server.serverTagline.contentEquals("[*WEBVIEW*]")) {
      return;
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      new FetchForumConfigurationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      new FetchForumConfigurationTask().execute();
    }
  }

  public final void loginSession(final String username, final String password) {
    this.currentServer.serverUserName = username;
    this.currentServer.serverPassword = password;

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      new ConnectSessionTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      new ConnectSessionTask().execute();
    }
  }

  public final void logOutSession() {
    if (this.currentServer == null) {
      return;
    }

    this.currentServer.serverUserId = null;
    this.currentServer.serverUserName = "0";
    this.currentServer.serverPassword = "0";
    this.currentServer.serverPostcount = 0;
    this.currentServer.serverTab = "0";
    this.currentServer.serverAvatar = "0";

    this.updateServer();
  }

  public final void refreshLogin() {
    if (this.currentServer != null) {
      if (this.currentServer.serverUserId != null) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          new ConnectSessionTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
          new ConnectSessionTask().execute();
        }
      }
    }
  }

  public final void updateSpecificServer(final Server server) {
    ServerRepository.update(this.borrowedDbHelper.getWritableDatabase(), server);
  }

  public final void updateServer() {
    if (this.currentServer != null) {
      this.updateSpecificServer(this.currentServer);
    }
  }

  public final void setSessionListener(final SessionListener l) {
    this.sessionListener = l;
  }

  @SuppressLint("TrulyRandom")
  @SuppressWarnings("rawtypes")
  public final Object performSynchronousCall(final String method, final Vector parms) {

    Log.d(TAG, "Performing Server Call: Method = " + method + " (URL: " + this.currentServer.getTapatalkURL() + ")");
    try {
      final Object[] parmsobject = new Object[parms.size()];
      for (int i = 0; i < parms.size(); i++) {
        parmsobject[i] = parms.get(i);
      }

      //TODO: when removing this, make method private
      return this.getApi().getXMLRPCClient().call(method, parmsobject);
    } catch (Exception ex) {
      String.format("Tapatalk call error (%s) : %s", ex.getClass().getName(), method);
      if (ex.getMessage() != null) {
        Log.e(TAG, ex.getMessage());
      } else {
        Log.e(TAG, "(no message available)");
      }
    }
    return null;

    /*

    Object returnObject = null;

    try {

      CookieManager cookiemanager = new CookieManager();
        cookiemanager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookiemanager);
        cookiemanager.getCookieStore().removeAll();

        if(authenticatedSession) {

          for(HttpCookie c:getCookies()) {
            try {
              URI cookieUri = new URI(c.getDomain());
              cookiemanager.getCookieStore().add(cookieUri, c);
            } catch(Exception ex) {
              //nobody cares
            }
          }
        }

        if(client == null) {
          XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

        config.setUserAgent("Peris");
        config.setConnectionTimeout(24000);
        config.setReplyTimeout(24000);
          config.setServerURL(new URL(currentServer.serverAddress + "/mobiquo/mobiquo.php"));

          client = new XmlRpcClient();
          client.setConfig(config);

        }

        cookiemanager.getCookieStore();

        XmlRpcTransportFactory tFactory = new XmlRpcSun15HttpTransportFactory(client);

        client.setTransportFactory(tFactory);

        returnObject = client.execute(method, parms);

        if(method.contentEquals("login")) {
          CookieStore theStore = cookiemanager.getCookieStore();
          sessionCookies = theStore.getCookies();
          authenticatedSession = true;
        }

        if(authenticatedSession) {
          Log.i(TAG, "Method Success = " + method + " (Authenticated)");
        } else {
          Log.i(TAG, "Method Success = " + method + " (NOT Authenticated)");
        }

    } catch(Exception ex) {
      Log.e(TAG, "Method Fail = " + method);

      if(ex.getMessage() != null) {
        Log.e(TAG, ex.getMessage());
      }
    }

    return returnObject;
    */
  }

  public interface SessionListener {
    void onSessionConnected();
    void onSessionConnectionFailed(final String reason);
  }

  private class ConnectSessionTask extends AsyncTask<String, Void, ApiResult> {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected ApiResult doInBackground(final String... params) {
      Log.i(TAG, String.format("Attempting u:%s    p:%s[...]", Session.this.currentServer.serverUserName, Session.this.currentServer.serverPassword.substring(0, 1)));
      return Session.this.getApi().login(Session.this.currentServer.serverUserName, Session.this.currentServer.serverPassword);
    }

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis", "checkstyle:nestedifdepth"})
    protected void onPostExecute(final ApiResult result) {
      if (!result.isSuccess()) {
        if (sessionListener != null) {
          sessionListener.onSessionConnectionFailed(result.getMessage());
        }
      } else {
        final Identity identity = Session.this.getApi().getIdentity();
        if (currentServer.analyticsId != null && !currentServer.analyticsId.contentEquals("0")) {
          application.getAnalyticsHelper().trackCustomEvent(currentServer.analyticsId, "ff_user", "connected", currentServer.serverUserName);
        }
        currentServer.serverUserId = identity.getId();
        currentServer.serverAvatar = identity.getAvatarUrl();
        currentServer.serverPostcount = identity.getPostCount();

        if (sessionListener != null) {
          sessionListener.onSessionConnected();
        }
        updateServer();
      }
    }
  }

  private class FetchForumConfigurationTask extends AsyncTask<String, Void, Config> {

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    @Override
    protected Config doInBackground(final String... params) {
      return Session.this.getApi().getConfig();
    }

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    protected void onPostExecute(final Config config) {
      /*  THIS MAY HAVE TO BE MOVED IN FUTURE, TO INSURE
       *  THAT WE HAVE CONFIGURATION SUCCESSFULLY BEFORE
       *  ATTEMPTING TO LOG IN!
       */
      if (config == null) {
        sessionListener.onSessionConnectionFailed("Failed to get server config");
      } else if (currentServer.serverUserName.contentEquals("0")) {
        sessionListener.onSessionConnected();
      } else {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          new ConnectSessionTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
          new ConnectSessionTask().execute();
        }
      }
    }
  }

}
