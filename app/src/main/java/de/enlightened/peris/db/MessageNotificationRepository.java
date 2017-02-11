package de.enlightened.peris.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.enlightened.peris.MessageNotification;
import de.enlightened.peris.db.PerisDBContract.MessageNotificationEntry;

import static de.enlightened.peris.db.PerisDBContract.MessageNotificationEntry.COLUMN_ID_SERVER;
import static de.enlightened.peris.db.PerisDBContract.MessageNotificationEntry.COLUMN_MESSAGE_ID;

/**
 * Created by Nicolai Ehemann on 08.02.2017.
 */

public final class MessageNotificationRepository {

  private static final String SELECTION_ONE_BY_ID = String.format("%s = ?", MessageNotificationEntry._ID);
  private static final String SELECTION_ONE_BY_SERVER_AND_MESSAGE = String.format("%s = ?, %s = ?",
      MessageNotificationEntry.COLUMN_ID_SERVER, MessageNotificationEntry.COLUMN_MESSAGE_ID);

  private MessageNotificationRepository() {
  }

  private static ContentValues getValues(final MessageNotification messageNotification) {
    final ContentValues values = new ContentValues();
    values.put(COLUMN_ID_SERVER, messageNotification.getIdServer());
    values.put(COLUMN_MESSAGE_ID, messageNotification.getMessageId());
    return values;
  }

  private static String[] getProjection() {
    return new String[] {
        MessageNotificationEntry._ID,
        COLUMN_ID_SERVER,
        COLUMN_MESSAGE_ID,
    };
  }

  public static void add(final SQLiteDatabase db, final MessageNotification messageNotification) {
    messageNotification.setId(db.insert(MessageNotificationEntry.TABLE_NAME, null, getValues(messageNotification)));
  }

  public static void update(final SQLiteDatabase db, final MessageNotification messageNotification) {
    db.update(
        MessageNotificationEntry.TABLE_NAME,
        getValues(messageNotification),
        SELECTION_ONE_BY_ID,
        new String[] {Long.toString(messageNotification.getId())});
  }

  private static MessageNotification constructEntityFrom(final Cursor cursor) {
    final MessageNotification messageNotification = new MessageNotification(
        cursor.getLong(cursor.getColumnIndex(COLUMN_ID_SERVER)),
        cursor.getInt(cursor.getColumnIndex(COLUMN_MESSAGE_ID)));
    messageNotification.setId(cursor.getLong(cursor.getColumnIndex(MessageNotificationEntry._ID)));
    return messageNotification;
  }

  private static MessageNotification findOneBySelection(final SQLiteDatabase db, final String selection, final String[] selectionArgs) {
    final Cursor cursor = db.query(
        MessageNotificationEntry.TABLE_NAME,
        getProjection(),
        selection,
        selectionArgs,
        null,
        null,
        null);
    final MessageNotification messageNotification;
    if (cursor.moveToFirst()) {
      messageNotification = constructEntityFrom(cursor);
    } else {
      messageNotification = null;
    }
    cursor.close();
    return messageNotification;
  }

  public static MessageNotification findOne(final SQLiteDatabase db, final long id) {
    return findOneBySelection(db, SELECTION_ONE_BY_ID, new String[] {Long.toString(id)});
  }

  public static List<MessageNotification> findAll(final SQLiteDatabase db) {
    final Cursor cursor = db.query(
        MessageNotificationEntry.TABLE_NAME,
        getProjection(),
        null,
        null,
        null,
        null,
        null);
    final ArrayList<MessageNotification> messageNotifications = new ArrayList<>();
    while (cursor.moveToNext()) {
      messageNotifications.add(constructEntityFrom(cursor));
    }
    cursor.close();
    return messageNotifications;
  }

  public static void delete(final SQLiteDatabase db, final MessageNotification messageNotification) {
    db.delete(
        MessageNotificationEntry.TABLE_NAME,
        SELECTION_ONE_BY_ID,
        new String[] {Long.toString(messageNotification.getId())});
  }

  public static MessageNotification findOneByServerAndMessage(final SQLiteDatabase db, final long idServer, final int messageId) {
    return findOneBySelection(db,
        SELECTION_ONE_BY_SERVER_AND_MESSAGE,
        new String[] {Long.toString(idServer), Integer.toString(messageId)});
  }
}
