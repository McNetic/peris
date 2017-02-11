package de.enlightened.peris.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.enlightened.peris.Server;
import de.enlightened.peris.db.PerisDBContract.ServerEntry;

import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_ADDRESS;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_ANALYTICS;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_AVATAR;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_BACKGROUND;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_BOXBORDER;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_BOXCOLOR;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_CHAT_FORUM;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_CHAT_NAME;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_CHAT_THREAD;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_COLOR;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_COOKIE_COUNT;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_DIVIDERCOLOR;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_FFCHAT;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_HTTPS;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_ICONURL;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_LAST_TAB;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_MOBFOX;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_NAME;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_PASSWORD;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_POSTCOUNT;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_TAGLINE;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_TEXTCOLOR;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_THEME_INT;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_USERID;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_USERNAME;
import static de.enlightened.peris.db.PerisDBContract.ServerEntry.COLUMN_WALLPAPER;

/**
 * Created by Nicolai Ehemann on 08.02.2017.
 */

public final class ServerRepository {

  private static final String SELECTION_ONE_BY_ID = String.format("%s = ?", ServerEntry._ID);
  private static final String SELECTION_ONE_BY_ADDRESS = String.format("%s = ?", ServerEntry.COLUMN_ADDRESS);

  private ServerRepository() {
  }

  private static ContentValues getValues(final Server server) {
    final ContentValues values = new ContentValues();
    values.put(COLUMN_ADDRESS, server.serverAddress);
    values.put(COLUMN_HTTPS, server.serverHttps ? 1 : 0);
    values.put(COLUMN_NAME, server.serverName);
    values.put(COLUMN_ICONURL, server.serverIcon);
    values.put(COLUMN_COLOR, server.serverColor);
    values.put(COLUMN_USERNAME, server.serverUserName);
    values.put(COLUMN_PASSWORD, server.serverPassword);
    values.put(COLUMN_USERID, server.serverUserId);
    values.put(COLUMN_AVATAR, server.serverAvatar);
    values.put(COLUMN_POSTCOUNT, server.serverPostcount);
    values.put(COLUMN_THEME_INT, server.serverTheme);
    values.put(COLUMN_COOKIE_COUNT, server.serverCookies);
    values.put(COLUMN_LAST_TAB, server.serverTab);
    values.put(COLUMN_TAGLINE, server.serverTagline);
    values.put(COLUMN_CHAT_THREAD, server.chatThread);
    values.put(COLUMN_CHAT_FORUM, server.chatForum);
    values.put(COLUMN_CHAT_NAME, server.chatName);
    values.put(COLUMN_BACKGROUND, server.serverBackground);
    values.put(COLUMN_BOXCOLOR, server.serverBoxColor);
    values.put(COLUMN_BOXBORDER, server.serverBoxBorder);
    values.put(COLUMN_TEXTCOLOR, server.serverTextColor);
    values.put(COLUMN_DIVIDERCOLOR, server.serverDividerColor);
    values.put(COLUMN_WALLPAPER, server.serverWallpaper);
    values.put(COLUMN_FFCHAT, server.ffChatId);
    values.put(COLUMN_ANALYTICS, server.analyticsId);
    values.put(COLUMN_MOBFOX, server.mobfoxId);
    return values;
  }

  private static String[] getProjection() {
    return new String[] {
        ServerEntry._ID,
        COLUMN_ADDRESS,
        COLUMN_NAME,
        COLUMN_HTTPS,
        COLUMN_ICONURL,
        COLUMN_COLOR,
        COLUMN_USERNAME,
        COLUMN_PASSWORD,
        COLUMN_USERID,
        COLUMN_AVATAR,
        COLUMN_POSTCOUNT,
        COLUMN_THEME_INT,
        COLUMN_COOKIE_COUNT,
        COLUMN_LAST_TAB,
        COLUMN_TAGLINE,
        COLUMN_CHAT_THREAD,
        COLUMN_CHAT_FORUM,
        COLUMN_CHAT_NAME,
        COLUMN_BACKGROUND,
        COLUMN_BOXCOLOR,
        COLUMN_BOXBORDER,
        COLUMN_TEXTCOLOR,
        COLUMN_DIVIDERCOLOR,
        COLUMN_WALLPAPER,
        COLUMN_FFCHAT,
        COLUMN_ANALYTICS,
        COLUMN_MOBFOX,
    };
  }

  public static void add(final SQLiteDatabase db, final Server server) {
    server.setId(db.insert(ServerEntry.TABLE_NAME, null, getValues(server)));
  }

  public static void update(final SQLiteDatabase db, final Server server) {
    db.update(
        ServerEntry.TABLE_NAME,
        getValues(server),
        SELECTION_ONE_BY_ID,
        new String[] {Long.toString(server.getId())});
  }


  private static Server constructEntityFrom(final Cursor cursor) {
    final Server server = new Server();
    server.setId(cursor.getLong(cursor.getColumnIndex(ServerEntry._ID)));
    server.serverAddress = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));
    server.serverName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
    server.serverHttps = cursor.getInt(cursor.getColumnIndex(COLUMN_HTTPS)) == 1;
    server.serverIcon = cursor.getString(cursor.getColumnIndex(COLUMN_ICONURL));
    server.serverColor = cursor.getString(cursor.getColumnIndex(COLUMN_COLOR));
    server.serverUserName = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
    server.serverPassword = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
    server.serverUserId = cursor.getString(cursor.getColumnIndex(COLUMN_USERID));
    server.serverAvatar = cursor.getString(cursor.getColumnIndex(COLUMN_AVATAR));
    server.serverPostcount = cursor.getString(cursor.getColumnIndex(COLUMN_POSTCOUNT));
    server.serverTheme = cursor.getString(cursor.getColumnIndex(COLUMN_THEME_INT));
    server.serverCookies = cursor.getString(cursor.getColumnIndex(COLUMN_COOKIE_COUNT));
    server.serverTab = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_TAB));
    server.serverTagline = cursor.getString(cursor.getColumnIndex(COLUMN_TAGLINE));
    server.chatThread = cursor.getString(cursor.getColumnIndex(COLUMN_CHAT_THREAD));
    server.chatForum = cursor.getString(cursor.getColumnIndex(COLUMN_CHAT_FORUM));
    server.chatName = cursor.getString(cursor.getColumnIndex(COLUMN_CHAT_NAME));
    server.serverBackground = cursor.getString(cursor.getColumnIndex(COLUMN_BACKGROUND));
    server.serverBoxColor = cursor.getString(cursor.getColumnIndex(COLUMN_BOXCOLOR));
    server.serverBoxBorder = cursor.getString(cursor.getColumnIndex(COLUMN_BOXBORDER));
    server.serverTextColor = cursor.getString(cursor.getColumnIndex(COLUMN_TEXTCOLOR));
    server.serverDividerColor = cursor.getString(cursor.getColumnIndex(COLUMN_DIVIDERCOLOR));
    server.serverWallpaper = cursor.getString(cursor.getColumnIndex(COLUMN_WALLPAPER));
    server.ffChatId = cursor.getString(cursor.getColumnIndex(COLUMN_FFCHAT));
    server.analyticsId = cursor.getString(cursor.getColumnIndex(COLUMN_ANALYTICS));
    server.mobfoxId = cursor.getString(cursor.getColumnIndex(COLUMN_MOBFOX));
    return server;
  }

  private static Server findOneBySelection(final SQLiteDatabase db, final String selection, final String[] selectionArgs) {
    final Cursor cursor = db.query(
        ServerEntry.TABLE_NAME,
        getProjection(),
        selection,
        selectionArgs,
        null,
        null,
        null);
    final Server server;
    if (cursor.moveToFirst()) {
      server = constructEntityFrom(cursor);
    } else {
      server = null;
    }
    cursor.close();
    return server;
  }

  public static Server findOne(final SQLiteDatabase db, final long id) {
    return findOneBySelection(db, SELECTION_ONE_BY_ID, new String[] {Long.toString(id)});
  }

  public static Server findOneByAddress(final SQLiteDatabase db, final String address) {
    return findOneBySelection(db, SELECTION_ONE_BY_ADDRESS, new String[] {address});
  }

  public static List<Server> findAll(final SQLiteDatabase db) {
    final Cursor cursor = db.query(
        ServerEntry.TABLE_NAME,
        getProjection(),
        null,
        null,
        null,
        null,
        null);
    final ArrayList<Server> servers = new ArrayList<>();
    while (cursor.moveToNext()) {
      servers.add(constructEntityFrom(cursor));
    }
    cursor.close();
    return servers;
  }

  public static void delete(final SQLiteDatabase db, final Server server) {
    db.delete(ServerEntry.TABLE_NAME, SELECTION_ONE_BY_ID, new String[] {Long.toString(server.getId())});
  }
}
