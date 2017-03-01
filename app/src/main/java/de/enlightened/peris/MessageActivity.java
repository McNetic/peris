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

import android.annotation.SuppressLint;
import android.content.Intent;
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
import java.util.Vector;

import de.enlightened.peris.db.PerisDBHelper;
import de.enlightened.peris.db.ServerRepository;

public class MessageActivity extends FragmentActivity {

  private static final String TAG = MessageActivity.class.getName();
  private static final int MAX_ITEM_COUNT = 50;
  private static final boolean RETURN_HTML = true;

  private String messageId;
  private String partnerName;
  private ListView conversationList;
  private String boxId = "0";
  private String senderName = "";
  private String conversationModerator;
  private String accent = "";
  private PerisApp application;
  private String externalServer = "0";
  private Session mailSession;
  private AnalyticsHelper ah;
  private PerisDBHelper dbHelper;

  /**
   * Called when the activity is first created.
   */
  @SuppressLint("NewApi")
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    this.dbHelper = new PerisDBHelper(this);
    this.application = (PerisApp) getApplication();
    final Bundle bundle = getIntent().getExtras();
    this.messageId = bundle.getString("id");
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
      Log.i(TAG, "Mail bundle contains server!");
      final Server server = ServerRepository.findOne(this.dbHelper.getWritableDatabase(), Long.parseLong(this.externalServer));
      if (server == null) {
        Log.i(TAG, "Conversation Server is null!");
        return;
      }
      this.mailSession = new Session(this, (PerisApp) getApplication(), this.dbHelper);
      this.mailSession.setSessionListener(new Session.SessionListener() {

        @Override
        public void onSessionConnected() {
          new LoadMessageTask(MessageActivity.this.messageId, MessageActivity.this.boxId).execute();
        }

        @Override
        public void onSessionConnectionFailed(final String reason) {
          return;
        }

      });
      this.mailSession.setServer(server);
    } else {
      this.mailSession = this.application.getSession();
      new LoadMessageTask(MessageActivity.this.messageId, MessageActivity.this.boxId).execute();
    }

    if (getString(R.string.server_location).contentEquals("0")) {
      if (ThemeSetter.getForegroundDark(this.mailSession.getServer().serverColor)) {
        getActionBar().setIcon(R.drawable.ic_ab_main_black);
      } else {
        getActionBar().setIcon(R.drawable.ic_ab_main_white);
      }
    }
  }

  @Override
  protected void onDestroy() {
    this.dbHelper.close();
    super.onDestroy();
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

    if (ThemeSetter.getForegroundDark(this.accent)) {
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
    final Intent myIntent = new Intent(MessageActivity.this, NewPost.class);

    final Bundle bundle = new Bundle();
    bundle.putString("postid", (String) this.messageId);
    bundle.putString("parent", (String) this.conversationModerator);
    bundle.putString("category", this.senderName);
    bundle.putString("subforum_id", (String) "0");
    bundle.putString("original_text", (String) "");
    bundle.putString("boxTitle", (String) this.partnerName);
    bundle.putString("picture", (String) "0");
    bundle.putString("color", (String) this.accent);
    bundle.putString("subject", (String) this.partnerName);
    bundle.putString("post_type", NewPost.Type.Message.name());

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

  private class LoadMessageTask extends AsyncTask<String, Void, Post> {

    private final String messageId;
    private final String boxId;

    public LoadMessageTask(final String messageId, final String boxId) {
      this.messageId = messageId;
      this.boxId = boxId;
    }

    // can use UI thread here
    protected void onPreExecute() {
    }

    // automatically done on worker thread (separate from UI thread)
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Post doInBackground(final String... args) {
      return MessageActivity.this.application.getSession().getApi().getMessage(this.boxId, this.messageId, RETURN_HTML);
    }

    // can use UI thread here
    @SuppressWarnings("rawtypes")
    protected void onPostExecute(final Post post) {
      if (post != null) {
        final ArrayList<Post> postList = new ArrayList<Post>();
        MessageActivity.this.senderName = post.author;
        postList.add(post);
        MessageActivity.this.conversationList.setAdapter(new PostAdapter(postList, MessageActivity.this, MessageActivity.this.application, -1));
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
        paramz.addElement(messageId);
        paramz.addElement(boxId);
        result[0] = application.getSession().performSynchronousCall("delete_message", paramz);
      } catch (Exception e) {
        Log.w(TAG, e.getMessage());
        return null;
      }
      return result;

    }

    protected void onPostExecute(final Object[] result) {
      finish();
    }
  }
}
