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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.Vector;

import de.enlightened.peris.site.Config;
import de.enlightened.peris.site.Topic;
import de.enlightened.peris.support.Net;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class PostsFragment extends Fragment {

  private static final String TAG = PostsFragment.class.getName();;
  static final int POST_RESPONSE = 1;
  static final int POSTS_PER_PAGE = 20;
  private static final int MAX_ITEM_COUNT = 50;
  private static final int MAX_SUBJECT_LENGTH = 45;
  private String serverAddress;
  private String subforumId;
  private String categoryId;
  private String threadId;
  private String userid;
  private Integer pageNumber;
  private Integer totalPages;
  private String storagePrefix = "";
  private DownloadPostsTask postsDownloader;
  private Post selectedPost;
  private String lock;
  private String postCount;
  private String background;
  private String currentThreadSubject;
  private URL shareURL;
  private int scrollLocation = 0;
  private int curMinPost = 0;
  private int curMaxPost = POSTS_PER_PAGE - 1;
  private int curTotalPosts = 0;
  private boolean forceBottomScroll = false;
  private boolean canPost = false;

  private PerisApp application;

  private ListView mainList;

  private LinearLayout postsBottomholder;
  private LinearLayout postsInputArea;
  private LinearLayout postsPagination;

  private EditText postsQuickReply;
  private Button postsQuickReplySubmit;

  private ImageView imFirst;
  private ImageView imPrevious;
  private ImageView imNext;
  private ImageView imLast;

  private TextView postsPageNumber;

  private FragmentActivity activity;
  private ProfileSelectedListener profileSelected = null;

  @Override
  public final void onCreate(final Bundle bundle) {
    super.onCreate(bundle);

    this.activity = (FragmentActivity) getActivity();

    this.application = (PerisApp) this.activity.getApplication();

    setHasOptionsMenu(true);
  }

  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.posts, container, false);
  }

  @Override
  public final void onStart() {
    super.onStart();

    this.mainList = (ListView) this.activity.findViewById(R.id.posts_list_view);

    // Set up reply and pagination colors
    this.postsBottomholder = (LinearLayout) this.activity.findViewById(R.id.posts_bottom_holder);
    this.postsInputArea = (LinearLayout) this.activity.findViewById(R.id.posts_input_area);
    this.postsPagination = (LinearLayout) this.activity.findViewById(R.id.posts_pagination);
    this.postsQuickReply = (EditText) this.activity.findViewById(R.id.posts_quick_reply);
    this.postsQuickReplySubmit = (Button) this.activity.findViewById(R.id.posts_quick_reply_submit);
    this.imFirst = (ImageView) this.activity.findViewById(R.id.imFirst);
    this.imPrevious = (ImageView) this.activity.findViewById(R.id.imPrevious);
    this.imNext = (ImageView) this.activity.findViewById(R.id.imNext);
    this.imLast = (ImageView) this.activity.findViewById(R.id.imLast);
    this.postsPageNumber = (TextView) this.activity.findViewById(R.id.posts_page_number);

    this.postsQuickReplySubmit.setOnClickListener(new OnClickListener() {
      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        postsQuickReply.setEnabled(false);
        postsQuickReplySubmit.setEnabled(false);
        new QuickReplyTask().execute();
      }
    });

    this.imFirst.setOnClickListener(new OnClickListener() {
      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        firstPage();
      }
    });

    this.imPrevious.setOnClickListener(new OnClickListener() {
      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        prevPage();
      }
    });

    this.imNext.setOnClickListener(new OnClickListener() {
      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        nextPage();
      }
    });

    this.imLast.setOnClickListener(new OnClickListener() {
      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        lastPage();
      }
    });

    if (this.application.getSession().getServer().serverColor.contains("#")) {
      this.postsQuickReplySubmit.setTextColor(Color.parseColor(this.application.getSession().getServer().serverColor));

      this.imFirst.setColorFilter(Color.parseColor(this.application.getSession().getServer().serverColor));
      this.imPrevious.setColorFilter(Color.parseColor(this.application.getSession().getServer().serverColor));
      this.imNext.setColorFilter(Color.parseColor(this.application.getSession().getServer().serverColor));
      this.imLast.setColorFilter(Color.parseColor(this.application.getSession().getServer().serverColor));
    }

    if (this.application.getSession().getServer().serverTextColor.contains("#")) {
      this.postsQuickReply.setTextColor(Color.parseColor(this.application.getSession().getServer().serverTextColor));
      this.postsPageNumber.setTextColor(Color.parseColor(this.application.getSession().getServer().serverTextColor));

      if (this.application.getSession().getServer().serverColor.contentEquals(this.application.getSession().getServer().serverBoxColor)) {
        this.postsQuickReplySubmit.setTextColor(Color.parseColor(this.application.getSession().getServer().serverTextColor));

        this.imFirst.setColorFilter(Color.parseColor(this.application.getSession().getServer().serverTextColor));
        this.imPrevious.setColorFilter(Color.parseColor(this.application.getSession().getServer().serverTextColor));
        this.imNext.setColorFilter(Color.parseColor(this.application.getSession().getServer().serverTextColor));
        this.imLast.setColorFilter(Color.parseColor(this.application.getSession().getServer().serverTextColor));
      }

    }

    String boxColor = getString(R.string.default_element_background);

    if (this.application.getSession().getServer().serverBoxColor != null) {
      boxColor = this.application.getSession().getServer().serverBoxColor;
    }

    if (boxColor.contains("#")) {
      this.postsBottomholder.setBackgroundColor(Color.parseColor(boxColor));
    }

    if (!(this.application.getSession().getServer().serverBackground.contentEquals(
        this.application.getSession().getServer().serverBoxColor)
        && this.application.getSession().getServer().serverBoxBorder.contentEquals("0"))) {
      this.mainList.setDivider(null);
    }

    final Bundle bundle = getArguments();
    this.subforumId = bundle.getString("subforum_id");
    this.categoryId = bundle.getString("category_id");
    this.threadId = bundle.getString("thread_id");
    this.lock = bundle.getString("lock");
    this.postCount = bundle.getString("posts");
    this.currentThreadSubject = bundle.getString("subject");

    if (this.application.getSession().getApi().getConfig().getForumSystem() == Config.ForumSystem.PHPBB) {
      this.shareURL = this.application.getSession().getServer().getURL("/viewtopic.php?f=" + this.subforumId + "&t=" + this.threadId);
    }

    this.background = this.application.getSession().getServer().serverColor;

    final SharedPreferences appPreferences = this.activity.getSharedPreferences("prefs", 0);
    this.serverAddress = this.application.getSession().getServer().serverAddress;


    if (getString(R.string.server_location).contentEquals("0")) {
      this.storagePrefix = this.serverAddress + "_";
    }

    this.userid = this.application.getSession().getServer().serverUserId;


    if (this.lock.contentEquals("1")) {
      final Toast toast = Toast.makeText(this.activity, "Thread is locked!", Toast.LENGTH_LONG);
      toast.show();
    }

    this.pageNumber = 1;
    this.pageNumber = appPreferences.getInt(this.storagePrefix + "thread_" + this.threadId + "_retained_page", -1);
    this.totalPages = ((Integer.parseInt(this.postCount) - 1) / POSTS_PER_PAGE) + 1;

    if (this.pageNumber == -1) {
      this.pageNumber = this.totalPages;

      if (this.pageNumber > 1) {
        this.forceBottomScroll = true;
      }
    }

    this.curMinPost = (this.pageNumber - 1) * POSTS_PER_PAGE;
    this.curMaxPost = this.curMinPost + POSTS_PER_PAGE - 1;

    this.postsInputArea.setVisibility(View.GONE);
  }

  @Override
  public final void onStop() {
    this.endCurrentlyRunning();
    super.onStop();

  }

  @Override
  public final void onPause() {

    final SharedPreferences preferences = this.activity.getSharedPreferences("prefs", 0);
    final SharedPreferences.Editor editor = preferences.edit();
    editor.putInt(this.storagePrefix + "t_" + this.threadId + "_p_" + this.pageNumber + "_position", this.mainList.getFirstVisiblePosition());
    editor.commit();

    this.endCurrentlyRunning();
    super.onPause();
  }

  @Override
  public final void onResume() {

    this.activity.getActionBar().setTitle(this.currentThreadSubject);

    final SharedPreferences preferences = this.activity.getSharedPreferences("prefs", 0);
    this.scrollLocation = preferences.getInt(this.storagePrefix + "t_" + this.threadId + "_p_" + this.pageNumber + "_position", 0);

    this.loadPosts();
    super.onResume();
  }

  public final void onDestroy() {
    super.onDestroy();
  }

  private void setupPagination() {
    this.totalPages = ((this.curTotalPosts - 1) / POSTS_PER_PAGE) + 1;
    this.postsPageNumber.setText(this.pageNumber + " of " + this.totalPages);

    if (this.totalPages > 1) {
      this.postsPagination.setVisibility(View.VISIBLE);
    } else {
      this.postsPagination.setVisibility(View.GONE);
    }

  }

  private void loadPosts() {
    //getActivity().getActionBar().setSubtitle("Page " + pageNumber + " of " + totalPages);

    final SharedPreferences appPreferences = this.activity.getSharedPreferences("prefs", 0);

    //Save what page we are on
    final SharedPreferences.Editor editor = appPreferences.edit();
    editor.putInt(this.storagePrefix + "thread_" + this.threadId + "_retained_page", this.pageNumber);
    editor.commit();

    this.endCurrentlyRunning();

    this.postsDownloader = new DownloadPostsTask();

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      this.postsDownloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      this.postsDownloader.execute();
    }
  }

  private void endCurrentlyRunning() {
    //Stop any running tasks
    if (this.postsDownloader != null) {
      if (this.postsDownloader.getStatus() == Status.RUNNING) {
        this.postsDownloader.cancel(true);
      }
    }
  }

  private void refresh() {
    this.loadPosts();
  }

  private void nextPage() {
    if (this.pageNumber < this.totalPages) {
      this.curMinPost = this.curMinPost + POSTS_PER_PAGE;
      this.curMaxPost = this.curMaxPost + POSTS_PER_PAGE;
      this.pageNumber++;
      this.loadPosts();
    }
  }

  private void prevPage() {
    if (this.pageNumber > 1) {
      this.curMinPost = this.curMinPost - POSTS_PER_PAGE;
      this.curMaxPost = this.curMaxPost - POSTS_PER_PAGE;
      this.pageNumber--;
      this.loadPosts();
    }
  }

  private void firstPage() {
    this.curMinPost = 0;
    this.curMaxPost = POSTS_PER_PAGE - 1;
    this.pageNumber = 1;
    this.loadPosts();
  }

  private void lastPage() {
    this.curMinPost = (this.totalPages - 1) * POSTS_PER_PAGE;
    this.curMaxPost = this.curMinPost + POSTS_PER_PAGE - 1;
    this.pageNumber = this.totalPages;
    this.loadPosts();
  }

  private void startPost() {
    if (this.lock.contentEquals("1")) {
      final Toast toast = Toast.makeText(this.activity, "Thread is locked!", Toast.LENGTH_LONG);
      toast.show();
    } else if (this.application.getSession().getServer().serverUserId == null) {
      final Toast toast = Toast.makeText(this.activity, "You must be logged in to post!", Toast.LENGTH_LONG);
      toast.show();
    } else {
      final Intent myIntent = new Intent(this.activity, NewPost.class);

      final Bundle bundle = new Bundle();
      bundle.putString("postid", (String) "0");
      bundle.putString("parent", (String) this.threadId);
      bundle.putString("category", (String) this.categoryId);
      bundle.putString("subforum_id", (String) this.subforumId);
      bundle.putString("original_text", (String) "");
      bundle.putString("boxTitle", (String) "RE: " + this.currentThreadSubject);
      bundle.putString("picture", (String) "0");
      bundle.putString("color", (String) this.background);
      bundle.putString("subject", (String) this.currentThreadSubject);
      bundle.putString("post_type", NewPost.Type.Reply.name());
      myIntent.putExtras(bundle);

      PostsFragment.this.startActivityForResult(myIntent, POST_RESPONSE);
    }
  }

  private void editPost() {
    if (this.lock.contentEquals("1")) {
      final Toast toast = Toast.makeText(this.activity, "Thread is locked!", Toast.LENGTH_LONG);
      toast.show();
    } else if (this.application.getSession().getServer().serverUserId == null) {
      final Toast toast = Toast.makeText(this.activity, "You must be logged in to post!", Toast.LENGTH_LONG);
      toast.show();
    } else {
      final Intent myIntent = new Intent(this.activity, NewPost.class);
      final Bundle bundle = new Bundle();
      bundle.putString("postid", (String) this.selectedPost.id);
      bundle.putString("parent", (String) this.threadId);
      bundle.putString("category", (String) this.categoryId);
      bundle.putString("subforum_id", (String) this.subforumId);
      bundle.putString("original_text", (String) this.selectedPost.body);
      bundle.putString("boxTitle", (String) "RE: " + this.currentThreadSubject);
      bundle.putString("picture", (String) this.selectedPost.picture);
      bundle.putString("color", (String) this.background);
      bundle.putString("subject", (String) this.currentThreadSubject);
      bundle.putString("post_type", NewPost.Type.EditPost.name());
      myIntent.putExtras(bundle);

      PostsFragment.this.startActivityForResult(myIntent, POST_RESPONSE);
    }
  }

  private void quotePost() {
    if (this.lock.contentEquals("1")) {
      final Toast toast = Toast.makeText(this.activity, "Thread is locked!", Toast.LENGTH_LONG);
      toast.show();
    } else if (this.application.getSession().getServer().serverUserId == null) {
      final Toast toast = Toast.makeText(this.activity, "You must be logged in to post!", Toast.LENGTH_LONG);
      toast.show();
    } else {
      final Intent myIntent = new Intent(this.activity, NewPost.class);
      final Bundle bundle = new Bundle();
      bundle.putString("postid", (String) "0");
      bundle.putString("parent", (String) this.threadId);
      bundle.putString("category", (String) this.categoryId);
      bundle.putString("subforum_id", (String) this.subforumId);
      bundle.putString("boxTitle", (String) "RE: " + this.currentThreadSubject);
      bundle.putString("original_text", (String) "[quote=\"" + this.selectedPost.author + "\"]" + this.selectedPost.body + "[/quote]<br /><br />");
      bundle.putString("picture", (String) "0");
      bundle.putString("color", (String) this.background);
      bundle.putString("post_type", NewPost.Type.Reply.name());
      bundle.putString("subject", (String) this.currentThreadSubject);
      myIntent.putExtras(bundle);

      PostsFragment.this.startActivityForResult(myIntent, POST_RESPONSE);
    }
  }

  public final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    if (requestCode == POST_RESPONSE) {
      this.pageNumber = this.totalPages;
      this.loadPosts();
    }
  }

  public final void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
    //Block profile viewing on restricted accounts
    final SharedPreferences appPreferences = this.activity.getSharedPreferences("prefs", 0);
    final boolean accountRestricted = appPreferences.getBoolean(this.storagePrefix + "logged_banned", false);
    if (accountRestricted) {
      return;
    }

    final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    this.selectedPost = (Post) this.mainList.getItemAtPosition(info.position);

    super.onCreateContextMenu(menu, v, menuInfo);
    final MenuInflater inflater = this.activity.getMenuInflater();
    inflater.inflate(R.menu.posts_context, menu);

    if (this.userid != null) {
      final MenuItem item3 = menu.findItem(R.id.posts_quote);
      item3.setVisible(true);

      final MenuItem item5 = menu.findItem(R.id.posts_message);
      item5.setVisible(true);
    }

    if (this.selectedPost.authorId != null) {
      if (this.selectedPost.authorId.equals(this.userid)) {
        final MenuItem item5 = menu.findItem(R.id.posts_message);
        item5.setVisible(false);
      }
    }

    final MenuItem itemBan = menu.findItem(R.id.posts_context_ban);
    final MenuItem itemDelete = menu.findItem(R.id.posts_context_delete);
    final MenuItem itemEdit = menu.findItem(R.id.posts_edit);
    final MenuItem itemThank = menu.findItem(R.id.posts_thank);
    final MenuItem itemLike = menu.findItem(R.id.posts_like);

    if (this.selectedPost.canThank) {
      itemThank.setVisible(true);
    } else {
      itemThank.setVisible(false);
    }

    if (this.selectedPost.canLike) {
      itemLike.setVisible(true);
    } else {
      itemLike.setVisible(false);
    }

    if (this.selectedPost.canBan && !this.selectedPost.userBanned) {
      itemBan.setVisible(true);
    } else {
      itemBan.setVisible(false);
    }

    if (this.selectedPost.canDelete) {
      itemDelete.setVisible(true);
    } else {
      itemDelete.setVisible(false);
    }

    if (this.selectedPost.canEdit) {
      itemEdit.setVisible(true);
    } else {
      itemEdit.setVisible(false);
    }
  }

  public final boolean onContextItemSelected(final MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == R.id.posts_edit) {
      this.editPost();
    } else if (itemId == R.id.posts_quote) {
      this.quotePost();
    } else if (itemId == R.id.posts_message) {
      this.startConvo();
    } else if (itemId == R.id.posts_copy) {
      this.storePostInClipboard();
    } else if (itemId == R.id.posts_context_delete_yes) {
      new DeletePostTask().execute(this.selectedPost.id);
    } else if (itemId == R.id.posts_context_ban) {
      this.dropTheHammer();
    } else if (itemId == R.id.posts_thank) {
      new ThankPostTask().execute(this.selectedPost.id);
    } else if (itemId == R.id.posts_like) {
      new LikePostTask().execute(this.selectedPost.id);
    } else {
      return super.onContextItemSelected(item);
    }
    return true;
  }

  @SuppressLint("NewApi")
  private void storePostInClipboard() {

    //Copy text support for all Android versions
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      final ClipboardManager clipboard = (ClipboardManager) this.activity.getSystemService(Context.CLIPBOARD_SERVICE);
      final ClipData cd = ClipData.newHtmlText(this.currentThreadSubject, this.selectedPost.body, this.selectedPost.body);
      clipboard.setPrimaryClip(cd);
    } else {
      final android.text.ClipboardManager clipboard = (android.text.ClipboardManager) this.activity.getSystemService(Context.CLIPBOARD_SERVICE);
      clipboard.setText(this.selectedPost.body);
    }

    final Toast toast = Toast.makeText(this.activity, "Text copied!", Toast.LENGTH_SHORT);
    toast.show();
  }

  @SuppressLint("NewApi")
  @Override
  public final void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    inflater.inflate(R.menu.post_n_page, menu);

    final MenuItem itemRefresh = menu.findItem(R.id.menu_refresh);
    final MenuItem itemNew = menu.findItem(R.id.menu_newpost);

    if (ThemeSetter.getForegroundDark(this.background)) {
      itemRefresh.setIcon(R.drawable.ic_action_refresh_dark);
      itemNew.setIcon(R.drawable.ic_action_new_dark);
    }

    if (this.totalPages == null) {
      this.totalPages = 0;
    }


    final MenuItem browserItem = menu.findItem(R.id.posts_menu_browser);
    final MenuItem shareItem = menu.findItem(R.id.posts_menu_share);

    if (this.shareURL != null) {
      browserItem.setVisible(false);
      shareItem.setVisible(false);
    } else {
      browserItem.setVisible(true);
      shareItem.setVisible(true);
    }

    super.onCreateOptionsMenu(menu, inflater);
  }

  public final boolean onOptionsItemSelected(final MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == R.id.posts_menu_browser) {
      final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Net.uriFromURL(this.shareURL));
      startActivity(browserIntent);
    } else if (itemId == R.id.posts_menu_share) {
      final Intent sendIntent = new Intent(Intent.ACTION_SEND);
      sendIntent.putExtra(Intent.EXTRA_SUBJECT, this.currentThreadSubject);
      sendIntent.putExtra(Intent.EXTRA_TEXT, this.shareURL.toExternalForm());
      sendIntent.setType("text/plain");
      startActivity(sendIntent);
    } else if (itemId == R.id.menu_newpost) {
      this.startPost();
    } else if (itemId == R.id.menu_refresh) {
      this.refresh();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  private void startConvo() {
    final Intent myIntent = new Intent(this.activity, NewPost.class);
    final Bundle bundle = new Bundle();
    bundle.putString("postid", (String) "0");
    bundle.putString("parent", (String) "0");
    bundle.putString("category", this.selectedPost.author);
    bundle.putString("subforum_id", (String) "0");
    bundle.putString("original_text", (String) "");
    bundle.putString("boxTitle", (String) "New Message");
    bundle.putString("picture", (String) "0");
    bundle.putString("color", (String) getString(R.string.default_color));
    bundle.putString("subject", (String) "");
    bundle.putString("post_type", NewPost.Type.Message.name());
    myIntent.putExtras(bundle);

    startActivity(myIntent);
  }

  private void dropTheHammer() {
    final Bundle bundle = new Bundle();
    bundle.putString("username", this.selectedPost.author);

    final BanDialogFragment newFragment = BanDialogFragment.newInstance();
    newFragment.setArguments(bundle);
    newFragment.show(this.activity.getSupportFragmentManager(), "dialog");
  }

  public final void setOnProfileSelectedListener(final ProfileSelectedListener listener) {
    this.profileSelected = listener;
  }

  //Profile selected interface
  public interface ProfileSelectedListener {
    void onProfileSelected(String username, String userid);
  }

  private class DownloadPostsTask extends AsyncTask<Object, Object, Topic> {
    @SuppressWarnings({"rawtypes", "unchecked", "checkstyle:requirethis"})
    @Override
    protected Topic doInBackground(final Object... params) {
      Log.d(TAG, "Posts - DownloadPostsTask");
      return PostsFragment.this.application.getSession().getApi().getTopic(threadId, curMinPost, curMaxPost, true);
    }

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis", "checkstyle:nestedifdepth", "checkstyle:nestedfordepth"})
    protected void onPostExecute(final Topic result) {
      if (activity != null) {
        if (result == null) {
          final Toast toast = Toast.makeText(activity, "No response from the server!", Toast.LENGTH_LONG);
          toast.show();
        } else {
          if (result.isCanPost()) {
            final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);
            if (appPreferences.getBoolean("show_quick_reply", true)) {
              postsInputArea.setVisibility(View.VISIBLE);
            }
          }

          setupPagination();
          if (mainList != null) {
            registerForContextMenu(mainList);
            mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

              public void onItemClick(final AdapterView<?> adapterView, final View view, final int itemPos, final long arg3) {
                if (userid == null) {
                  return;
                }

                if (profileSelected != null) {
                  final Post sender = (Post) adapterView.getItemAtPosition(itemPos);
                  profileSelected.onProfileSelected(sender.author, sender.authorId);
                }
              }
            });

            PostsFragment.this.mainList.setAdapter(new PostAdapter(result.getPosts(), activity, application, pageNumber));

            mainList.setItemsCanFocus(true);
            activity.setProgressBarIndeterminateVisibility(false);

            if (forceBottomScroll) {
              Log.d(TAG, "Force Bottom Scroll: " + (result.getPosts().size() - 1));
              forceBottomScroll = false;
              mainList.setSelection(result.getPosts().size() - 1);
            } else {
              mainList.setSelection(scrollLocation);
              Log.d(TAG, "Retained Scroll: " + scrollLocation);
            }
          }
        }
      }
    }
  }

  private class DeletePostTask extends AsyncTask<String, Void, String> {

    // parm[0] - (string)topic_id

    @SuppressLint("UseValueOf")
    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected String doInBackground(final String... params) {
      if (activity == null) {
        return null;
      }
      try {
        final Vector paramz = new Vector();
        paramz.addElement(params[0]);
        paramz.addElement(2);

        application.getSession().performSynchronousCall("m_delete_post", paramz);
      } catch (Exception ex) {
        Log.w(TAG, ex.getMessage());
      }

      return "";
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final String result) {
      if (activity == null) {
        return;
      }
      loadPosts();
    }
  }

  private class ThankPostTask extends AsyncTask<String, Void, String> {

    @SuppressLint("UseValueOf")
    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected String doInBackground(final String... params) {
      if (activity == null) {
        return null;
      }
      try {
        final Vector paramz = new Vector();
        paramz.addElement(params[0]);
        application.getSession().performSynchronousCall("thank_post", paramz);
      } catch (Exception ex) {
        Log.w(TAG, ex.getMessage());
      }
      return "";
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final String result) {

      if (activity == null) {
        return;
      }

      loadPosts();
    }
  }

  private class LikePostTask extends AsyncTask<String, Void, String> {

    @SuppressLint("UseValueOf")
    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected String doInBackground(final String... params) {
      if (activity == null) {
        return null;
      }
      try {
        final Vector paramz = new Vector();
        paramz.addElement(params[0]);
        application.getSession().performSynchronousCall("like_post", paramz);
      } catch (Exception ex) {
        Log.w(TAG, ex.getMessage());
      }
      return "";
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final String result) {
      if (activity == null) {
        return;
      }
      loadPosts();
    }
  }

  private class QuickReplyTask extends AsyncTask<String, Void, Object[]> {

    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    protected Object[] doInBackground(final String... args) {
      String comment = postsQuickReply.getText().toString().trim();
      if (comment.length() > 0) {
        String subject = currentThreadSubject.trim();

        final Object[] result = new Object[MAX_ITEM_COUNT];

        if (subject.length() > MAX_SUBJECT_LENGTH) {
          subject = subject.substring(0, MAX_SUBJECT_LENGTH - 1);
        }
        if (subject.length() < 1) {
          subject = "no subject";
        }

        final String tagline = application.getSession().getServer().serverTagline;

        if (tagline.length() > 0) {
          comment = comment + "\n\n" + tagline;
        }

        try {
          final Vector paramz = new Vector();
          paramz.addElement(categoryId);
          paramz.addElement(threadId);
          paramz.addElement(subject.getBytes());
          paramz.addElement(comment.getBytes());
          result[0] = application.getSession().performSynchronousCall("reply_post", paramz);
          return result;
        } catch (Exception e) {
          Log.w(TAG, e.getMessage());
        }
      }
      return null;
    }

    //This method is executed after the thread has completed.
    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final Object[] result) {

      postsQuickReplySubmit.setEnabled(true);
      postsQuickReply.setEnabled(true);

      if (result == null) {
        final Toast toast = Toast.makeText(activity, "Submission error, please retry :-(", Toast.LENGTH_LONG);
        toast.show();
        return;
      }

      forceBottomScroll = true;
      postsQuickReply.setText("");
      loadPosts();
    }

  }
}
