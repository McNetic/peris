package de.enlightened.peris.db;

import android.provider.BaseColumns;

/**
 * Created by Nicolai Ehemann on 08.02.2017.
 */

public final class PerisDBContract {
  // To prevent someone from accidentally instantiating the contract class,
  // make the constructor private.
  private PerisDBContract() { }

  /* Inner class that defines the table contents */
  public static class ServerEntry implements BaseColumns {
    public static final String TABLE_NAME = "server";
    public static final String COLUMN_HTTPS = "https";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_ICONURL = "iconurl";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USERID = "userid";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_POSTCOUNT = "postcount";
    public static final String COLUMN_THEME_INT = "theme_int";
    public static final String COLUMN_COOKIE_COUNT = "cookie_count";
    public static final String COLUMN_LAST_TAB = "last_tab";
    public static final String COLUMN_TAGLINE = "tagline";
    public static final String COLUMN_CHAT_THREAD = "chat_thread";
    public static final String COLUMN_CHAT_FORUM = "chat_forum";
    public static final String COLUMN_CHAT_NAME = "chat_name";
    public static final String COLUMN_BACKGROUND = "background";
    public static final String COLUMN_BOXCOLOR = "boxcolor";
    public static final String COLUMN_BOXBORDER = "boxborder";
    public static final String COLUMN_TEXTCOLOR = "textcolor";
    public static final String COLUMN_DIVIDERCOLOR = "dividercolor";
    public static final String COLUMN_WALLPAPER = "wallpaper";
    public static final String COLUMN_FFCHAT = "ffchat";
    public static final String COLUMN_ANALYTICS = "analytics";
    public static final String COLUMN_MOBFOX = "mobfox";
  }
}
