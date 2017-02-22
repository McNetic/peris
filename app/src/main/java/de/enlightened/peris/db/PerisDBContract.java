/*
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

package de.enlightened.peris.db;

import android.provider.BaseColumns;

public final class PerisDBContract {
  private PerisDBContract() { }

  public static class ServerEntry implements BaseColumns {
    public static final String TABLE_NAME =           "server";
    public static final String COLUMN_HTTPS =         "https";
    public static final String COLUMN_NAME =          "name";
    public static final String COLUMN_ADDRESS =       "address";
    public static final String COLUMN_ICONURL =       "iconurl";
    public static final String COLUMN_COLOR =         "color";
    public static final String COLUMN_USERNAME =      "username";
    public static final String COLUMN_PASSWORD =      "password";
    public static final String COLUMN_USERID =        "userid";
    public static final String COLUMN_AVATAR =        "avatar";
    public static final String COLUMN_POSTCOUNT =     "postcount";
    public static final String COLUMN_THEME_INT =     "theme_int";
    public static final String COLUMN_COOKIE_COUNT =  "cookie_count";
    public static final String COLUMN_LAST_TAB =      "last_tab";
    public static final String COLUMN_TAGLINE =       "tagline";
    public static final String COLUMN_CHAT_THREAD =   "chat_thread";
    public static final String COLUMN_CHAT_FORUM =    "chat_forum";
    public static final String COLUMN_CHAT_NAME =     "chat_name";
    public static final String COLUMN_BACKGROUND =    "background";
    public static final String COLUMN_BOXCOLOR =      "boxcolor";
    public static final String COLUMN_BOXBORDER =     "boxborder";
    public static final String COLUMN_TEXTCOLOR =     "textcolor";
    public static final String COLUMN_DIVIDERCOLOR =  "dividercolor";
    public static final String COLUMN_WALLPAPER =     "wallpaper";
    public static final String COLUMN_FFCHAT =        "ffchat";
    public static final String COLUMN_ANALYTICS =     "analytics";
    public static final String COLUMN_MOBFOX =        "mobfox";
  }

  public static class MessageNotificationEntry implements BaseColumns {
    public static final String TABLE_NAME           = "notified_message";
    public static final String COLUMN_ID_SERVER     = "id_server";
    public static final String COLUMN_MESSAGE_ID    = "message_id";
  }
}
