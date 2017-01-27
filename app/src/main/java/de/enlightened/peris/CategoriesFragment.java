package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.Vector;

@SuppressLint("NewApi")
public class CategoriesFragment extends ListFragment {
  private static final int CATEGORIES_PER_PAGE = 20;
  private static final int MAX_ITEM_COUNT = 50;

  private String serverAddress;
  private String subforumId = "0";
  private String background;
  private String userid;
  private Category clickedCategory;
  private String username;

  //private String hashId = "0";

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
  private String shareURL = "0";
  private FragmentActivity activity;
  private String totalHash;
  private ArrayList<Category> categoryList;
  private boolean initialParseDone = false;
  private CategorySelectedListener categorySelected = null;
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

    //Log.i("Peris", "**** New CategoriesFragment Instance ****");
    //Log.d("Peris", "Passed subforum " + this.subforumId);

    this.totalHash = this.subforumId;

    if (this.subforumId.contains("###")) {
      this.subforumParts = this.subforumId.split("###");
      Log.d("Peris", "Subforum has " + this.subforumParts.length + " parts.");
      this.subforumId = this.subforumParts[0];
      //hashId = subforumParts[1];
    } else {
      this.subforumParts = new String[1];
      this.subforumParts[0] = this.subforumId;
    }

    Log.d("Peris", "Entering subforum " + this.subforumId);

    this.serverAddress = this.application.getSession().getServer().serverAddress;

    if (getString(R.string.server_location).contentEquals("0")) {
      this.storagePrefix = this.serverAddress + "_";
    }

    this.userid = this.application.getSession().getServer().serverUserId;
    this.username = this.application.getSession().getServer().serverPassword;

    final String shareId = this.subforumId;
    //if(hashId != "0") {
    //  shareId = hashId;
    //}

    if (shareId.contentEquals("0")) {
      this.shareURL = this.application.getSession().getServer().serverAddress;
    } else {
      if (this.application.getSession().forumSystem == 1) {
        this.shareURL = this.application.getSession().getServer().serverAddress + "/viewforum.php?f=" + shareId;
      }
    }


    getListView().setOnScrollListener(this.listScrolled);

    //Log.d("Peris","CF OnStart Completed");
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

    //Log.d("Peris","CF OnResume Began");

    this.activity.getActionBar().setTitle(this.screenTitle);
    this.activity.getActionBar().setSubtitle(this.screenSubtitle);

    //activity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);


    final SharedPreferences appPreferences = this.activity.getSharedPreferences("prefs", 0);
    final String cachedForum = appPreferences.getString(this.storagePrefix + "forum" + this.subforumId, "n/a");

    if (!(cachedForum.contentEquals("n/a"))) {
      try {
        final Object[][] forumObject = GsonHelper.customGson.fromJson(cachedForum, Object[][].class);
        this.parseCachedForums(forumObject);
        Log.d("Peris", "Forum cache available, using it");
      } catch (Exception ex) {
        if (ex.getMessage() != null) {
          Log.e("Peris", ex.getMessage());
        }
      }
    }

    this.loadCategories();

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      this.activity.invalidateOptionsMenu();
    }

    //Log.d("Peris","CF OnResume Completed");

    super.onResume();
  }

  private void endCurrentlyRunning() {
    //Stop any running tasks
    if (this.categoriesDownloader != null) {
      if (this.categoriesDownloader.getStatus() == Status.RUNNING) {
        this.categoriesDownloader.cancel(true);
        Log.i("Peris", "Killed Currently Running");
      }
    }
  }

  @Override
  public final void onStop() {
    super.onStop();

    this.endCurrentlyRunning();
  }

  private void loadCategories() {
    Log.d("Peris", "CF Starting loadCategories");
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

        application.getSession().getServer().chatThread = clickedCategory.category_id;
        application.getSession().getServer().chatForum = clickedCategory.subforumId;
        application.getSession().getServer().chatName = clickedCategory.category_name;
        application.getSession().updateServer();

        chatChanged.onChatChanged(application.getSession().getServer().chatThread);
  }
  */

  @SuppressWarnings("rawtypes")
  private void parseCachedForums(final Object[][] result) {
    if (this.categoryList == null || !this.isExtraScrolling) {
      this.categoryList = new ArrayList<Category>();
    }
    int retainedPosition = getListView().getFirstVisiblePosition();

    if (!this.initialParseDone) {
      final SharedPreferences appPreferences = this.activity.getSharedPreferences("prefs", 0);
      final String savedForumPosition = appPreferences.getString(this.storagePrefix + "forumScrollPosition" + this.passedSubforum, "0");
      retainedPosition = Integer.parseInt(savedForumPosition);
    }
    //Announcement Topics
    for (Object o : result[1]) {
      if (o != null) {
        final LinkedTreeMap map = (LinkedTreeMap) o;

        if (map.containsKey("topics")) {
          final ArrayList topics = (ArrayList) map.get("topics");
          for (Object t : topics) {
            this.categoryList.add(this.createCategoryFromTopics((LinkedTreeMap) t, false, true));
          }
        }
      }
    }
    //Sticky Topics
    for (Object o : result[2]) {
      if (o != null) {
        final LinkedTreeMap map = (LinkedTreeMap) o;

        if (map.containsKey("topics")) {
          final ArrayList topics = (ArrayList) map.get("topics");
          for (Object t : topics) {
            this.categoryList.add(this.createCategoryFromTopics((LinkedTreeMap) t, false, true));
          }
        }
      }
    }

    Log.d("Peris", "Starting category parse!");
    //Forums
    if (result[0] != null) {
      ArrayList<Category> forumz = CategoryParser.parseCategories(result[0], this.subforumId, this.background);
      Log.d("Peris", "Forums parsed!");
      String currentHash = this.subforumParts[0];
      Log.d("Peris", "Hash Size: " + this.subforumParts.length);
      if (this.subforumParts.length == 1) {
        for (Category c : forumz) {
          this.categoryList.add(c);
        }
      } else {
        for (int i = 1; i < this.subforumParts.length; i++) {
          currentHash = currentHash + "###" + this.subforumParts[i];
          Log.d("Peris", "Checking hash: " + currentHash + " (total hash is " + this.totalHash + ")");
          ArrayList<Category> tempForums = null;
          for (Category c : forumz) {
            if (c.children != null && c.category_id.contentEquals(currentHash)) {
              tempForums = c.children;
            }
          }

          if (tempForums != null) {
            forumz = tempForums;

            if (currentHash.contentEquals(this.totalHash)) {
              for (Category c : forumz) {
                this.categoryList.add(c);
              }
            }
          }
        }
      }
    }
    Log.d("Peris", "Finished category parse!");
    //Non-Sticky Topics
    if (result[3] == null || result[3].length == 0) {
      this.canScrollMoreThreads = false;
    }
    for (Object o : result[3]) {
      if (o != null) {
        final LinkedTreeMap map = (LinkedTreeMap) o;

        if (map.containsKey("topics")) {
          final ArrayList topics = (ArrayList) map.get("topics");
          for (Object t : topics) {
            this.categoryList.add(this.createCategoryFromTopics((LinkedTreeMap) t, true, false));
          }
        }
      }
    }

    for (Object o : result[4]) {
      if (o != null) {
        Log.i("Peris", "We have some favs!");

        final LinkedTreeMap map = (LinkedTreeMap) o;
        if (map.containsKey("forums")) {
          final ArrayList forums = (ArrayList) map.get("forums");
          for (Object f : forums) {
            final LinkedTreeMap forumMap = (LinkedTreeMap) f;

            final Category ca = new Category();
            ca.category_name = (String) forumMap.get("forum_name");
            ca.subforum_id = this.subforumId;
            ca.category_id = (String) forumMap.get("forum_id");
            ca.categoryType = "S";
            ca.categoryColor = this.background;

            if (forumMap.containsKey("icon_url")) {
              if (forumMap.get("icon_url") != null) {
                ca.categoryIcon = (String) forumMap.get("icon_url");
              }
            }
            ca.isSubscribed = true;
            if (forumMap.get("new_post") != null) {
              ca.hasNewTopic = (Boolean) forumMap.get("new_post");
            }
            this.categoryList.add(ca);
          }
        } else {
          Log.e("Peris", "Favs has no forums!");
        }
      }
    }

    setListAdapter(new CategoryAdapter(this.categoryList, this.activity, this.application));
    registerForContextMenu(getListView());
    getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @SuppressWarnings("checkstyle:requirethis")
      public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {
        final Category sender = (Category) arg0.getItemAtPosition(arg2);

        if (sender == null) {
          return;
        }
        if (categorySelected == null) {
          return;
        }

        categorySelected.onCategorySelected(sender);
      }
    });

    getListView().setSelection(retainedPosition);
    this.initialParseDone = true;
  }

  private Category createCategoryFromTopics(final LinkedTreeMap topicMap, final boolean subforum, final boolean sticky) {
    final Category ca = new Category();
    ca.category_name = (String) topicMap.get("topic_title");

    if (subforum && topicMap.get("forum_id") != null) {
      ca.subforum_id = (String) topicMap.get("forum_id");
    } else {
      ca.subforum_id = this.subforumId;

      //if(!hashId.contentEquals("0")) {
      //  ca.subforumId = hashId;
      //}
    }
    ca.category_id = (String) topicMap.get("topic_id");
    ca.category_lastupdate = (String) topicMap.get("last_reply_time");
    if (!subforum || topicMap.get("topic_author_name") != null) {
      ca.category_lastthread = (String) topicMap.get("topic_author_name");
    } else {
      ca.category_lastthread = (String) topicMap.get("forum_name");
    }
    if (sticky) {
      ca.topicSticky = "Y";
    }
    ca.categoryType = "C";
    ca.categoryColor = this.background;

    if (topicMap.get("reply_number") != null) {
      ca.thread_count = topicMap.get("reply_number").toString().replace(".0", "");
    }

    if (topicMap.get("view_number") != null) {
      ca.view_count = topicMap.get("view_number").toString().replace(".0", "");
    }

    if (topicMap.get("new_post") != null) {
      ca.hasNewTopic = (Boolean) topicMap.get("new_post");
    }

    if (topicMap.get("is_closed") != null) {
      ca.isLocked = (Boolean) topicMap.get("is_closed");
    }

    if (topicMap.containsKey("icon_url")) {
      if (topicMap.get("icon_url") != null) {
        ca.categoryIcon = (String) topicMap.get("icon_url");
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
  }

  public final void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
    final String serverUserId = this.application.getSession().getServer().serverUserId;
    final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

    this.clickedCategory = (Category) CategoriesFragment.this.getListView().getItemAtPosition(info.position);

    if (serverUserId.contentEquals("0")) {
      return;
    }
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.setHeaderTitle(this.clickedCategory.category_name);
    final MenuInflater inflater = this.activity.getMenuInflater();

    inflater.inflate(R.menu.categories_context, menu);

    final MenuItem ubsubItem = menu.findItem(R.id.categories_unsubscribe);
    final MenuItem subItem = menu.findItem(R.id.categories_subscribe);
    final MenuItem stickyItem = menu.findItem(R.id.categories_context_sticky);
    final MenuItem lockItem = menu.findItem(R.id.categories_context_lock);
    final MenuItem deleteItem = menu.findItem(R.id.categories_context_delete);

    final MenuItem subscribeItem = menu.findItem(R.id.categories_add_favorite);
    final MenuItem unsubscribeItem = menu.findItem(R.id.categories_remove_favorite);

    if (this.clickedCategory.categoryType.contentEquals("S")) {
      ubsubItem.setVisible(false);
      subItem.setVisible(false);
      stickyItem.setVisible(false);
      lockItem.setVisible(false);
      deleteItem.setVisible(false);

      if (this.clickedCategory.canSubscribe) {
        subscribeItem.setVisible(true);
      } else {
        subscribeItem.setVisible(false);
      }

      if (this.clickedCategory.isSubscribed) {
        unsubscribeItem.setVisible(true);
        subscribeItem.setVisible(false);
      } else {
        unsubscribeItem.setVisible(false);
      }
    } else {

      unsubscribeItem.setVisible(false);
      subscribeItem.setVisible(false);


      if (this.clickedCategory.canSticky) {
        stickyItem.setVisible(true);

        if (this.clickedCategory.topicSticky.contentEquals("N")) {
          stickyItem.setTitle("Stick Topic");
        } else {
          stickyItem.setTitle("Unstick Topic");
        }
      } else {
        stickyItem.setVisible(false);
      }

      if (this.clickedCategory.canDelete) {
        deleteItem.setVisible(true);
      } else {
        deleteItem.setVisible(false);
      }

      if (this.clickedCategory.canLock) {
        lockItem.setVisible(true);

        if (this.clickedCategory.isLocked) {
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
        new UnsubscribeTopicTask().execute(this.clickedCategory.category_id);
        break;
      case R.id.categories_subscribe:
        new SubscribeTopicTask().execute(this.clickedCategory.category_id);
        break;
      case R.id.categories_context_sticky:
        if (this.clickedCategory.topicSticky.contentEquals("N")) {
          new StickyTopicTask().execute(this.clickedCategory.category_id, "1");
        } else {
          new StickyTopicTask().execute(this.clickedCategory.category_id, "2");
        }
        break;
      case R.id.categories_context_lock:
        if (this.clickedCategory.isLocked) {
          new LockTopicTask().execute(this.clickedCategory.category_id, "1");
        } else {
          new LockTopicTask().execute(this.clickedCategory.category_id, "2");
        }
        break;
      case R.id.categories_context_delete_yes:
        new DeleteTopicTask().execute(this.clickedCategory.category_id);
        break;
      case R.id.categories_add_favorite:
        new AddToFavoritesTask().execute(this.clickedCategory.category_id);
        break;
      case R.id.categories_remove_favorite:
        new RemoveFromFavoritesTask().execute(this.clickedCategory.category_id);
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
      if (!this.userid.contentEquals("0")) {
        inflater.inflate(R.menu.categories_menu, menu);
      }
    }

    super.onCreateOptionsMenu(menu, inflater);

  }

  @Override
  public final void onPrepareOptionsMenu(final Menu menu) {
    super.onPrepareOptionsMenu(menu);

    if ((this.userid != null) && !this.userid.contentEquals("0") && (menu != null)) {
      if (this.subforumId.contentEquals("0")
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
          if (ForegroundColorSetter.getForegroundDark(this.background)) {
            item.setIcon(R.drawable.ic_action_read_dark);
          }
        }
      }

      if (this.subforumId.contentEquals("0")
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
          if (ForegroundColorSetter.getForegroundDark(this.background)) {
            item2.setIcon(R.drawable.ic_action_new_dark);
          }
        }
      }

      final MenuItem browserItem = menu.findItem(R.id.cat_open_browser);

      if (this.shareURL.contentEquals("0")) {
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
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.shareURL));
        this.startActivity(browserIntent);
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  private void startPost() {

    if (this.subforumId.contentEquals("0") || this.userid.contentEquals("0")) {
      final Toast toast = Toast.makeText(this.activity, "You are not allowed to post here!", Toast.LENGTH_LONG);
      toast.show();
      return;
    }

    final Intent myIntent = new Intent(this.activity, New_Post.class);

    final Bundle bundle = new Bundle();
    bundle.putString("postid", (String) "0");
    bundle.putString("parent", (String) "0");
    bundle.putString("category", (String) this.subforumId);
    bundle.putString("subforum_id", (String) this.subforumId);
    bundle.putString("original_text", (String) "");
    bundle.putString("boxTitle", (String) "New Thread");
    bundle.putString("picture", (String) "0");
    bundle.putString("subject", (String) "");
    bundle.putInt("post_type", (Integer) 1);
    bundle.putString("color", (String) this.background);
    myIntent.putExtras(bundle);

    startActivity(myIntent);

  }

  public final void setOnCategorySelectedListener(final CategorySelectedListener l) {
    this.categorySelected = l;
  }

  private void markAsRead() {
    new ReadMarkerTask().execute(this.subforumId);
  }

  //Category Selected Interface
  public interface CategorySelectedListener {
    void onCategorySelected(Category ca);
  }

  private class DownloadCategoriesTask extends AsyncTask<String, Void, Object[][]> {

    @Override
    protected void onPreExecute() {
      Log.i("Peris", "DownloadCategoriesTask onPreExecute");
      super.onPreExecute();
    }

    @SuppressLint("UseValueOf")
    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected Object[][] doInBackground(final String... params) {
      Log.i("Peris", "DownloadCategoriesTask doInBackground");
      if (activity == null) {
        Log.e("Peris", "Category activity is null!");
        return null;
      }
      isLoading = true;
      final Object[][] result = new Object[5][MAX_ITEM_COUNT];
      if (subforumId.contentEquals("favs")) {
        //Handle subscription category
        try {
          final Vector paramz = new Vector();
          paramz.addElement(startingPos);
          paramz.addElement(endingPos);
          result[3][0] = application.getSession().performNewSynchronousCall("get_subscribed_topic", paramz);
        } catch (Exception ex) {
          if (ex.getMessage() != null) {
            Log.w("Peris", ex.getMessage());
          }
        }
      } else if (subforumId.contentEquals("forum_favs")) {
        //Handle favorites category
        try {
          final Vector paramz = new Vector();
          //result[0] = (Object[]) client.execute("get_subscribed_forum", paramz);
          result[4][0] = application.getSession().performNewSynchronousCall("get_subscribed_forum", paramz);
        } catch (Exception ex) {
          if (ex.getMessage() != null) {
            Log.w("Peris", "Favorites Error: " + ex.getMessage());
          }
        }
      } else if (subforumId.contentEquals("participated")) {
        //Handle participated topics category
        try {
          final Vector paramz = new Vector();
          paramz.addElement(username.getBytes());
          paramz.addElement(startingPos);
          paramz.addElement(endingPos);
          paramz.addElement("");
          paramz.addElement(userid);
          result[3][0] = application.getSession().performNewSynchronousCall("get_participated_topic", paramz);
        } catch (Exception ex) {
          if (ex.getMessage() != null) {
            Log.w("Peris", ex.getMessage());
          }
        }
      } else if (subforumId.contentEquals("search")) {
        //Handle topic listing for the Search function
        try {
          final Vector paramz = new Vector();
          paramz.addElement(searchQuery.getBytes());
          paramz.addElement(startingPos);
          paramz.addElement(endingPos);
          result[3][0] = application.getSession().performNewSynchronousCall("search_topic", paramz);
        } catch (Exception ex) {
          if (ex.getMessage() != null) {
            Log.w("Peris", ex.getMessage());
          }
        }
      } else if (subforumId.contentEquals("timeline")) {
          //Handle timeline get_latest_topic
          try {
            final Vector paramz = new Vector();
            //paramz.addElement(username.getBytes());
            paramz.addElement(startingPos);
            paramz.addElement(endingPos);
            //paramz.addElement(userid);
            result[3][0] = application.getSession().performNewSynchronousCall("get_latest_topic", paramz);
          } catch (Exception ex) {
            if (ex.getMessage() != null) {
              Log.w("Peris", ex.getMessage());
            }
          }
      } else if (subforumId.contentEquals("unread")) {
        //Handle topic listing for the Unread category
        if (!isExtraScrolling) {
          try {
            final Vector paramz = new Vector();
            result[3][0] = application.getSession().performNewSynchronousCall("get_unread_topic", paramz);
          } catch (Exception ex) {
            if (ex.getMessage() != null) {
              Log.w("Peris", ex.getMessage());
            }
          }
        }
      } else if (!subforumId.contentEquals("userrecent")) {
          //Do not get a forum listing if we are inside one of the special sections
        if (!isExtraScrolling) {
          try {
            final Vector paramz = new Vector();

            if (!subforumId.contentEquals("0")) {
              paramz.addElement(new Boolean(true));
              paramz.addElement(subforumId);
            }
            result[0] = (Object[]) application.getSession().performNewSynchronousCall("get_forum", paramz);
            if (result[0] == null) {
              Log.e("Peris", "shits null on " + subforumId);
            }
          } catch (Exception ex) {
            if (ex.getMessage() != null) {
              Log.w("Peris", ex.getMessage());
            }
          }
          try {
            //First grab any announcement topics
            final Vector paramz = new Vector();
            paramz.addElement(subforumId);
            paramz.addElement(0);
            paramz.addElement(CATEGORIES_PER_PAGE);
            paramz.addElement("ANN");
            //result[1][0] = application.getSession().performSynchronousCall("get_topic", paramz);
            result[1][0] = application.getSession().performNewSynchronousCall("get_topic", paramz);
          } catch (Exception ex) {
            if (ex.getMessage() != null) {
              Log.w("Peris", ex.getMessage());
            }
          }
          try {
            //Then grab any sticky topics
            final Vector paramz = new Vector();
            paramz.addElement(subforumId);
            paramz.addElement(0);
            paramz.addElement(CATEGORIES_PER_PAGE);
            paramz.addElement("TOP");
            //result[2][0] = application.getSession().performSynchronousCall("get_topic", paramz);
            result[2][0] = application.getSession().performNewSynchronousCall("get_topic", paramz);
          } catch (Exception ex) {
            if (ex.getMessage() != null) {
              Log.w("Peris", ex.getMessage());
            }
          }
        }
        try {
          //Grab the non-sticky topics
          Log.d("Peris", "Getting topics " + startingPos + " through " + endingPos);

          final Vector paramz = new Vector();
          paramz.addElement(subforumId);
          paramz.addElement(startingPos);
          paramz.addElement(endingPos);
          result[3][0] = application.getSession().performNewSynchronousCall("get_topic", paramz);
        } catch (Exception ex) {
          if (ex.getMessage() != null) {
            Log.w("Peris", ex.getMessage());
          }
        }
      }
      return result;
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final Object[][] result) {
      Log.i("Peris", "DownloadCategoriesTask onPostExecute");

      if (activity != null) {
        if (result == null) {
          final Toast toast = Toast.makeText(activity, "Error pulling data from the server, ecCFDL", Toast.LENGTH_SHORT);
          toast.show();
        } else {
          Log.i("Peris", "Recieved category data!");

          initialLoadComplete = true;
          isLoading = false;

          final String objectString = GsonHelper.customGson.toJson(result);
          final SharedPreferences appPreferences = activity.getSharedPreferences("prefs", 0);
          final String cachedForum = appPreferences.getString(storagePrefix + "forum" + subforumId, "n/a");

          if (!objectString.contentEquals(cachedForum)) {
            if (!isExtraScrolling) {
              final SharedPreferences.Editor editor = appPreferences.edit();
              editor.putString(storagePrefix + "forum" + subforumId, objectString);
              editor.commit();
            }

            final Object[][] forumObject = GsonHelper.customGson.fromJson(objectString, Object[][].class);
            parseCachedForums(forumObject);
          }
        }
      }
    }
  }

  private class ReadMarkerTask extends AsyncTask<String, Void, String> {

    @SuppressLint("UseValueOf")
    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected String doInBackground(final String... params) {

      if (activity == null) {
        return null;
      }
      final String result = "";
      try {
        final Vector paramz = new Vector();
        if (!params[0].contentEquals("0") && !params[0].contentEquals("unread")) {
          paramz.addElement(params[0]);
        }
        //application.getSession().performSynchronousCall("mark_all_as_read", paramz);
        application.getSession().performNewSynchronousCall("mark_all_as_read", paramz);

      } catch (Exception ex) {
        Log.w("Discussions", ex.getMessage());
      }

      return result;
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final String result) {

      if (activity != null) {
        if (subforumId.contentEquals("unread")) {
          activity.finish();
        } else {
          loadCategories();
          final Toast toast = Toast.makeText(activity, "Posts marked read!", Toast.LENGTH_LONG);
          toast.show();
        }
      }
    }
  }

  private class SubscribeTopicTask extends AsyncTask<String, Void, String> {

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
        //application.getSession().performSynchronousCall("subscribe_topic", paramz);
        application.getSession().performNewSynchronousCall("subscribe_topic", paramz);

      } catch (Exception ex) {
        Log.w("Discussions", ex.getMessage());
      }

      return "";
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final String result) {
      if (activity != null) {
        final Toast toast = Toast.makeText(activity, "Subscribed!", Toast.LENGTH_SHORT);
        toast.show();
      }
    }
  }

  private class UnsubscribeTopicTask extends AsyncTask<String, Void, String> {

    @SuppressLint("UseValueOf")
    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected String doInBackground(final String... params) {
      if (activity != null) {
        try {
          final Vector paramz = new Vector();
          paramz.addElement(params[0]);
          //application.getSession().performSynchronousCall("unsubscribe_topic", paramz);
          application.getSession().performNewSynchronousCall("unsubscribe_topic", paramz);
          return "";
        } catch (Exception ex) {
          Log.w("Discussions", ex.getMessage());
        }
      }
      return null;
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final String result) {
      if (activity != null) {
        loadCategories();
      }
    }
  }

  private class StickyTopicTask extends AsyncTask<String, Void, String> {
    // parm[0] - (string)topic_id
    // parm[1] - (int)mode (1 - stick; 2 - unstick)
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
        paramz.addElement(Integer.parseInt(params[1]));
        //application.getSession().performSynchronousCall("m_stick_topic", paramz);
        application.getSession().performNewSynchronousCall("m_stick_topic", paramz);
      } catch (Exception ex) {
        Log.w("Peris", ex.getMessage());
      }

      return "";
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final String result) {
      if (activity != null) {
        loadCategories();
      }
    }
  }

  private class LockTopicTask extends AsyncTask<String, Void, String> {

    // parm[0] - (string)topic_id
    // parm[1] - (int)mode (1 - unlock; 2 - lock)

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
        paramz.addElement(Integer.parseInt(params[1]));
        //application.getSession().performSynchronousCall("m_close_topic", paramz);
        application.getSession().performNewSynchronousCall("m_close_topic", paramz);

      } catch (Exception ex) {
        Log.w("Peris", ex.getMessage());
      }

      return "";
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final String result) {
      if (activity != null) {
        loadCategories();
      }
    }
  }

  private class DeleteTopicTask extends AsyncTask<String, Void, String> {

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
        //application.getSession().performSynchronousCall("m_delete_topic", paramz);
        application.getSession().performNewSynchronousCall("m_delete_topic", paramz);
      } catch (Exception ex) {
        Log.w("Peris", ex.getMessage());
      }

      return "";
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final String result) {
      if (activity != null) {
        loadCategories();
      }
    }
  }

  private class AddToFavoritesTask extends AsyncTask<String, Void, String> {

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
        //application.getSession().performSynchronousCall("subscribe_forum", paramz);
        application.getSession().performNewSynchronousCall("subscribe_forum", paramz);
      } catch (Exception ex) {
        Log.w("Discussions", ex.getMessage());
      }
      return "";
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final String result) {
      if (activity != null) {
        final Toast toast = Toast.makeText(activity, "Forum added to favorites!", Toast.LENGTH_SHORT);
        toast.show();
        loadCategories();
      }
    }
  }


  private class RemoveFromFavoritesTask extends AsyncTask<String, Void, String> {

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
        //application.getSession().performSynchronousCall("unsubscribe_forum", paramz);
        application.getSession().performNewSynchronousCall("unsubscribe_forum", paramz);
      } catch (Exception ex) {
        Log.w("Discussions", ex.getMessage());
      }
      return "";
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final String result) {
      if (activity != null) {
        final Toast toast = Toast.makeText(activity, "Forum removed from favorites!", Toast.LENGTH_SHORT);
        toast.show();

        loadCategories();
      }
    }
  }
}
