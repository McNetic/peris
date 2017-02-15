package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.enlightened.peris.db.PerisDBHelper;
import de.enlightened.peris.db.ServerRepository;
import de.timroes.axmlrpc.XMLRPCClient;

@SuppressLint({"NewApi", "TrulyRandom"})
public class Session {

  private static final String TAG = Session.class.getName();
  private static final int MAX_ITEM_COUNT = 50;
  private final PerisDBHelper borrowedDbHelper;
  private ForumSystem forumSystem = ForumSystem.UNKNOWN;
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
  private String avatarSubmissionName = "uploadfile";
  private boolean allowRegistration = false;
  private XMLRPCClient newClient;
  private PerisApp application;
  private SessionListener sessionListener = null;

  public Session(final Context c, final PerisApp app, final PerisDBHelper dbHelper) {
    this.context = c;
    this.borrowedDbHelper = dbHelper;
    this.application = app;
    this.sessionId = new Date().getTime();

    Log.i(TAG, "*** NEW SESSION (" + this.sessionId + ") ***");
  }

  public enum ForumSystem {
    UNKNOWN,
    PHPBB,
    MYBB,
    VBULLETIN
  }

  public final String getAvatarName() {
    return this.avatarSubmissionName;
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

  public final Map<String, String> getCookies() {

    if (this.newClient == null) {
      return null;
    }
    return this.newClient.getCookies();
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

    this.currentServer.serverUserId = "0";
    this.currentServer.serverUserName = "0";
    this.currentServer.serverPassword = "0";
    this.currentServer.serverPostcount = "0";
    this.currentServer.serverTab = "0";
    this.currentServer.serverAvatar = "0";

    this.updateServer();
  }

  public final void refreshLogin() {
    if (this.currentServer != null) {
      if (!this.currentServer.serverUserId.contentEquals("0")) {
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

      if (this.newClient == null) {

        if (this.currentServer.serverHttps) {
          this.sc = SSLContext.getInstance("SSL");
          this.sc.init(null, this.trustAllCerts, new java.security.SecureRandom());
          HttpsURLConnection.setDefaultSSLSocketFactory(this.sc.getSocketFactory());
          HttpsURLConnection.setDefaultHostnameVerifier(this.hv);
        }
        this.newClient = new XMLRPCClient(this.currentServer.getTapatalkURL(), XMLRPCClient.FLAGS_ENABLE_COOKIES);
      }

      return this.newClient.call(method, parmsobject);
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

  public final boolean getAllowRegistration() {
    return this.allowRegistration;
  }

  public final ForumSystem getForumSystem() {
    return this.forumSystem;
  }

  public interface SessionListener {
    void onSessionConnected();
    void onSessionConnectionFailed(final String reason);
  }

  private class ConnectSessionTask extends AsyncTask<String, Void, Object[]> {

    @SuppressWarnings({"rawtypes", "unchecked", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final String... params) {

      final Object[] result = new Object[MAX_ITEM_COUNT];

      Log.i(TAG, "Attempting u:" + currentServer.serverUserName + "    " + "p:" + currentServer.serverPassword);

      try {
        final Vector paramz = new Vector();
        paramz.addElement(currentServer.serverUserName.getBytes());
        paramz.addElement(currentServer.serverPassword.getBytes());

        result[0] = performSynchronousCall("login", paramz);

      } catch (Exception ex) {
        Log.d(TAG, ex.getMessage());
      }

      return result;
    }

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis", "checkstyle:nestedifdepth"})
    protected void onPostExecute(final Object[] result) {

      if (result == null) {
        sessionListener.onSessionConnectionFailed("Login Failed.");
      } else if (result[0] != null) {
        final HashMap map = (HashMap) result[0];

        if (map.containsKey("result")) {
          final Boolean loginSuccess = (Boolean) map.get("result");
          if (loginSuccess) {
            // Submit server login stat to forum owners' analytics account
            if (currentServer.analyticsId != null && !currentServer.analyticsId.contentEquals("0")) {
              application.getAnalyticsHelper().trackCustomEvent(currentServer.analyticsId, "ff_user", "connected", currentServer.serverUserName);
            }

            if (map.containsKey("login_name")) {
              final String loginName = new String((byte[]) map.get("login_name"));
              Log.i(TAG, "User login_name is " + loginName);
            } else {
              Log.e(TAG, "Server provides no login_name information!");
            }

            if (map.get("user_id") instanceof Integer) {
              currentServer.serverUserId = Integer.toString((Integer) map.get("user_id"));
            } else {
              currentServer.serverUserId = (String) map.get("user_id");
            }

            if (map.containsKey("icon_url")) {
              currentServer.serverAvatar = (String) map.get("icon_url");
            }

            if (map.get("post_count") != null) {
              currentServer.serverPostcount = Integer.toString((Integer) map.get("post_count"));
            } else {
              currentServer.serverPostcount = "0";
            }

            if (map.containsKey("can_profile")) {
              final boolean canProfile = (Boolean) map.get("can_profile");

              if (canProfile) {
                Log.i(TAG, "Use can view and edit profiles!");
              } else {
                Log.e(TAG, "Use can NOT view or edit profiles!");
              }
            } else {
              Log.e(TAG, "Server provides no profile permission information!");
            }

            if (sessionListener != null) {
              sessionListener.onSessionConnected();
            }
            if (currentServer.serverUserId == null) {
              currentServer.serverUserId = "0";
            }
            updateServer();
          } else {
            if (map.containsKey("result_text")) {
              final String failReason = new String((byte[]) map.get("result_text"));
              sessionListener.onSessionConnectionFailed(failReason);
            } else {
              if (sessionListener != null) {
                sessionListener.onSessionConnectionFailed("Wrong username or password.");
              }
            }
          }
        } else {
          sessionListener.onSessionConnectionFailed("No result key.");
        }
      } else {
        if (sessionListener != null) {
          sessionListener.onSessionConnectionFailed("Login attempt failed.");
        }
      }
    }
  }

  private class FetchForumConfigurationTask extends AsyncTask<String, Void, Object[]> {

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final String... params) {

      final Object[] result = new Object[MAX_ITEM_COUNT];

      try {
        final Vector paramz = new Vector();
        result[0] = performSynchronousCall("get_config", paramz);

      } catch (Exception ex) {
        Log.d(TAG, ex.getMessage());
      }
      return result;
    }

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    protected void onPostExecute(final Object[] result) {
            /*
       *
       *  THIS MAY HAVE TO BE MOVED IN FUTURE, TO INSURE
       *  THAT WE HAVE CONFIGURATION SUCCESSFULLY BEFORE
       *  ATTEMPTING TO LOG IN!
       *
       */
      if (currentServer.serverUserName.contentEquals("0")) {
        sessionListener.onSessionConnected();
      } else {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          new ConnectSessionTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
          new ConnectSessionTask().execute();
        }
      }

      if (result == null) {
        Log.e(TAG, "Fetching Configuration Failed!");
        return;
      }

      //Parse tapatalk api data
      if (result[0] != null) {
        final HashMap map = (HashMap) result[0];

        /*
        if(map.containsKey("api_level")) {
          String api = (String) map.get("api_level");
        }
        */

        if (map.containsKey("version")) {
          final String system = (String) map.get("version");

          Log.i(TAG, "Forum system code is: " + system);

          if (system.contains("pb")) {
            forumSystem = ForumSystem.PHPBB;
            avatarSubmissionName = "uploadfile";
            Log.i(TAG, "Forum is phpBB");
          }
          if (system.contains("mb")) {
            forumSystem = ForumSystem.MYBB;
            avatarSubmissionName = "avatarupload";
            Log.i(TAG, "Forum is MyBB");
          }
          if (system.contains("vb")) {
            forumSystem = ForumSystem.VBULLETIN;
            avatarSubmissionName = "upload";
            Log.i(TAG, "Forum is vBulletin");
          }

        } else {
          Log.e(TAG, "Server returned no system information!");
          if (result[0] != null) {
            Log.e(TAG, result[0].toString());
          }
        }

        // see if in-app registration is allowed
        if (map.containsKey("inappreg")) {
          final String regKey = (String) map.get("inappreg");
          Log.i(TAG, "Forum inappreg code is: " + regKey);

          if (regKey.contentEquals("1")) {
            allowRegistration = true;
          }
        }
      } else {
        Log.e(TAG, "Unable to fetch configuration data!");
        return;
      }
    }
  }

}
