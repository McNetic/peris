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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.enlightened.peris.api.ApiResult;
import de.enlightened.peris.site.Category;
import de.enlightened.peris.site.Config;
import de.enlightened.peris.site.Topic;
import de.enlightened.peris.site.TopicItem;
import de.enlightened.peris.support.CacheService;
import de.enlightened.peris.support.Net;

@SuppressLint("NewApi")
public class CategoriesFragment extends ListFragment {
  private static final String TAG = CategoriesFragment.class.getName();
  private static final int CATEGORIES_PER_PAGE = 20;

  private String serverAddress;
  private String subforumId = null;
  private String background;
  private String userid;
  private TopicItem clickedTopicItem;
  private String storagePrefix = "";
  private DownloadCategoriesTask categoriesDownloader;
  private PerisApp application;
  private String searchQuery = "";
  private String passedSubforum = "";
  private String screenTitle;
  private String screenSubtitle;
  private int startingPos = 0;
  private int endingPos = CATEGORIES_PER_PAGE;

  private boolean canScrollMoreThreads = true;
  private boolean isExtraScrolling = false;
  private boolean isLoading = false;
  private boolean initialLoadComplete = false;

  private String[] subforumParts;
  private URL shareURL = null;
  private FragmentActivity activity;
  private String totalHash;
  private List<TopicItem> categoryList;
  private boolean initialParseDone = false;
  private TopicItemSelectedListener categorySelected = null;
  private OnScrollListener listScrolled = new OnScrollListener() {

    @Override
    public void onScroll(final AbsListView arg0, final int arg1, final int arg2, final int arg3) {
      //do nothing
    }

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onScrollStateChanged(final AbsListView arg0, final int arg1) {
      if (canScrollMoreThreads
          && !isLoading
          && categoryList != null
          && categoryList.size() >= CATEGORIES_PER_PAGE
          && initialLoadComplete
          && arg1 == SCROLL_STATE_IDLE
          && arg0.getLastVisiblePosition() >= categoryList.size() - 5) {
        isExtraScrolling = true;

        startingPos = endingPos + 1;
        endingPos = startingPos + CATEGORIES_PER_PAGE;

        categoriesDownloader = new DownloadCategoriesTask();
        categoriesDownloader.execute();
      }
    }

  };

  @Override
  public final void onCreate(final Bundle bundle) {
    super.onCreate(bundle);

    this.activity = (FragmentActivity) getActivity();
    this.application = (PerisApp) this.activity.getApplication();

    if (this.activity != null) {
      if (this.activity.getActionBar() != null) {
        if (this.activity.getActionBar().getSubtitle() != null) {
          this.screenSubtitle = this.activity.getActionBar().getSubtitle().toString();
        }
      }
    }

    setHasOptionsMenu(true);
  }

  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public final void onStart() {
    super.onStart();

    if (!(this.application.getSession().getServer().serverBackground.contentEquals(this.application.getSession().getServer().serverBoxColor) && this.application.getSession().getServer().serverBoxBorder.contentEquals("0"))) {
      getListView().setDivider(null);
    }

    final Bundle bundle = getArguments();
    this.subforumId = bundle.getString("subforum_id");
    this.background = bundle.getString("background");
    this.screenTitle = bundle.getString("subforum_name");
    this.passedSubforum = this.subforumId;

    if (bundle.containsKey("query")) {
      this.searchQuery = bundle.getString("query");
    }

    //Log.i(TAG, "**** New CategoriesFragment Instance ****");
    //Log.d(TAG, "Passed subforum " + this.subforumId);

    this.totalHash = this.subforumId;

    if (this.subforumId.contains("###")) {
      this.subforumParts = this.subforumId.split("###");
      Log.d(TAG, "Subforum has " + this.subforumParts.length + " parts.");
      this.subforumId = this.subforumParts[0];
      //hashId = subforumParts[1];
    } else {
      this.subforumParts = new String[1];
      this.subforumParts[0] = this.subforumId;
    }

    Log.d(TAG, "Entering subforum " + this.subforumId);

    this.serverAddress = this.application.getSession().getServer().serverAddress;

    if (getString(R.string.server_location).contentEquals("0")) {
      this.storagePrefix = this.serverAddress + "_";
    }

    this.userid = this.application.getSession().getServer().serverUserId;

    final String shareId = this.subforumId;
    if (shareId.contentEquals("0")) {
      this.shareURL = this.application.getSession().getServer().getURL();
    } else {
      if (this.application.getSession().getApi().getConfig().getForumSystem() == Config.ForumSystem.PHPBB) {
        this.shareURL = this.application.getSession().getServer().getURL("viewforum.php?f=" + shareId);
      }
    }
    getListView().setOnScrollListener(this.listScrolled);
  }

  @Override
  public final void onPause() {
    if (!this.subforumId.contentEquals("unread")
        && !this.subforumId.contentEquals("participated")
        && !this.subforumId.contentEquals("userrecent")
        && !this.subforumId.contentEquals("favs")
        && !this.subforumId.contentEquals("search")
        && !this.subforumId.contentEquals("forum_favs")) {
      final String scrollY = Integer.toString(getListView().getFirstVisiblePosition());

      final SharedPreferences appPreferences = this.activity.getSharedPreferences("prefs", 0);
      final SharedPreferences.Editor editor = appPreferences.edit();
      editor.putString(this.storagePrefix + "forumScrollPosition" + this.passedSubforum, scrollY);
      editor.commit();
    }

    this.endCurrentlyRunning();

    super.onPause();
  }

  @Override
  public final void onResume() {
    //Log.d(TAG,"CF OnResume Began");
    this.activity.getActionBar().setTitle(this.screenTitle);
    this.activity.getActionBar().setSubtitle(this.screenSubtitle);

    //activity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    final ResultObject forumObject = (ResultObject) CacheService.readObject(CategoriesFragment.this.getContext(), "forums/" + this.storagePrefix + "forum" + this.subforumId);

    if (forumObject != null) {
      try {
        this.parseCachedForums(forumObject);
        Log.d(TAG, "Forum cache available, using it");
      } catch (Exception ex) {
        if (ex.getMessage() != null) {
          Log.e(TAG, ex.getMessage());
        }
      }
    }

    this.loadCategories();

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      this.activity.invalidateOptionsMenu();
    }
    Log.d(TAG, "CF OnResume Completed");
    super.onResume();
  }

  private void endCurrentlyRunning() {
    //Stop any running tasks
    if (this.categoriesDownloader != null) {
      if (this.categoriesDownloader.getStatus() == Status.RUNNING) {
        this.categoriesDownloader.cancel(true);
        Log.i(TAG, "Killed Currently Running");
      }
    }
  }

  @Override
  public final void onStop() {
    super.onStop();

    this.endCurrentlyRunning();
  }

  private void loadCategories() {
    Log.d(TAG, "CF Starting loadCategories");
    this.endCurrentlyRunning();
    this.categoriesDownloader = new DownloadCategoriesTask();

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      this.categoriesDownloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      this.categoriesDownloader.execute();
    }
  }

  /*
  private void setChatThread() {

        application.getSession().getServer().chatThread = clickedTopicItem.id;
        application.getSession().getServer().chatForum = clickedTopicItem.subforumId;
        application.getSession().getServer().chatName = clickedTopicItem.name;
        application.getSession().updateServer();

        chatChanged.onChatChanged(application.getSession().getServer().chatThread);
  }
  */

  @SuppressWarnings("rawtypes")
  private void parseCachedForums(final ResultObject result) {
    Log.d(TAG, "parseCachedForums()");
    if (this.categoryList == null || !this.isExtraScrolling) {
      this.categoryList = new ArrayList<>();
    }
    int retainedPosition = getListView().getFirstVisiblePosition();

    if (!this.initialParseDone) {
      final SharedPreferences appPreferences = this.activity.getSharedPreferences("prefs", 0);
      final String savedForumPosition = appPreferences.getString(this.storagePrefix + "forumScrollPosition" + this.passedSubforum, "0");
      retainedPosition = Integer.parseInt(savedForumPosition);
    }
    //Announcement Topics
    if (result.announcementTopics != null) {
      //this.categoryList.add(result.announcementTopics);
      this.categoryList.addAll(result.announcementTopics.getTopics());
    }

    //Sticky Topics
    if (result.stickyTopics != null) {
      this.categoryList.addAll(result.stickyTopics.getTopics());
    }

    Log.d(TAG, "Starting category parse! " + this.subforumId);
    //Forums
    if (result.categories != null) {
      this.categoryList.addAll(result.categories.get(this.subforumId).getChildren());
    }
    /*
    if (result.categories != null) {
      ArrayList<CategoryOld> forumz = CategoryParser.parseCategories(result.categories, this.subforumId, this.background);
      Log.d(TAG, "Forums parsed!");
      String currentHash = this.subforumParts[0];
      Log.d(TAG, "Hash Size: " + this.subforumParts.length);
      if (this.subforumParts.length == 1) {
        for (CategoryOld c : forumz) {
          this.categoryList.add(c);
        }
      } else {
        for (int i = 1; i < this.subforumParts.length; i++) {
          currentHash = currentHash + "###" + this.subforumParts[i];
          Log.d(TAG, "Checking hash: " + currentHash + " (total hash is " + this.totalHash + ")");
          ArrayList<CategoryOld> tempForums = null;
          for (CategoryOld c : forumz) {
            if (c.children != null && c.id.contentEquals(currentHash)) {
              tempForums = c.children;
            }
          }

          if (tempForums != null) {
            forumz = tempForums;

            if (currentHash.contentEquals(this.totalHash)) {
              for (CategoryOld c : forumz) {
                this.categoryList.add(c);
              }
            }
          }
        }
      }
    }*/
    Log.d(TAG, "Finished category parse!");
    //Non-Sticky Topics
    if (result.defaultTopics == null) {
      this.canScrollMoreThreads = false;
    } else {
      this.categoryList.addAll(result.defaultTopics.getTopics());
    }

    if (result.favoriteTopics != null) {
      Log.i(TAG, "We have some favs!");
      this.categoryList.addAll(result.favoriteTopics);
    }

    setListAdapter(new CategoryAdapter(this.categoryList, this.activity, this.application));
    registerForContextMenu(getListView());
    getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final TopicItem topicItem = (TopicItem) parent.getItemAtPosition(position);

        if (topicItem != null && categorySelected != null) {
          categorySelected.onTopicItemSelected(topicItem);
        }
      }
    });

    getListView().setSelection(retainedPosition);
    this.initialParseDone = true;
  }

  /*private CategoryOld createCategoryFromTopics(final Map topicMap, final boolean subforum, final boolean sticky) {
    final CategoryOld ca = new CategoryOld();
    ca.name = new String((byte[]) topicMap.get("topic_title"));

    if (subforum && topicMap.get("forum_id") != null) {
      ca.subforumId = (String) topicMap.get("forum_id");
    } else {
      ca.subforumId = this.subforumId;

      //if(!hashId.contentEquals("0")) {
      //  ca.subforumId = hashId;
      //}
    }
    ca.id = (String) topicMap.get("topic_id");
    ca.lastUpdate = (Date) topicMap.get("last_reply_time");
    if (!subforum || topicMap.get("topic_author_name") != null) {
      ca.lastThread = new String((byte[]) topicMap.get("topic_author_name"));
    } else {
      ca.lastThread = new String((byte[]) topicMap.get("forum_name"));
    }
    if (sticky) {
      ca.topicSticky = "Y";
    }
    ca.type = "C";
    ca.color = this.background;

    if (topicMap.get("reply_number") != null) {
      ca.threadCount = topicMap.get("reply_number").toString().replace(".0", "");
    }

    if (topicMap.get("view_number") != null) {
      ca.viewCount = topicMap.get("view_number").toString().replace(".0", "");
    }

    if (topicMap.get("new_post") != null) {
      ca.hasNewTopic = (Boolean) topicMap.get("new_post");
    }

    if (topicMap.get("is_closed") != null) {
      ca.isLocked = (Boolean) topicMap.get("is_closed");
    }

    if (topicMap.containsKey("icon_url")) {
      if (topicMap.get("icon_url") != null) {
        ca.icon = (String) topicMap.get("icon_url");
      }
    }

    if (topicMap.get("can_stick") != null) {
      ca.canSticky = (Boolean) topicMap.get("can_stick");
    }

    if (topicMap.get("can_delete") != null) {
      ca.canDelete = (Boolean) topicMap.get("can_delete");
    }

    if (topicMap.get("can_close") != null) {
      ca.canLock = (Boolean) topicMap.get("can_close");
    }
    return ca;
  }*/

  public final void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
    final String serverUserId = this.application.getSession().getServer().serverUserId;
    final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

    this.clickedTopicItem = (TopicItem) CategoriesFragment.this.getListView().getItemAtPosition(info.position);

    if (serverUserId == null) {
      return;
    }
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.setHeaderTitle(this.clickedTopicItem.getHeading());
    final MenuInflater inflater = this.activity.getMenuInflater();

    inflater.inflate(R.menu.categories_context, menu);

    final MenuItem ubsubItem = menu.findItem(R.id.categories_unsubscribe);
    final MenuItem subItem = menu.findItem(R.id.categories_subscribe);
    final MenuItem stickyItem = menu.findItem(R.id.categories_context_sticky);
    final MenuItem lockItem = menu.findItem(R.id.categories_context_lock);
    final MenuItem deleteItem = menu.findItem(R.id.categories_context_delete);

    final MenuItem subscribeItem = menu.findItem(R.id.categories_add_favorite);
    final MenuItem unsubscribeItem = menu.findItem(R.id.categories_remove_favorite);

    if (this.clickedTopicItem instanceof Category) {
      final Category category = (Category) this.clickedTopicItem;
      ubsubItem.setVisible(false);
      subItem.setVisible(false);
      stickyItem.setVisible(false);
      lockItem.setVisible(false);
      deleteItem.setVisible(false);

      if (category.isCanSubscribe()) {
        subscribeItem.setVisible(true);
      } else {
        subscribeItem.setVisible(false);
      }

      if (category.isSubscribed()) {
        unsubscribeItem.setVisible(true);
        subscribeItem.setVisible(false);
      } else {
        unsubscribeItem.setVisible(false);
      }
    } else {
      final Topic topic = (Topic) this.clickedTopicItem;

      unsubscribeItem.setVisible(false);
      subscribeItem.setVisible(false);


      if (topic.isCanStick()) {
        stickyItem.setVisible(true);

        if (Topic.Type.Sticky == topic.getType()) {
          stickyItem.setTitle("Unstick Topic");
        } else {
          stickyItem.setTitle("Stick Topic");
        }
      } else {
        stickyItem.setVisible(false);
      }

      if (topic.isCanDelete()) {
        deleteItem.setVisible(true);
      } else {
        deleteItem.setVisible(false);
      }

      if (topic.isCanClose()) {
        lockItem.setVisible(true);

        if (topic.isClosed()) {
          lockItem.setTitle("Unlock Topic");
        } else {
          lockItem.setTitle("Lock Topic");
        }
      } else {
        lockItem.setVisible(false);
      }

      if (this.subforumId.contentEquals("favs")) {
        ubsubItem.setVisible(true);
        subItem.setVisible(false);
      } else {
        ubsubItem.setVisible(false);
        subItem.setVisible(true);
      }
    }
  }

  public final  boolean onContextItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.categories_unsubscribe:
        new UnsubscribeTopicTask().execute(this.clickedTopicItem.getId());
        break;
      case R.id.categories_subscribe:
        new SubscribeTopicTask().execute(this.clickedTopicItem.getId());
        break;
      case R.id.categories_context_sticky:
        if (this.clickedTopicItem instanceof Topic) {
          if (Topic.Type.Sticky == ((Topic) this.clickedTopicItem).getType()) {
            new StickyTopicTask().execute(this.clickedTopicItem.getId(), "2");
          } else {
            new StickyTopicTask().execute(this.clickedTopicItem.getId(), "1");
          }
        }
        break;
      case R.id.categories_context_lock:
        if (this.clickedTopicItem instanceof Topic) {
          final Topic topic = (Topic) this.clickedTopicItem;
          if (((Topic) this.clickedTopicItem).isClosed()) {
            new LockTopicTask().execute(this.clickedTopicItem.getId(), "1");
          } else {
            new LockTopicTask().execute(this.clickedTopicItem.getId(), "2");
          }
        }
        break;
      case R.id.categories_context_delete_yes:
        new DeleteTopicTask().execute(this.clickedTopicItem.getId());
        break;
      case R.id.categories_add_favorite:
        new AddToFavoritesTask().execute(this.clickedTopicItem.getId());
        break;
      case R.id.categories_remove_favorite:
        new RemoveFromFavoritesTask().execute(this.clickedTopicItem.getId());
        break;
      default:
        return super.onContextItemSelected(item);
    }
    return true;
  }

  @SuppressLint("NewApi")
  @Override
  public final void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

    if (this.userid != null) {
      inflater.inflate(R.menu.categories_menu, menu);
    }

    super.onCreateOptionsMenu(menu, inflater);

  }

  @Override
  public final void onPrepareOptionsMenu(final Menu menu) {
    super.onPrepareOptionsMenu(menu);

    if ((this.userid != null) && (menu != null)) {
      if (this.subforumId == null
          || this.subforumId.contentEquals("participated")
          || this.subforumId.contentEquals("favs")
          || this.subforumId.contentEquals("search")) {
        final MenuItem item = menu.findItem(R.id.cat_mark_read);
        if (item != null) {
          item.setVisible(false);
        }
      } else {
        final MenuItem item = menu.findItem(R.id.cat_mark_read);
        if (item != null) {
          if (ThemeSetter.getForegroundDark(this.background)) {
            item.setIcon(R.drawable.ic_action_read_dark);
          }
        }
      }

      if (this.subforumId ==  null
          || this.subforumId.contentEquals("participated")
          || this.subforumId.contentEquals("favs")
          || this.subforumId.contentEquals("userrecent")
          || this.subforumId.contentEquals("search")) {
        final MenuItem item2 = menu.findItem(R.id.cat_new_thread);
        if (item2 != null) {
          item2.setVisible(false);
        }
      } else {
        final MenuItem item2 = menu.findItem(R.id.cat_new_thread);
        if (item2 != null) {
          if (ThemeSetter.getForegroundDark(this.background)) {
            item2.setIcon(R.drawable.ic_action_new_dark);
          }
        }
      }

      final MenuItem browserItem = menu.findItem(R.id.cat_open_browser);

      if (this.shareURL == null) {
        browserItem.setVisible(false);
      } else {
        browserItem.setVisible(true);
      }
    }
  }

  @Override
  public final boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.cat_new_thread:
        this.startPost();
        break;
      case R.id.cat_mark_read:
        this.markAsRead();
        break;
      case R.id.cat_open_browser:
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Net.uriFromURL(this.shareURL));
        this.startActivity(browserIntent);
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  private void startPost() {

    if (this.subforumId == null || this.userid == null) {
      final Toast toast = Toast.makeText(this.activity, "You are not allowed to post here!", Toast.LENGTH_LONG);
      toast.show();
      return;
    }

    final Intent myIntent = new Intent(this.activity, NewPost.class);

    final Bundle bundle = new Bundle();
    bundle.putString("postid", (String) "0");
    bundle.putString("parent", (String) "0");
    bundle.putString("category", (String) this.subforumId);
    bundle.putString("subforum_id", (String) this.subforumId);
    bundle.putString("original_text", (String) "");
    bundle.putString("boxTitle", (String) "New Thread");
    bundle.putString("picture", (String) "0");
    bundle.putString("subject", (String) "");
    bundle.putString("post_type", NewPost.Type.NewThread.name());
    bundle.putString("color", (String) this.background);
    myIntent.putExtras(bundle);

    startActivity(myIntent);

  }

  public final void setOnCategorySelectedListener(final TopicItemSelectedListener l) {
    this.categorySelected = l;
  }

  private void markAsRead() {
    new ReadMarkerTask().execute(this.subforumId);
  }

  //Category Selected Interface
  public interface TopicItemSelectedListener {
    void onTopicItemSelected(TopicItem category);
  }

  private class DownloadCategoriesTask extends AsyncTask<String, Void, ResultObject> {

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @SuppressLint("UseValueOf")
    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected ResultObject doInBackground(final String... params) {
      Log.i(TAG, "DownloadCategoriesTask doInBackground subforumId " + subforumId);
      if (activity == null) {
        Log.e(TAG, "Category activity is null!");
        return null;
      }
      isLoading = true;
      final ResultObject result = new ResultObject();
      if (subforumId.contentEquals("favs")) {
        //Handle subscription category
        result.defaultTopics = application.getSession().getApi().getSubscribedTopics(subforumId, startingPos, endingPos);
      } else if (subforumId.contentEquals("forum_favs")) {
        //Handle favorites category
        result.favoriteTopics = application.getSession().getApi().getSubscribedCategories();
      } else if (subforumId.contentEquals("participated")) {
        //Handle participated topics category
        result.defaultTopics = application.getSession().getApi().getParticipatedTopics(subforumId, startingPos, endingPos);
      } else if (subforumId.contentEquals("search")) {
        //Handle topic listing for the Search function
        result.defaultTopics = application.getSession().getApi().searchTopic(subforumId, searchQuery, startingPos, endingPos);
      } else if (subforumId.contentEquals("timeline")) {
        //Handle timeline
        result.defaultTopics = application.getSession().getApi().getLatestTopics(subforumId, startingPos, endingPos);
      } else if (subforumId.contentEquals("unread")) {
        //Handle topic listing for the Unread category
        if (!isExtraScrolling) {
          //TODO: paging?
          result.defaultTopics = application.getSession().getApi().getUnreadTopics(subforumId);
        }
      } else if (!subforumId.contentEquals("userrecent")) {
          //Do not get a forum listing if we are inside one of the special sections
        if (!isExtraScrolling) {
          final String topicId;
          if (subforumId != null  && !Category.ROOT_ID.equals(subforumId)) {
            result.categories = CategoriesFragment.this.application.getSession().getApi().getCategory(subforumId);
            topicId = subforumId;
          } else {
            result.categories = CategoriesFragment.this.application.getSession().getApi().getCategories();
            topicId = "-1";
          }
          //First grab any announcement topics
          result.announcementTopics = CategoriesFragment.this.application.getSession().getApi().getTopics(topicId, 0, CATEGORIES_PER_PAGE, Topic.Type.Announcement);
          //Then grab any sticky topics
          result.stickyTopics = CategoriesFragment.this.application.getSession().getApi().getTopics(topicId, 0, CATEGORIES_PER_PAGE, Topic.Type.Sticky);
        }

        //Grab the non-sticky topics
        Log.d(TAG, "Getting topics " + startingPos + " through " + endingPos);
        result.defaultTopics = CategoriesFragment.this.application.getSession().getApi().getTopics(subforumId, startingPos, endingPos);
      }
      return result;
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final ResultObject result) {
      Log.i(TAG, "DownloadCategoriesTask onPostExecute");

      if (activity != null) {
        if (result == null) {
          final Toast toast = Toast.makeText(activity, "Error pulling data from the server, ecCFDL", Toast.LENGTH_SHORT);
          toast.show();
        } else {
          Log.i(TAG, "Received category data!");

          initialLoadComplete = true;
          isLoading = false;

          final ResultObject cachedForum = (ResultObject) CacheService.readObject(CategoriesFragment.this.getContext(), "forums/" + storagePrefix + "forum" + subforumId);
          if (!result.equals(cachedForum)) {
            if (!isExtraScrolling) {
              try {
                CacheService.writeObject(CategoriesFragment.this.getContext(), "forums/" + storagePrefix + "forum" + subforumId, result);
              } catch (IOException e) {
                Log.e(TAG, "Error writing forum cache (" + e.getMessage() + ")\n" + e);
              }
            }
            parseCachedForums(result);
            Log.i(TAG, "Found " + CategoriesFragment.this.categoryList.size() + " categories");
          }
          /*
          final String objectString = GsonHelper.CUSTOM_GSON.toJson(result);
          final SharedPreferences appPreferences = activity.getSharedPreferences("prefs", 0);
          final String cachedForum = appPreferences.getString(storagePrefix + "forum" + subforumId, "n/a");

          if (!objectString.contentEquals(cachedForum)) {
            if (!isExtraScrolling) {
              final SharedPreferences.Editor editor = appPreferences.edit();
              editor.putString(storagePrefix + "forum" + subforumId, objectString);
              editor.commit();
            }

            final Object[][] forumObject = GsonHelper.CUSTOM_GSON.fromJson(objectString, Object[][].class);
            parseCachedForums(result);
          }
          */
        }
      }
    }
  }

  private class ReadMarkerTask extends AsyncTask<String, Void, ApiResult> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected ApiResult doInBackground(final String... params) {
      final ApiResult result;
      if (CategoriesFragment.this.activity != null) {
        if (!"0".equals(params[0]) && !"unread".equals(params[0])) {
          result = CategoriesFragment.this.application.getSession().getApi().markForumTopicsRead(params[0]);
        } else {
          result = CategoriesFragment.this.application.getSession().getApi().markAllTopicsRead();
        }
      } else {
        result = null;
      }
      return result;
    }

    protected void onPostExecute(final ApiResult result) {
      if (CategoriesFragment.this.activity != null) {
        if (CategoriesFragment.this.subforumId.equals("unread")) {
          CategoriesFragment.this.activity.finish();
        } else {
          CategoriesFragment.this.loadCategories();
          final Toast toast = Toast.makeText(CategoriesFragment.this.activity, "Posts marked read!", Toast.LENGTH_LONG);
          toast.show();
        }
      }
    }
  }

  private class SubscribeTopicTask extends AsyncTask<String, Void, ApiResult> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected ApiResult doInBackground(final String... params) {
      final ApiResult result;
      if (CategoriesFragment.this.activity != null) {
        result = CategoriesFragment.this.application.getSession().getApi().subscribeTopic(params[0]);
      } else {
        result = null;
      }
      return result;
    }

    protected void onPostExecute(final ApiResult result) {
      if (CategoriesFragment.this.activity != null) {
        final Toast toast = Toast.makeText(CategoriesFragment.this.activity, "Subscribed!", Toast.LENGTH_SHORT);
        toast.show();
      }
    }
  }

  private class UnsubscribeTopicTask extends AsyncTask<String, Void, ApiResult> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected ApiResult doInBackground(final String... params) {
      final ApiResult result;
      if (CategoriesFragment.this.activity != null) {
        result = CategoriesFragment.this.application.getSession().getApi().unsubscribeTopic(params[0]);
      } else {
        result = null;
      }
      return result;
    }

    protected void onPostExecute(final ApiResult result) {
      if (CategoriesFragment.this.activity != null) {
        CategoriesFragment.this.loadCategories();
      }
    }
  }

  private class StickyTopicTask extends AsyncTask<String, Void, ApiResult> {
    // parm[0] - (string)topic_id
    // parm[1] - (int)mode (1 - stick; 2 - unstick)
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected ApiResult doInBackground(final String... params) {
      final ApiResult result;
      if (CategoriesFragment.this.activity != null) {
        if ("1".equals(params[1])) {
          result = CategoriesFragment.this.application.getSession().getApi().setTopicSticky(params[0], true);
        } else {
          result = CategoriesFragment.this.application.getSession().getApi().setTopicSticky(params[0], false);
        }
      } else {
        result = null;
      }
      return result;
    }

    protected void onPostExecute(final ApiResult result) {
      if (CategoriesFragment.this.activity != null) {
        CategoriesFragment.this.loadCategories();
      }
    }
  }

  private class LockTopicTask extends AsyncTask<String, Void, ApiResult> {
    // parm[0] - (string)topic_id
    // parm[1] - (int)mode (1 - unlock; 2 - lock)
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected ApiResult doInBackground(final String... params) {
      final ApiResult result;
      if (CategoriesFragment.this.activity != null) {
        if ("1".equals(params[1])) {
          result = CategoriesFragment.this.application.getSession().getApi().setTopicLocked(params[0], false);
        } else {
          result = CategoriesFragment.this.application.getSession().getApi().setTopicLocked(params[0], true);
        }
      } else {
        result = null;
      }
      return result;
    }

    protected void onPostExecute(final ApiResult result) {
      if (CategoriesFragment.this.activity != null) {
        CategoriesFragment.this.loadCategories();
      }
    }
  }

  private class DeleteTopicTask extends AsyncTask<String, Void, ApiResult> {

    // parm[0] - (string)topic_id
    @SuppressLint("UseValueOf")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected ApiResult doInBackground(final String... params) {
      final ApiResult result;
      if (CategoriesFragment.this.activity != null) {
        result = CategoriesFragment.this.application.getSession().getApi().deleteTopic(params[0]);
      } else {
        result = null;
      }
      return result;
    }

    protected void onPostExecute(final ApiResult result) {
      if (CategoriesFragment.this.activity != null && result.isSuccess()) {
        CategoriesFragment.this.loadCategories();
      }
    }
  }

  private class AddToFavoritesTask extends AsyncTask<String, Void, ApiResult> {

    @SuppressLint("UseValueOf")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected ApiResult doInBackground(final String... params) {
      final ApiResult result;
      if (CategoriesFragment.this.activity != null) {
        result = CategoriesFragment.this.application.getSession().getApi().subscribeCategory(params[0]);
      } else {
        result = null;
      }
      return result;
    }

    protected void onPostExecute(final ApiResult result) {
      if (CategoriesFragment.this.activity != null && result.isSuccess()) {
        final Toast toast = Toast.makeText(CategoriesFragment.this.activity, "Category added to favorites!", Toast.LENGTH_SHORT);
        toast.show();
        CategoriesFragment.this.loadCategories();
      }
    }
  }


  private class RemoveFromFavoritesTask extends AsyncTask<String, Void, ApiResult> {

    @SuppressLint("UseValueOf")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected ApiResult doInBackground(final String... params) {
      final ApiResult result;
      if (CategoriesFragment.this.activity != null) {
        result = CategoriesFragment.this.application.getSession().getApi().unsubscribeCategory(params[0]);
      } else {
        result = null;
      }
      return result;
    }

    protected void onPostExecute(final ApiResult result) {
      if (CategoriesFragment.this.activity != null && result.isSuccess()) {
        final Toast toast = Toast.makeText(CategoriesFragment.this.activity, "Category removed to favorites!", Toast.LENGTH_SHORT);
        toast.show();
        CategoriesFragment.this.loadCategories();
      }
    }
  }
}
