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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import de.enlightened.peris.api.ApiResult;
import de.enlightened.peris.db.PerisDBHelper;
import de.enlightened.peris.db.ServerRepository;
import de.enlightened.peris.site.Message;
import de.enlightened.peris.support.DateTimeUtils;
import de.enlightened.peris.support.Net;

public class MessageActivity extends FragmentActivity {

  private static final String TAG = MessageActivity.class.getName();
  private static final int MAX_ITEM_COUNT = 50;
  private static final boolean RETURN_HTML = true;
  private static final int DEFAULT_FONT_SIZE = 16;

  private String messageId;
  private String partnerName;
  private String folderId = "0";
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

    this.setContentView(R.layout.message_layout);

    if (bundle.getString("boxid") != null) {
      this.folderId = bundle.getString("boxid");
    }

    setTitle(this.partnerName);

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
          new LoadMessageTask(MessageActivity.this.messageId, MessageActivity.this.folderId).execute();
        }

        @Override
        public void onSessionConnectionFailed(final String reason) {
          return;
        }

      });
      this.mailSession.setServer(server);
    } else {
      this.mailSession = this.application.getSession();
      new LoadMessageTask(MessageActivity.this.messageId, MessageActivity.this.folderId).execute();
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
    final TaskListener<ApiResult> listener = new TaskListener<ApiResult>() {
      @Override
      public void onPostExecute(final ApiResult result) {
        MessageActivity.this.finish();
      }
    };

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      new DeleteMessageTask(MessageActivity.this.application.getSession().getApi(),
          MessageActivity.this.messageId, MessageActivity.this.folderId, listener)
          .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      new DeleteMessageTask(MessageActivity.this.application.getSession().getApi(),
          MessageActivity.this.messageId, MessageActivity.this.folderId, listener)
          .execute();
    }
  }

  private void renderMessage(final Message message) {
    final Context context = this.getApplicationContext();
    final SharedPreferences appPreferences = context.getSharedPreferences("prefs", 0);
    final boolean useShading = appPreferences.getBoolean("use_shading", false);
    final boolean useOpenSans = appPreferences.getBoolean("use_opensans", false);
    final int fontSize = appPreferences.getInt("font_size", DEFAULT_FONT_SIZE);
    final boolean currentAvatarSetting = appPreferences.getBoolean("show_images", true);
    final Typeface opensans = Typeface.createFromAsset(context.getAssets(), "fonts/opensans.ttf");

    final View view = this.findViewById(R.id.message_layout);
    final TextView poAuthor = (TextView) view.findViewById(R.id.message_author);
    final TextView poTimestamp = (TextView) view.findViewById(R.id.message_timestamp);
    final TextView tvOnline = (TextView) view.findViewById(R.id.message_online_status);

    final LinearLayout llBorderBackground = (LinearLayout) view.findViewById(R.id.ll_border_background);
    final LinearLayout llColorBackground = (LinearLayout) view.findViewById(R.id.ll_color_background);

    String textColor = context.getString(R.string.default_text_color);
    if (this.application.getSession().getServer().serverTextColor.contains("#")) {
      textColor = this.application.getSession().getServer().serverTextColor;
    }

    String boxColor = context.getString(R.string.default_element_background);
    if (this.application.getSession().getServer().serverBoxColor != null) {
      boxColor = this.application.getSession().getServer().serverBoxColor;
    }

    if (boxColor.contains("#")) {
      llColorBackground.setBackgroundColor(Color.parseColor(boxColor));
    } else {
      llColorBackground.setBackgroundColor(Color.TRANSPARENT);
    }

    //TODO: remove border?
    String boxBorder = context.getString(R.string.default_element_border);
    if (this.application.getSession().getServer().serverBoxBorder != null) {
      boxBorder = this.application.getSession().getServer().serverBoxBorder;
    }

    if (boxBorder.contentEquals("1")) {
      llBorderBackground.setBackgroundResource(R.drawable.element_border);
    } else {
      llBorderBackground.setBackgroundColor(Color.TRANSPARENT);
    }

    if (useOpenSans) {
      poAuthor.setTypeface(opensans);
      poTimestamp.setTypeface(opensans);
      tvOnline.setTypeface(opensans);
    }

    if (useShading) {
      poAuthor.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
      tvOnline.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
    }

    final LinearLayout llPostBodyHolder = (LinearLayout) view.findViewById(R.id.message_body_holder);
    llPostBodyHolder.removeAllViews();

    final ImageView poAvatar = (ImageView) view.findViewById(R.id.message_avatar);

    if (boxColor != null && boxColor.contains("#") && boxColor.length() == 7) {
      final ImageView postAvatarFrame = (ImageView) view.findViewById(R.id.message_avatar_frame);
      postAvatarFrame.setColorFilter(Color.parseColor(boxColor));
    } else {
      final ImageView postAvatarFrame = (ImageView) view.findViewById(R.id.message_avatar_frame);
      postAvatarFrame.setVisibility(View.GONE);
    }

    if (message.isAuthorOnline()) {
      tvOnline.setText("ONLINE");
      tvOnline.setVisibility(View.VISIBLE);
    } else {
      tvOnline.setVisibility(View.GONE);
    }

    poAuthor.setText(message.getAuthor());
    poTimestamp.setText(DateTimeUtils.getTimeAgo(message.getTimestamp()));

    final String postContent = message.getBody();
    // TODO: add attachments
    BBCodeParser.parseCode(context, llPostBodyHolder, postContent, opensans, useOpenSans, useShading, new ArrayList<PostAttachment>(), fontSize, true, textColor, this.application);

    poAuthor.setTextColor(Color.parseColor(textColor));
    poTimestamp.setTextColor(Color.parseColor(textColor));

    if (currentAvatarSetting) {
      if (Net.isUrl(message.getAuthorAvatar())) {
        final String imageUrl = message.getAuthorAvatar();
        ImageLoader.getInstance().displayImage(imageUrl, poAvatar);
      } else {
        poAvatar.setImageResource(R.drawable.no_avatar);
      }
    } else {
      poAvatar.setVisibility(View.GONE);
    }
  }

  private class LoadMessageTask extends AsyncTask<String, Void, Message> {

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
    protected Message doInBackground(final String... args) {
      return MessageActivity.this.application.getSession().getApi().getMessage(this.boxId, this.messageId, RETURN_HTML);
    }

    // can use UI thread here
    @SuppressWarnings("rawtypes")
    protected void onPostExecute(final Message message) {
      if (message != null) {
        MessageActivity.this.senderName = message.getAuthor();
        MessageActivity.this.renderMessage(message);
      }
    }
  }
}
