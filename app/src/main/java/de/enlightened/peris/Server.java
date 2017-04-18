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

import android.net.Uri;
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

  public URL getAvatarURL(final String userId) {
    return this.getURL(String.format("mobiquo/avatar.php?user_id=%s", Uri.encode(userId)));
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
