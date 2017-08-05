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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.enlightened.peris.db.MessageNotificationRepository;
import de.enlightened.peris.db.PerisDBHelper;
import de.enlightened.peris.db.ServerRepository;
import de.enlightened.peris.site.MessageBox;


public class MailService extends Service {

  private static final String TAG = MailService.class.getName();;
  private static final int SERVICE_TIMER = 20000000;
  private static final int LED_ON_MS = 500;
  private static final int LED_OFF_MS = 500;

  private int currentServer = 0;
  private Session mailSession;
  private List<Server> serverList;
  private PerisDBHelper dbHelper;

  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    this.dbHelper = new PerisDBHelper(this);
    this.startservice();
  }

  @Override
  public void onDestroy() {
    this.dbHelper.close();
    super.onDestroy();
    this.stopservice();

  }

  private void startservice() {
    final MyCount counter = new MyCount(SERVICE_TIMER, 1000);
    counter.start();
    Log.d(TAG, "Starting MailService");
  }

  private void stopservice() {

  }

  private void routineMailCheck() {
    this.mailSession = new Session(this, (PerisApp) this.getApplication(), this.dbHelper);
    final List<Server> servers = ServerRepository.findAll(this.dbHelper.getReadableDatabase());
    this.serverList = new ArrayList<>();

    for (final Server server : servers) {
      Log.i(TAG, "Checking login data for server " + server.serverAddress);
      if (server.serverUserId != null) {
        this.serverList.add(server);
      }
    }
    if (this.serverList.size() == 0) {
      Log.d(TAG, "No servers found, ending check.");
    }
    this.currentServer = 0;
    this.nextServer();
  }

  private void nextServer() {
    if ((this.currentServer + 1) > this.serverList.size()) {
      return;
    }
    Log.d(TAG, "MailService Tick - Checking " + this.serverList.get(this.currentServer).serverAddress);
    this.mailSession.setSessionListener(new Session.SessionListener() {

      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onSessionConnected() {
        new CheckMailTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mailSession.getServer());
      }

      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onSessionConnectionFailed(final String reason) {
        nextServer();
      }

    });
    this.mailSession.setServer(this.serverList.get(this.currentServer));
    this.currentServer++;
  }

  //See if notification previously sent.  If not, make a new notification
  private void processUnreadMessage(final InboxItem ii, final long idServer) {
    final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
    if (MessageNotificationRepository.findOneByServerAndMessage(
        db, idServer, Integer.parseInt(ii.messageId)) == null) {
      String notificationColor = getString(R.string.default_color);
      final String customColor = this.mailSession.getServer().serverColor;

      if (customColor.contains("#")) {
        notificationColor = customColor;
      }

      final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      final long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};

      final NotificationCompat.Builder mBuilder =
          new NotificationCompat.Builder(MailService.this)
              .setSmallIcon(R.drawable.ic_launcher)
              .setContentTitle("New Message From " + ii.sender)
              .setContentText(ii.subject)
              .setSound(alarmSound)
              .setLights(Color.parseColor(notificationColor), LED_ON_MS, LED_OFF_MS)
              .setVibrate(pattern)
              .setAutoCancel(true);

      final Intent resultIntent = new Intent(MailService.this, MessageActivity.class);
      final Bundle bundle = new Bundle();
      bundle.putString("id", (String) ii.messageId);
      bundle.putString("boxid", (String) "0");
      bundle.putString("name", (String) ii.subject);
      bundle.putString("moderator", (String) ii.senderId);
      bundle.putString("background", (String) notificationColor);
      bundle.putString("server", this.mailSession.getServer().serverId);
      resultIntent.putExtras(bundle);

      final TaskStackBuilder stackBuilder = TaskStackBuilder.create(MailService.this);
      stackBuilder.addParentStack(MessageActivity.class);
      stackBuilder.addNextIntent(resultIntent);

      String flag = ii.messageId;
      if (flag.length() > 5) {
        flag = flag.substring(flag.length() - 5, flag.length());
      }

      final PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(Integer.parseInt(flag), PendingIntent.FLAG_UPDATE_CURRENT);
      mBuilder.setContentIntent(resultPendingIntent);

      final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      mNotificationManager.notify(Integer.parseInt(ii.messageId), mBuilder.build());

      MessageNotificationRepository.add(db, new MessageNotification(idServer, Integer.parseInt(ii.messageId)));
    }
  }

  public class MyCount extends CountDownTimer {

    public MyCount(final long millisInFuture, final long countDownInterval) {
      super(millisInFuture, countDownInterval);
    }

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onFinish() {
      Log.d(TAG, "MailService Tick - Checking Mail");
      routineMailCheck();

      final MyCount counter = new MyCount(SERVICE_TIMER, 1000);
      counter.start();
    }

    @Override
    public void onTick(final long millisUntilFinished) {
      //whatever
    }
  }

  private class CheckMailTask extends AsyncTask<Server, Object, List<InboxItem>> {
    private long serverId;

    @Override
    protected List<InboxItem> doInBackground(final Server... params) {
      this.serverId = params[0].getId();
      final MessageBox messageBox = MailService.this.mailSession.getApi().getMessageBox();
      if (messageBox != null) {
        return MailService.this.mailSession.getApi().getMessages(messageBox.getInboxFolder());
      }
      return new ArrayList<>();
    }

    protected void onPostExecute(final List<InboxItem> messages) {
      if (messages != null) {
        for (InboxItem inboxItem : messages) {
          if (inboxItem.isUnread) {
            MailService.this.processUnreadMessage(inboxItem, this.serverId);
          }
        }
        MailService.this.nextServer();
      }
    }
  }
}
