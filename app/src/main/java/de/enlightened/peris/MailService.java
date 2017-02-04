package de.enlightened.peris;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;


public class MailService extends Service {

  private static final int SERVICE_TIMER = 20000000;
  private static final int MAX_ITEM_COUNT = 50;
  private static final int LED_ON_MS = 500;
  private static final int LED_OFF_MS = 500;

  private int currentServer = 0;
  private SQLiteDatabase notetasticDB;
  private String sql;
  private Session mailSession;
  private ArrayList<Server> serverList;

  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    this.initDatabase();
    this.startservice();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    this.stopservice();

  }

  private void startservice() {
    final MyCount counter = new MyCount(SERVICE_TIMER, 1000);
    counter.start();
    Log.d("Peris", "Starting MailService");
  }

  private void stopservice() {

  }

  private void routineMailCheck() {
    this.mailSession = new Session(this, (PerisApp) this.getApplication());
    this.serverList = new ArrayList<Server>();
    this.notetasticDB = this.openOrCreateDatabase("peris", 0, null);
    this.sql = "select * from accountlist;";

    final Cursor c = this.notetasticDB.rawQuery(this.sql, null);

    if (c == null) {
      this.notetasticDB.close();
      return;
    }

    while (c.moveToNext()) {
      final Server server = IntroScreen.parseServerData(c);
      Log.i("Peris", "Checking login data for server " + server.serverAddress);

      if (!server.serverUserId.contentEquals("0")) {
        this.serverList.add(server);
      }
    }
    this.notetasticDB.close();

    if (this.serverList.size() == 0) {
      Log.d("Peris", "No servers found, ending check.");
    }

    this.currentServer = 0;
    this.nextServer();
  }

  private void nextServer() {
    if ((this.currentServer + 1) > this.serverList.size()) {
      return;
    }

    Log.d("Peris", "MailService Tick - Checking " + this.serverList.get(this.currentServer).serverAddress);

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
  private void processUnreadMessage(final InboxItem ii, final String server) {
    if (this.checkIfAlreadyNotified(server, Integer.parseInt(ii.senderId))) {
      return;
    }
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
            .setContentTitle("New Message From " + ii.moderator)
            .setContentText(ii.sender)
            .setSound(alarmSound)
            .setLights(Color.parseColor(notificationColor), LED_ON_MS, LED_OFF_MS)
            .setVibrate(pattern)
            .setAutoCancel(true);

    final Intent resultIntent = new Intent(MailService.this, Conversation.class);
    final Bundle bundle = new Bundle();
    bundle.putString("id", (String) ii.senderId);
    bundle.putString("boxid", (String) "0");
    bundle.putString("name", (String) ii.sender);
    bundle.putString("moderator", (String) ii.moderatorId);
    bundle.putString("background", (String) notificationColor);
    bundle.putString("server", this.mailSession.getServer().serverId);
    resultIntent.putExtras(bundle);

    final TaskStackBuilder stackBuilder = TaskStackBuilder.create(MailService.this);
    stackBuilder.addParentStack(Conversation.class);
    stackBuilder.addNextIntent(resultIntent);

    String flag = ii.senderId;
    if (flag.length() > 5) {
      flag = flag.substring(flag.length() - 5, flag.length());
    }

    final PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(Integer.parseInt(flag), PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);

    final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(Integer.parseInt(ii.senderId), mBuilder.build());

    this.insertNotificationIntoDatabase(server, Integer.parseInt(ii.senderId));
  }

  private void initDatabase() {
    this.notetasticDB = this.openOrCreateDatabase("peris", MODE_PRIVATE, null);
    this.sql = "create table if not exists notifications(_id integer primary key,server varchar,message integer);";
    this.notetasticDB.setVersion(4);
    this.notetasticDB.execSQL(this.sql);
    this.notetasticDB.close();
  }

  private void insertNotificationIntoDatabase(final String server, final int notification) {
    final String cleanServer = DatabaseUtils.sqlEscapeString(server);

    this.notetasticDB = this.openOrCreateDatabase("peris", MODE_PRIVATE, null);
    this.sql = "insert into notifications(server,message) values(" + cleanServer + "," + notification + ");";
    this.notetasticDB.execSQL(this.sql);
    this.notetasticDB.close();
  }

  private boolean checkIfAlreadyNotified(final String server, final int notification) {
    final String cleanServer = DatabaseUtils.sqlEscapeString(server);

    this.notetasticDB = this.openOrCreateDatabase("peris", 0, null);
    this.sql = "select _id "
        + "from notifications "
        + "where server = " + cleanServer + " "
        + "and message = " + notification + ";";

    final Cursor c = this.notetasticDB.rawQuery(this.sql, null);

    if (c == null) {
      this.notetasticDB.close();
    } else if (c.getCount() == 0) {
      this.notetasticDB.close();
    } else {
      this.notetasticDB.close();
      return true;
    }
    return false;
  }

  public class MyCount extends CountDownTimer {

    public MyCount(final long millisInFuture, final long countDownInterval) {
      super(millisInFuture, countDownInterval);
    }

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onFinish() {
      Log.d("Peris", "MailService Tick - Checking Mail");
      routineMailCheck();

      final MyCount counter = new MyCount(SERVICE_TIMER, 1000);
      counter.start();
    }

    @Override
    public void onTick(final long millisUntilFinished) {
      //whatever
    }

  }

  private class CheckMailTask extends AsyncTask<Server, Void, Object[]> {
    private String currentServerAddress;

    @SuppressWarnings({"rawtypes", "unchecked", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final Server... params) {
      currentServerAddress = params[0].serverAddress;
      final Object[] result = new Object[MAX_ITEM_COUNT];

      try {
        Vector paramz = new Vector();
        final HashMap map = (HashMap) mailSession.performSynchronousCall("get_box_info", paramz);
        final Object[] boxes = (Object[]) map.get("list");

        String ourInboxId = "0";

        for (Object o : boxes) {
          final HashMap boxMap = (HashMap) o;
          final String boxType = (String) boxMap.get("box_type");

          if (boxType.contentEquals("INBOX")) {
            ourInboxId = (String) boxMap.get("box_id");
          }
        }

        paramz = new Vector();
        paramz.addElement(ourInboxId);

        result[0] = mailSession.performSynchronousCall("get_box", paramz);

      } catch (Exception e) {
        //null response
        return null;
      }
      return result;
    }

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    protected void onPostExecute(final Object[] result) {
      if (result == null) {
        //Toast toast = Toast.makeText(getActivity(), "Server connection timeout :-(", Toast.LENGTH_SHORT);
        //toast.show();
        return;
      }

      try {

        try {
          final ArrayList<InboxItem> inboxList = new ArrayList<InboxItem>();
          for (Object o : result) {
            if (o != null) {
              final HashMap map = (HashMap) o;

              if (map.containsKey("list")) {
                final Object[] topics = (Object[]) map.get("list");
                for (Object t : topics) {
                  final HashMap topicMap = (HashMap) t;
                  final Date timestamp = (Date) topicMap.get("sent_date");
                  final InboxItem ii = new InboxItem();

                  if (topicMap.containsKey("msg_state")) {
                    final int state = (Integer) topicMap.get("msg_state");

                    if (state == 1) {
                      ii.isUnread = true;
                    }
                  }

                  ii.unread = timestamp.toString();
                  ii.sender = new String((byte[]) topicMap.get("msg_subject"));
                  ii.senderId = (String) topicMap.get("msg_id");
                  ii.moderator = new String((byte[]) topicMap.get("msg_from"));
                  ii.moderatorId = (String) topicMap.get("msg_from_id");
                  inboxList.add(ii);

                  if (ii.isUnread) {
                    processUnreadMessage(ii, currentServerAddress);
                  }

                }
              }
            }
          }
        } catch (Exception ex) {
          Log.d("Peris", ex.getMessage());
        }
      } catch (Exception e) {
        Log.d("Peris", e.getMessage());
      }
      nextServer();
    }
  }
}
