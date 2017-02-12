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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import de.enlightened.peris.db.MessageNotificationRepository;
import de.enlightened.peris.db.PerisDBHelper;
import de.enlightened.peris.db.ServerRepository;


public class MailService extends Service {

  private static final String TAG = MailService.class.getName();;
  private static final int SERVICE_TIMER = 20000000;
  private static final int MAX_ITEM_COUNT = 50;
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
      if (!server.serverUserId.contentEquals("0")) {
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
        db, idServer, Integer.parseInt(ii.senderId)) == null) {
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

      MessageNotificationRepository.add(db, new MessageNotification(idServer, Integer.parseInt(ii.senderId)));
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

  private class CheckMailTask extends AsyncTask<Server, Void, Object[]> {
    private long serverId;

    @SuppressWarnings({"rawtypes", "unchecked", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final Server... params) {
      serverId = params[0].getId();
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
                    processUnreadMessage(ii, this.serverId);
                  }

                }
              }
            }
          }
        } catch (Exception ex) {
          Log.d(TAG, ex.getMessage());
        }
      } catch (Exception e) {
        Log.d(TAG, e.getMessage());
      }
      nextServer();
    }
  }
}
