package de.enlightened.peris;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import de.enlightened.peris.db.DBEntity;

@SuppressWarnings("checkstyle:visibilitymodifier")
public class Server extends DBEntity {

  private static final String TAG = Server.class.getName();;

  public String serverId = "0";
  public String serverAddress = null;
  public boolean serverHttps = true;
  public String serverUserId = null;
  public String serverUserName = "0";
  public String serverPassword = "0";
  public String serverTagline = "null";
  public String serverAvatar = "0";
  public int serverPostcount = 0;
  public String serverColor = "0";
  public String serverCookies = "0";
  public String serverTheme = "0";
  public String serverTab = "0";
  public String chatForum = "0";
  public String chatThread = "0";
  public String chatName = "0";
  public String serverIcon = null;
  public String serverName = "0";
  public String serverBackground = "0";
  public String serverWallpaper = "0";
  public String serverTextColor = "0";
  public String serverBoxColor = "0";
  public String serverBoxBorder = "0";
  public String serverDividerColor = "0";
  public String ffChatId = "0";
  public String analyticsId = "0";
  public String mobfoxId = "0";

  @Override
  public long getId() {
    return Long.parseLong(this.serverId);
  }

  @Override
  public void setId(final long id) {
    super.setId(id);
    this.serverId = Long.toString(id);
  }

  public String getScheme() {
    return this.serverHttps ? "https" : "http";
  }

  public URL getTapatalkURL() {
    return this.getURL("mobiquo/mobiquo.php");
  }

  public URL getUploadURL() {
    return this.getURL("mobiquo/upload.php");
  }

  public URL getURL() {
    return this.getURL("");
  }

  public URL getURL(final String path) {
    try {
      return new URL(this.getScheme(), this.serverAddress, path);
    } catch (MalformedURLException e) {
      Log.d(TAG, e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public String getUrlString() {
    return String.format("%s://%s", this.getScheme(), this.serverAddress);
  }
}
