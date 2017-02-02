package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class Conversation extends FragmentActivity {

  private static final int MAX_ITEM_COUNT = 50;
  private String partner;
  private String partnerName;
  private ListView conversationList;
  private String boxId = "0";
  private String senderName = "";
  private String conversationModerator;
  private SQLiteDatabase notetasticDB;
  private String sql;
  private String accent = "";
  private PerisApp application;
  private String externalServer = "0";
  private Session mailSession;
  private AnalyticsHelper ah;

  /**
   * Called when the activity is first created.
   */
  @SuppressLint("NewApi")
  @Override
  public void onCreate(final Bundle savedInstanceState) {

    this.application = (PerisApp) getApplication();

    final Bundle bundle = getIntent().getExtras();
    this.partner = bundle.getString("id");
    this.partnerName = bundle.getString("name");
    this.conversationModerator = bundle.getString("moderator");

    if (bundle.getString("background") != null) {
      final String bgc = bundle.getString("background");

      if (bgc.contains("#")) {
        this.accent = bgc;

      } else {
        this.accent = this.application.getSession().getServer().serverColor;
      }
    } else {
      this.accent = this.application.getSession().getServer().serverColor;
    }

    ThemeSetter.setTheme(this, this.accent);
    super.onCreate(savedInstanceState);
    ThemeSetter.setActionBar(this, this.accent);

    //Track app analytics
    this.ah = ((PerisApp) getApplication()).getAnalyticsHelper();
    this.ah.trackScreen(getClass().getName(), false);

    setContentView(R.layout.conversation);

    if (bundle.getString("boxid") != null) {
      this.boxId = bundle.getString("boxid");
    }

    setTitle(this.partnerName);

    final FrameLayout container = (FrameLayout) findViewById(R.id.conversation_list_container);

    this.conversationList = new ListView(this);
    this.conversationList.setDivider(null);
    this.conversationList.setLayoutParams(
        new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT));

    container.addView(this.conversationList);

    if (bundle.containsKey("server")) {
      this.externalServer = bundle.getString("server");
      Log.i("Peris", "Mail bundle contains server!");
      this.notetasticDB = this.openOrCreateDatabase("peris", 0, null);
      final String cleanServer = DatabaseUtils.sqlEscapeString(bundle.getString("server"));
      this.sql = "select * from accountlist where _id = " + cleanServer + ";";
      final Cursor c = this.notetasticDB.rawQuery(this.sql, null);

      if (c == null) {
        this.notetasticDB.close();
        return;
      }
      Server server = null;

      while (c.moveToNext()) {
        server = IntroScreen.parseServerData(c);
      }

      this.notetasticDB.close();

      if (server == null) {
        Log.i("Peris", "Conversaion Server is null!");
        return;
      }
      this.mailSession = new Session(this, (PerisApp) getApplication());
      this.mailSession.setSessionListener(new Session.SessionListener() {

        @Override
        public void onSessionConnected() {
          new LoadInboxTask().execute();
        }

        @Override
        public void onSessionConnectionFailed(final String reason) {
          return;
        }

      });
      this.mailSession.setServer(server);


    } else {
      this.mailSession = this.application.getSession();

      new LoadInboxTask().execute();
    }

    if (getString(R.string.server_location).contentEquals("0")) {
      if (ForegroundColorSetter.getForegroundDark(this.mailSession.getServer().serverColor)) {
        getActionBar().setIcon(R.drawable.ic_ab_main_black);
      } else {
        getActionBar().setIcon(R.drawable.ic_ab_main_white);
      }
    }


  }

  @Override
  public void onResume() {
    //new fetchParticipants().execute();
    super.onResume();
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.conversation_menu, menu);

    if (ForegroundColorSetter.getForegroundDark(this.accent)) {
      final MenuItem itemReply = menu.findItem(R.id.convo_menu_reply);
      final MenuItem itemDelete = menu.findItem(R.id.convo_menu_delete);

      itemReply.setIcon(R.drawable.ic_action_reply_dark);
      itemDelete.setIcon(R.drawable.ic_action_discard_dark);
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.convo_menu_reply:
        this.launchComposer();
        break;
      case R.id.convo_menu_delete:
        this.deleteMessage();
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  private void launchComposer() {
    final Intent myIntent = new Intent(Conversation.this, NewPost.class);

    final Bundle bundle = new Bundle();
    bundle.putString("postid", (String) this.partner);
    bundle.putString("parent", (String) this.conversationModerator);
    bundle.putString("category", this.senderName);
    bundle.putString("subforum_id", (String) "0");
    bundle.putString("original_text", (String) "");
    bundle.putString("boxTitle", (String) this.partnerName);
    bundle.putString("picture", (String) "0");
    bundle.putString("color", (String) this.accent);
    bundle.putString("subject", (String) this.partnerName);
    bundle.putInt("post_type", (Integer) 4);

    if (!this.externalServer.contentEquals("0")) {
      bundle.putString("server", this.externalServer);
    }

    myIntent.putExtras(bundle);

    this.startActivity(myIntent);
  }

  @SuppressLint("NewApi")
  private void deleteMessage() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      new DeleteMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      new DeleteMessageTask().execute();
    }
  }

  private class LoadInboxTask extends AsyncTask<String, Void, Object[]> {

    // can use UI thread here
    protected void onPreExecute() {

    }

    // automatically done on worker thread (separate from UI thread)
    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    protected Object[] doInBackground(final String... args) {

      final Object[] result = new Object[MAX_ITEM_COUNT];
      try {
        final Vector paramz = new Vector();
        paramz.addElement(partner);
        paramz.addElement(boxId);
        paramz.addElement(true);
        result[0] = application.getSession().performSynchronousCall("get_message", paramz);

      } catch (Exception e) {
        Log.w("Peris", e.getMessage());
        return null;
      }
      return result;
    }

    // can use UI thread here
    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    protected void onPostExecute(final Object[] result) {
      if (result == null) {
        return;
      }

      final ArrayList<Post> postList = new ArrayList<Post>();
      try {
        for (Object o : result) {
          if (o != null) {
            final HashMap map = (HashMap) o;
            final Date timestamp = (Date) map.get("sent_date");
            final Post po = new Post();
            //po.category_id = category_id;
            //po.subforum_id = subforum_id;
            //po.thread_id = thread_id;
            //po.categoryModerator = moderator;

            senderName = new String((byte[]) map.get("msg_from"));
            po.author = new String((byte[]) map.get("msg_from"));
            po.authorId = conversationModerator;
            po.body = new String((byte[]) map.get("text_body"));
            po.avatar = (String) map.get("icon_url");
            po.id = partner;
            po.tagline = "tagline";
            po.timestamp = timestamp.toString();

            postList.add(po);
          }
        }

        conversationList.setAdapter(new PostAdapter(postList, Conversation.this, application, -1));

      } catch (Exception e) {
        return;
      }
    }
  }

  private class DeleteMessageTask extends AsyncTask<String, Void, Object[]> {

    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final String... params) {
      final Object[] result = new Object[MAX_ITEM_COUNT];
      try {
        final Vector paramz = new Vector();
        paramz.addElement(partner);
        paramz.addElement(boxId);
        result[0] = application.getSession().performSynchronousCall("delete_message", paramz);
      } catch (Exception e) {
        Log.w("Peris", e.getMessage());
        return null;
      }
      return result;

    }

    protected void onPostExecute(final Object[] result) {
      finish();
    }
  }
}
