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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.enlightened.peris.db.PerisDBContract.MessageNotificationEntry;
import de.enlightened.peris.db.PerisDBContract.ServerEntry;

public class PerisDBHelper extends SQLiteOpenHelper {
  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "peris.db";

  public PerisDBHelper(final Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
  public void onCreate(final SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + ServerEntry.TABLE_NAME + " ("
        + ServerEntry._ID                 + " INTEGER PRIMARY KEY, "
        + ServerEntry.COLUMN_ADDRESS + " TEXT, "
        + ServerEntry.COLUMN_NAME         + " TEXT, "
        + ServerEntry.COLUMN_HTTPS        + " INTEGER NOT NULL CHECK (" + ServerEntry.COLUMN_HTTPS + " IN (0,1)), "
        + ServerEntry.COLUMN_ICONURL      + " TEXT, "
        + ServerEntry.COLUMN_COLOR        + " TEXT, "
        + ServerEntry.COLUMN_USERNAME     + " TEXT, "
        + ServerEntry.COLUMN_PASSWORD     + " TEXT, "
        + ServerEntry.COLUMN_USERID       + " TEXT, "
        + ServerEntry.COLUMN_AVATAR       + " TEXT, "
        + ServerEntry.COLUMN_POSTCOUNT    + " TEXT, "
        + ServerEntry.COLUMN_THEME_INT    + " TEXT, "
        + ServerEntry.COLUMN_COOKIE_COUNT + " TEXT, "
        + ServerEntry.COLUMN_LAST_TAB     + " TEXT, "
        + ServerEntry.COLUMN_TAGLINE      + " TEXT, "
        + ServerEntry.COLUMN_CHAT_THREAD  + " TEXT, "
        + ServerEntry.COLUMN_CHAT_FORUM   + " TEXT, "
        + ServerEntry.COLUMN_CHAT_NAME    + " TEXT, "
        + ServerEntry.COLUMN_BACKGROUND   + " TEXT, "
        + ServerEntry.COLUMN_BOXCOLOR     + " TEXT, "
        + ServerEntry.COLUMN_BOXBORDER    + " TEXT, "
        + ServerEntry.COLUMN_TEXTCOLOR    + " TEXT, "
        + ServerEntry.COLUMN_DIVIDERCOLOR + " TEXT, "
        + ServerEntry.COLUMN_WALLPAPER    + " TEXT, "
        + ServerEntry.COLUMN_FFCHAT       + " TEXT, "
        + ServerEntry.COLUMN_ANALYTICS    + " TEXT, "
        + ServerEntry.COLUMN_MOBFOX       + " TEXT "
        + ")");

    db.execSQL("CREATE TABLE " + MessageNotificationEntry.TABLE_NAME + " ("
            + MessageNotificationEntry._ID                 + " INTEGER PRIMARY KEY, "
            + MessageNotificationEntry.COLUMN_ID_SERVER + " TEXT, "
            + MessageNotificationEntry.COLUMN_MESSAGE_ID + " INTEGER "
        + ")");
  }

  public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
  }

  public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
  }
}
