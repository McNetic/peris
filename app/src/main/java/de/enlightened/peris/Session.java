package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;
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

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

@SuppressLint({"NewApi", "TrulyRandom"})
public class Session {

  private static final int MAX_ITEM_COUNT = 50;
  /*
     *  Forum System Reference
     *  ----------------------
     *  0 - Unknown
     *  1 - phpBB
     *  2 - MyBB
     *  3 - vBulletin
     */
  private int forumSystem = 0;
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
  private SQLiteDatabase notetasticDB;
  private String sql;
  private String avatarSubmissionName = "uploadfile";
  private boolean allowRegistration = false;
  private XMLRPCClient newClient;
  private PerisApp application;
  private SessionListener sessionListener = null;

  public Session(final Context c, final PerisApp app) {
    this.context = c;
    this.application = app;
    this.sessionId = new Date().getTime();

    Log.i("Peris", "*** NEW SESSION (" + this.sessionId + ") ***");
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

    this.notetasticDB = this.context.openOrCreateDatabase("peris", Context.MODE_PRIVATE, null);

    final String cleanId = DatabaseUtils.sqlEscapeString(server.serverId);
    final String cleanUserid = DatabaseUtils.sqlEscapeString(server.serverUserId);
    final String cleanUsername = DatabaseUtils.sqlEscapeString(server.serverUserName);
    final String cleanPassword = DatabaseUtils.sqlEscapeString(server.serverPassword);
    final String cleanTagline = DatabaseUtils.sqlEscapeString(server.serverTagline);
    final String cleanAvatar = DatabaseUtils.sqlEscapeString(server.serverAvatar);
    final String cleanPostcount = DatabaseUtils.sqlEscapeString(server.serverPostcount);
    final String cleanColor = DatabaseUtils.sqlEscapeString(server.serverColor);
    final String cleanCookies = DatabaseUtils.sqlEscapeString(server.serverCookies);
    final String cleanTheme = DatabaseUtils.sqlEscapeString(server.serverTheme);
    final String cleanTab = DatabaseUtils.sqlEscapeString(server.serverTab);
    final String cleanChatThread = DatabaseUtils.sqlEscapeString(server.chatThread);
    final String cleanChatForum = DatabaseUtils.sqlEscapeString(server.chatForum);
    final String cleanChatName = DatabaseUtils.sqlEscapeString(server.chatName);
    final String cleanIcon = DatabaseUtils.sqlEscapeString(server.serverIcon);
    final String cleanname = DatabaseUtils.sqlEscapeString(server.serverName);
    final String cleanBackground = DatabaseUtils.sqlEscapeString(server.serverBackground);

    final String cleanBoxColor = DatabaseUtils.sqlEscapeString(server.serverBoxColor);
    final String cleanBoxBorder = DatabaseUtils.sqlEscapeString(server.serverBoxBorder);
    final String cleanTextColor = DatabaseUtils.sqlEscapeString(server.serverTextColor);
    final String cleanDividerColor = DatabaseUtils.sqlEscapeString(server.serverDividerColor);
    final String cleanWallpaper = DatabaseUtils.sqlEscapeString(server.serverWallpaper);

    final String cleanFFChat = DatabaseUtils.sqlEscapeString(server.ffChatId);

    final String cleanAnalytics = DatabaseUtils.sqlEscapeString(server.analyticsId);
    final String cleanMobfox = DatabaseUtils.sqlEscapeString(server.mobfoxId);

    this.sql = "update accountlist set color = " + cleanColor + ", username = " + cleanUsername + ", password = " + cleanPassword + ", userid = " + cleanUserid + ", avatar = " + cleanAvatar + ", postcount = " + cleanPostcount + ", themeInt = " + cleanTheme + ", cookieCount = " + cleanCookies + ", lastTab = " + cleanTab + ", tagline = " + cleanTagline + ", chatThread = " + cleanChatThread + ", chatForum = " + cleanChatForum + ", chatName = " + cleanChatName + ", icon = " + cleanIcon + ", servername = " + cleanname + ", background = " + cleanBackground + ", boxcolor = " + cleanBoxColor + ", boxborder = " + cleanBoxBorder + ", textcolor = " + cleanTextColor + ", dividercolor = " + cleanDividerColor + ", wallpaper = " + cleanWallpaper + ", ffchat = " + cleanFFChat + ", analytics = " + cleanAnalytics + ", mobfox = " + cleanMobfox + " where _id = " + cleanId + ";";

    try {
      this.notetasticDB.execSQL(this.sql);
    } catch (Exception ex) {
      Log.d("Peris", ex.getMessage());
      //fuck it for now
    }
    this.notetasticDB.close();
  }

  public final void updateServer() {
    if (this.currentServer == null) {
      return;
    }
    this.updateSpecificServer(this.currentServer);
  }

  public final void setSessionListener(final SessionListener l) {
    this.sessionListener = l;
  }

  @SuppressWarnings("rawtypes")
  public final Object performSynchronousCall(final String method, final Vector parms) {
    return this.performNewSynchronousCall(method, parms);
  }

  @SuppressLint("TrulyRandom")
  @SuppressWarnings("rawtypes")
  public final Object performNewSynchronousCall(final String method, final Vector parms) {

    Log.d("Peris", "Performing New Server Call: Method = " + method);
    try {
      final Object[] parmsobject = new Object[parms.size()];
      for (int i = 0; i < parms.size(); i++) {
        parmsobject[i] = parms.get(i);
      }

      if (this.newClient == null) {

        if (this.currentServer.serverAddress.contains("https")) {
          this.sc = SSLContext.getInstance("SSL");
          this.sc.init(null, this.trustAllCerts, new java.security.SecureRandom());
          HttpsURLConnection.setDefaultSSLSocketFactory(this.sc.getSocketFactory());
          HttpsURLConnection.setDefaultHostnameVerifier(this.hv);
        }
        this.newClient = new XMLRPCClient(new URL(this.currentServer.serverAddress + "/mobiquo/mobiquo.php"), XMLRPCClient.FLAGS_ENABLE_COOKIES);
      }

      return this.newClient.call(method, parmsobject);

    } catch (XMLRPCServerException ex) {
      Log.e(this.context.getString(R.string.app_name), "Error with tapatalk call er1: " + method);
      if (ex.getMessage() != null) {
        Log.e(this.context.getString(R.string.app_name), ex.getMessage());
      } else {
        Log.e(this.context.getString(R.string.app_name), "(no message available)");
      }
    } catch (XMLRPCException ex) {
      Log.e(this.context.getString(R.string.app_name), "Error with tapatalk call er2: " + method);
      if (ex.getMessage() != null) {
        Log.e(this.context.getString(R.string.app_name), ex.getMessage());
      } else {
        Log.e(this.context.getString(R.string.app_name), "(no message available)");
      }
    } catch (Exception ex) {
      Log.e(this.context.getString(R.string.app_name), "Error with tapatalk call er3: " + method);
      if (ex.getMessage() != null) {
        Log.e(this.context.getString(R.string.app_name), ex.getMessage());
      } else {
        Log.e(this.context.getString(R.string.app_name), "(no message available)");
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
          Log.i("Peris", "Method Success = " + method + " (Authenticated)");
        } else {
          Log.i("Peris", "Method Success = " + method + " (NOT Authenticated)");
        }

    } catch(Exception ex) {
      Log.e("Peris", "Method Fail = " + method);

      if(ex.getMessage() != null) {
        Log.e("Peris", ex.getMessage());
      }
    }

    return returnObject;
    */
  }

  public final boolean getAllowRegistration() {
    return this.allowRegistration;
  }

  public final int getForumSystem() {
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

      Log.i("Peris", "Attempting u:" + currentServer.serverUserName + "    " + "p:" + currentServer.serverPassword);

      try {
        final Vector paramz = new Vector();
        paramz.addElement(currentServer.serverUserName.getBytes());
        paramz.addElement(currentServer.serverPassword.getBytes());

        result[0] = performSynchronousCall("login", paramz);

      } catch (Exception ex) {
        Log.d("Peris", ex.getMessage());
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
              Log.i(context.getString(R.string.app_name), "User login_name is " + loginName);
            } else {
              Log.e(context.getString(R.string.app_name), "Server provides no login_name information!");
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
                Log.i(context.getString(R.string.app_name), "Use can view and edit profiles!");
              } else {
                Log.e(context.getString(R.string.app_name), "Use can NOT view or edit profiles!");
              }
            } else {
              Log.e(context.getString(R.string.app_name), "Server provides no profile permission information!");
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
        Log.d("Peris", ex.getMessage());
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
        Log.e("Peris", "Fetching Configuration Failed!");
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

          Log.i(context.getString(R.string.app_name), "Forum system code is: " + system);

          if (system.contains("pb")) {
            forumSystem = 1;
            avatarSubmissionName = "uploadfile";
            Log.i(context.getString(R.string.app_name), "Forum is phpBB");
          }
          if (system.contains("mb")) {
            forumSystem = 2;
            avatarSubmissionName = "avatarupload";
            Log.i(context.getString(R.string.app_name), "Forum is MyBB");
          }
          if (system.contains("vb")) {
            forumSystem = 3;
            avatarSubmissionName = "upload";
            Log.i(context.getString(R.string.app_name), "Forum is vBulletin");
          }

        } else {
          Log.e(context.getString(R.string.app_name), "Server returned no system information!");
          if (result[0] != null) {
            Log.e(context.getString(R.string.app_name), result[0].toString());
          }
        }

        // see if in-app registration is allowed
        if (map.containsKey("inappreg")) {
          final String regKey = (String) map.get("inappreg");
          Log.i(context.getString(R.string.app_name), "Forum inappreg code is: " + regKey);

          if (regKey.contentEquals("1")) {
            allowRegistration = true;
          }
        }
      } else {
        Log.e(context.getString(R.string.app_name), "Unable to fetch configuration data!");
        return;
      }
    }
  }

}
