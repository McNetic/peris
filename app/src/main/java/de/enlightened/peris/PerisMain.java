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
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import de.enlightened.peris.site.Category;
import de.enlightened.peris.site.Topic;
import de.enlightened.peris.site.TopicItem;

@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class PerisMain extends FragmentActivity {

    private static final String TAG = PerisMain.class.getName();;
    private boolean initialLoad = true;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar actionBar;
    private FrameLayout flSecondary;
    private String serverUserid;
    private String storagePrefix = "";
    private String background;
    private View seperator;
    private String incomingText = "";
    private String serverAddress;
    private String screenTitle;
    private String screenSubtitle;
    private String baseSubtitle;
    private GestureLibrary gLib;
    private GestureOverlayView gestures;

    private int backStackId;

    private DrawerLayout mDrawerLayout;

    private PerisApp application;
    private AnalyticsHelper ah;

    private boolean sidebarOption;
    private PostsFragment.ProfileSelectedListener profileSelected = new PostsFragment.ProfileSelectedListener() {

        @Override
        @SuppressWarnings("checkstyle:requirethis")
        public void onProfileSelected(final String username, final String userid) {

            getActionBar().setSubtitle(baseSubtitle);

            Log.i(TAG, "Preparing fragment for profile " + userid);

            final Bundle bundle = new Bundle();
            bundle.putString("username", username);
            bundle.putString("userid", userid);
            loadProfile(bundle);
        }
    };
    private final CategoriesFragment.TopicItemSelectedListener categorySelected = new CategoriesFragment.TopicItemSelectedListener() {

    @SuppressWarnings("checkstyle:requirethis")
    public void onTopicItemSelected(final TopicItem category) {
      loadTopicItem(category);
    }
  };
  private final ActiveList.OnProfileSelectedListener profileActiveSelected = new ActiveList.OnProfileSelectedListener() {

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onProfileSelected(final String username, final String userid) {

      final Bundle bundle = new Bundle();
      bundle.putString("username", username);
      bundle.putString("userid", userid);

      loadProfile(bundle);

      if (seperator == null) {
        final DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (dl.isDrawerOpen(flSecondary)) {
          dl.closeDrawer(flSecondary);
        }
      }
    }
  };
  private SettingsFragment.ProfileSelectedListener myProfileSelected = new SettingsFragment.ProfileSelectedListener() {

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onProfileSelected(final String username, final String userid) {

      final Bundle bundle = new Bundle();
      bundle.putString("username", username);
      bundle.putString("userid", userid);

      loadProfile(bundle);

      final DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
      final FrameLayout flDrawer = (FrameLayout) findViewById(R.id.left_drawer);

      if (dl.isDrawerOpen(flDrawer)) {
        dl.closeDrawer(flDrawer);
      }
    }
  };
  private SocialFragment.ProfileSelectedListener profileSocialSelected = new SocialFragment.ProfileSelectedListener() {

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onProfileSelected(final String username, final String userid) {

      final Bundle bundle = new Bundle();
      bundle.putString("username", username);
      bundle.putString("userid", userid);
      loadProfile(bundle);

      if (seperator == null) {
        final DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (dl.isDrawerOpen(flSecondary)) {
          dl.closeDrawer(flSecondary);
        }
      }
    }
  };
  private SettingsFragment.IndexRequestedListener goToIndex = new SettingsFragment.IndexRequestedListener() {

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onIndexRequested() {

      final SharedPreferences appPreferences = getSharedPreferences("prefs", 0);
      final String currentServerId = application.getSession().getServer().serverId;
      final String keyName = currentServerId + "_home_page";
      final String valueName = appPreferences.getString(keyName, getString(R.string.subforum_id));

      if (valueName.contentEquals(getString(R.string.subforum_id))) {

        final BackStackManager.BackStackItem item = application.getStackManager().navigateToBase(backStackId);

        switch (item.getType()) {
          case BackStackManager.BackStackItem.BACKSTACK_TYPE_FORUM:
            loadCategory(item.getBundle(), "SETTINGS_INDEX_REQUESTED", false);
            break;
          case BackStackManager.BackStackItem.BACKSTACK_TYPE_TOPIC:
            loadTopic(item.getBundle());
            break;
          case BackStackManager.BackStackItem.BACKSTACK_TYPE_PROFILE:
            loadProfile(item.getBundle());
            break;
          case BackStackManager.BackStackItem.BACKSTACK_TYPE_SETTINGS:
            loadSettings();
            break;
          default:
            Log.d(TAG, "Unknown backstack item type " + item.getType());
        }
      } else {
        final Category category = Category.builder()
            .id(getString(R.string.subforum_id))
            .name("Forums")
            .build();
        loadTopicItem(category);
      }

      final DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
      final FrameLayout flDrawer = (FrameLayout) findViewById(R.id.left_drawer);

      if (dl.isDrawerOpen(flDrawer)) {
        dl.closeDrawer(flDrawer);
      }
    }

  };
  private SettingsFragment.SettingsRequestedListener settingsRequested = new SettingsFragment.SettingsRequestedListener() {

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onSettingsRequested() {
      loadSettings();
    }
  };
  private SettingsFragment.CategorySelectedListener settingsCategorySelected = new SettingsFragment.CategorySelectedListener() {

    @SuppressWarnings("checkstyle:requirethis")
    public void onCategorySelected(final Category category) {
      loadTopicItem(category);
      final DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
      final FrameLayout flDrawer = (FrameLayout) findViewById(R.id.left_drawer);

      if (dl.isDrawerOpen(flDrawer)) {
        dl.closeDrawer(flDrawer);
      }
    }
  };
  private OnGesturePerformedListener handleGestureListener = new OnGesturePerformedListener() {
    @SuppressWarnings("checkstyle:requirethis")
    public void onGesturePerformed(final GestureOverlayView gestureView, final Gesture gesture) {
      final ArrayList<Prediction> predictions = gLib.recognize(gesture);
/*
      // one prediction needed
      if (predictions.size() > 0) {
        final Prediction prediction = predictions.get(0);
        // checking prediction
        if (prediction.score > 1.0) {

          if (prediction.name.contains("reload")) {
            // perform reload action
          }

          if (prediction.name.contains("logout")) {
            // perform logout action
          }

          if (prediction.name.contains("prev")) {
            // perform next action
          }

          if (prediction.name.contains("new")) {
            // perform new action
          }

          if (prediction.name.contains("next")) {
            // perform next action
          }
        }
      }
*/
    }
  };

  public static boolean isNumeric(final String str) {
    try {
      final double d = Double.parseDouble(str);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  /**
   * Called when the activity is first created.
   */
  @SuppressLint("NewApi")
  @Override
  public final void onCreate(final Bundle savedInstanceState) {
    this.application = (PerisApp) getApplication();
    this.application.setActive(true);
    this.backStackId = this.application.getBackStackId();
    this.ah = this.application.getAnalyticsHelper();

    if (this.application.getSession().getServer().serverIcon == null) {
      final int optimalIconSize = (int) this.getResources().getDimension(android.R.dimen.app_icon_size);
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
        new CheckForumIconTask(this.application.getSession(), optimalIconSize).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      } else {
        new CheckForumIconTask(this.application.getSession(), optimalIconSize).execute();
      }
    }
    this.serverAddress = this.application.getSession().getServer().serverAddress;
    final SharedPreferences appPreferences = getSharedPreferences("prefs", 0);
    this.sidebarOption = appPreferences.getBoolean("show_sidebar", true);
    this.screenTitle = getString(R.string.app_name);

    if (getString(R.string.server_location).contentEquals("0")) {
      this.storagePrefix = this.serverAddress + "_";
      this.screenSubtitle = this.serverAddress;
    } else {
      this.screenSubtitle = this.screenTitle;
    }
    this.serverUserid = this.application.getSession().getServer().serverUserId;
    final String tagline = this.application.getSession().getServer().serverTagline;
    final SharedPreferences.Editor editor = appPreferences.edit();

    if (tagline.contentEquals("null") || tagline.contentEquals("0")) {
      final String deviceName = android.os.Build.MODEL;
      final String appName = getString(R.string.app_name);
      final String appVersion = getString(R.string.app_version);
      String appColor = getString(R.string.default_color);

      if (this.application.getSession().getServer().serverColor.contains("#")) {
        appColor = this.application.getSession().getServer().serverColor;
      }
      final String standardTagline = "[color=" + appColor + "][b]Sent from my " + deviceName + " using " + appName + " v" + appVersion + ".[/b][/color]";
      this.application.getSession().getServer().serverTagline = standardTagline;
      this.application.getSession().updateServer();
    }
    editor.putInt(this.storagePrefix + "just_logged_in", 0);
    editor.commit();

    if (this.serverUserid != null) {
      final Toast toast = Toast.makeText(PerisMain.this, "TIP: Tap on the key icon to log in to your forum account.", Toast.LENGTH_LONG);
      toast.show();
    }
    final Intent intent = getIntent();
    final String action = intent.getAction();
    final String type = intent.getType();

    if (Intent.ACTION_SEND.equals(action) && type != null) {
      if ("text/plain".equals(type)) {
        this.handleSendText(intent); // Handle text being sent
      } else if (type.startsWith("image/")) {
        this.handleSendImage(intent); // Handle single image being sent
      }
    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
      if (type.startsWith("image/")) {
        this.handleSendMultipleImages(intent); // Handle multiple images being sent
      }
    } /*else {
      // Handle other intents, such as being started from the home screen
    }*/

    this.background = this.application.getSession().getServer().serverColor;
    ThemeSetter.setTheme(this, this.background);
    super.onCreate(savedInstanceState);
    ThemeSetter.setActionBar(this, this.background);
    this.actionBar = getActionBar();
    this.actionBar.setDisplayHomeAsUpEnabled(true);
    this.actionBar.setHomeButtonEnabled(true);
    this.actionBar.setTitle(this.screenTitle);
    this.actionBar.setSubtitle(this.screenSubtitle);

    //Send app analytics data
    this.ah.trackScreen(getClass().getSimpleName(), false);
    this.ah.trackEvent("server connection", "connected", "connected", false);

    //Send tracking data for parsed analytics from peris.json
    this.serverAddress = this.application.getSession().getServer().analyticsId;
    if (this.serverAddress != null) {
      this.ah.trackCustomScreen(this.serverAddress, "Peris Forum Reader v" + getString(R.string.app_version) + " for Android");
    }
    setContentView(R.layout.main_swipe);

    this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    this.flSecondary = (FrameLayout) findViewById(R.id.main_page_frame_right);
    this.seperator = findViewById(R.id.main_page_seperator);

    //Setup forum background
    final String forumWallpaper = this.application.getSession().getServer().serverWallpaper;
    final String forumBackground = this.application.getSession().getServer().serverBackground;
    if (forumBackground != null && forumBackground.contains("#") && forumBackground.length() == 7) {
      this.mDrawerLayout.setBackgroundColor(Color.parseColor(forumBackground));
    } else {
      this.mDrawerLayout.setBackgroundColor(Color.parseColor(getString(R.string.default_background)));
    }

    if (forumWallpaper != null && forumWallpaper.contains("http")) {
      final ImageView mainSwipeImageBackground = (ImageView) findViewById(R.id.main_swipe_image_background);
      final String imageUrl = forumWallpaper;
      ImageLoader.getInstance().displayImage(imageUrl, mainSwipeImageBackground);
    } else {
      findViewById(R.id.main_swipe_image_background).setVisibility(View.GONE);
    }

    this.setupSlidingDrawer();

    if (this.application.getStackManager().getBackStackSize(this.backStackId) == 0) {
      final Bundle bundle = this.initializeNewSession(appPreferences);
      this.loadCategory(bundle, "NEW_SESSION", false);
      //application.stackManager.addToBackstack(backStackId, BackStackManager.BackStackItem.BACKSTACK_TYPE_FORUM,bundle);
    } else {
      this.recoverBackstack();
    }

    final Bundle parms = getIntent().getExtras();
    if (parms != null) {
      if (parms.containsKey("stealing")) {
        final Boolean stealing = parms.getBoolean("stealing");

        if (stealing) {
          final String stealingLocation = parms.getString("stealing_location", "0");
          final String stealingType = parms.getString("stealing_type", "0");
          final boolean locationNumeric = isNumeric(stealingLocation);

          if (stealingType.contentEquals("forum") && locationNumeric && !stealingLocation.contentEquals("0")) {
            this.loadTopicItem(Category.builder()
                .id(stealingLocation)
                .name("External Link")
                .build());
          }
          if (stealingType.contentEquals("topic") && locationNumeric && !stealingLocation.contentEquals("0")) {
            this.loadTopicItem(Topic.builder()
                .id(stealingLocation)
                .title("External Link")
                .build());
          }
        }
      }
    }
    //Juice up gesture listener
    this.enableGestures();
  }

  private Bundle initializeNewSession(final SharedPreferences appPreferences) {
    Log.d(TAG, "Back stack is blank, new session");
    final Bundle bundle = new Bundle();
    final String currentServerId = this.application.getSession().getServer().serverId;
    final String keyName = currentServerId + "_home_page";
    final String valueName = appPreferences.getString(keyName, getString(R.string.subforum_id));

    String baseName = this.application.getSession().getServer().serverName;
    if (baseName.contentEquals("0")) {
      baseName = "Forums";
    }
    if (valueName.contentEquals(getString(R.string.subforum_id))) {
      baseName = "Forums";
    }
    if (valueName.contentEquals("forum_favs")) {
      baseName = "Favorites";
    }
    if (valueName.contentEquals("participated")) {
      baseName = "Participated Topics";
    }
    if (valueName.contentEquals("favs")) {
      baseName = "Subscribed Topics";
    }
    if (valueName.contentEquals("unread")) {
      baseName = "Unread Topics";
    }
    bundle.putString("subforum_id", valueName);
    bundle.putString("subforum_name", baseName);
    bundle.putString("inTab", "N");
    bundle.putString("background", this.background);
    return bundle;
  }

  private void enableGestures() {
    this.gLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
    if (!this.gLib.load()) {
      Log.w(TAG, "could not load gesture library");
      finish();
    }

    this.gestures = (GestureOverlayView) findViewById(R.id.gestures);
    this.gestures.addOnGesturePerformedListener(this.handleGestureListener);

    if (this.application.getSession().getServer().serverColor.contains("#")) {
      this.gestures.setUncertainGestureColor(Color.TRANSPARENT);
      this.gestures.setGestureColor(Color.parseColor(this.application.getSession().getServer().serverColor.replace("#", "#33")));
    }

    this.gestures.setEnabled(false);
  }

  private void recoverBackstack() {
    Log.d(TAG, "Recovering old backstack session");
    final BackStackManager.BackStackItem item = this.application.getStackManager().getActiveItemAndRemove(this.backStackId);

    switch (item.getType()) {
      case BackStackManager.BackStackItem.BACKSTACK_TYPE_FORUM:
        this.loadCategory(item.getBundle(), "BACKSTACK_RECOVERY", true);
        break;
      case BackStackManager.BackStackItem.BACKSTACK_TYPE_TOPIC:
        this.loadTopic(item.getBundle());
        break;
      case BackStackManager.BackStackItem.BACKSTACK_TYPE_PROFILE:
        this.loadProfile(item.getBundle());
        break;
      case BackStackManager.BackStackItem.BACKSTACK_TYPE_SETTINGS:
        this.loadSettings();
        break;
      default:
    }
  }

  @Override
  public final void onStart() {
    super.onStart();

  }

  @Override
  public final void onStop() {
    super.onStop();

  }

  public final void onDestroy() {
    if (!getString(R.string.server_location).contentEquals("0")) {
      final SharedPreferences appPreferences = getSharedPreferences("prefs", 0);
      final Editor editor = appPreferences.edit();
      editor.putBoolean("ff_clean_close", true);
      editor.commit();
    }
    this.application.setActive(false);

    try {
      super.onDestroy();
    } catch (RuntimeException e) {
      Log.d(TAG, e.getMessage());
    }
  }

  public final void onResume() {
    if (!this.initialLoad) {
      if (this.application.isActive()) {
        this.application.getSession().setSessionListener(null);
        this.application.getSession().refreshLogin();
      }
    }
    super.onResume();
    this.setupSidebar();
    this.initialLoad = false;
  }

  @Override
  protected final void onPostCreate(final Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    this.mDrawerToggle.syncState();
  }

  private void setupSlidingDrawer() {
    final FrameLayout flDrawer = (FrameLayout) findViewById(R.id.left_drawer);
    flDrawer.setBackgroundColor(Color.parseColor("#ffffff"));
    if (this.seperator == null) {
      this.flSecondary.setBackgroundColor(Color.parseColor("#dddddd"));
    }

    final SettingsFragment setf = new SettingsFragment();
    setf.setOnIndexRequestedListener(this.goToIndex);
    setf.setOnProfileSelectedListener(this.myProfileSelected);
    setf.setOnSettingsRequestedListener(this.settingsRequested);
    setf.setOnCategorySelectedListener(this.settingsCategorySelected);

    final Bundle bundle = new Bundle();
    bundle.putString("background", this.background);
    setf.setArguments(bundle);

    final FragmentManager fragmentManager = getSupportFragmentManager();
    final FragmentTransaction ftZ = fragmentManager.beginTransaction();
    ftZ.replace(R.id.left_drawer, setf);
    ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ftZ.commit();

    this.mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    this.mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_right, GravityCompat.END);

    this.mDrawerToggle = new ActionBarDrawerToggle(
        this,                  /* host Activity */
        this.mDrawerLayout,
        R.drawable.ic_drawer, /* DrawerLayout object */
        R.string.drawer_open,  /* "open drawer" description */
        R.string.drawer_close  /* "close drawer" description */
    ) {

      /** Called when a drawer has settled in a completely closed state. */
      public void onDrawerClosed(final View view) {
        super.onDrawerClosed(view);
        //getActionBar().setTitle(screenTitle);
        //getActionBar().setSubtitle(screenSubtitle);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          invalidateOptionsMenu();
        }
      }

      /** Called when a drawer has settled in a completely open state. */
      public void onDrawerOpened(final View drawerView) {
        super.onDrawerOpened(drawerView);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          invalidateOptionsMenu();
        }
      }
    };
    this.mDrawerToggle.setDrawerIndicatorEnabled(true);
    // Set the drawer toggle as the DrawerListener
    this.mDrawerLayout.setDrawerListener(this.mDrawerToggle);
  }

  public final void onPause() {
    final DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
    final FrameLayout flDrawer = (FrameLayout) findViewById(R.id.left_drawer);

    if (this.seperator == null) {
      if (dl.isDrawerOpen(this.flSecondary)) {
        dl.closeDrawer(this.flSecondary);
      }
    }
    if (dl.isDrawerOpen(flDrawer)) {
      dl.closeDrawer(flDrawer);
    }
    super.onPause();
  }

  @Override
  public final boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    final MenuItem searchMenuItem = menu.findItem(R.id.search);
    final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

    if (this.serverUserid == null || !getString(R.string.subforum_id).contentEquals("0")) {
      searchView.setVisibility(View.GONE);
    } else {
      if (ThemeSetter.getForegroundDark(this.background)) {
        searchMenuItem.setIcon(R.drawable.ic_action_search_dark);
      }
    }

    searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
      public void onFocusChange(final View view, final boolean queryTextFocused) {
        if (!queryTextFocused) {
          searchMenuItem.collapseActionView();
          searchView.setQuery("", false);
        }
      }
    });

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

      public boolean onQueryTextChange(final String newText) {
        // TODO Auto-generated method stub
        return false;
      }

      @SuppressWarnings("checkstyle:requirethis")
      public boolean onQueryTextSubmit(final String query) {
        if (getActionBar() != null) {
          getActionBar().setSubtitle(baseSubtitle);
        }
        searchMenuItem.collapseActionView();
        searchView.setQuery("", false);

        final Bundle bundle = new Bundle();
        bundle.putString("subforum_name", (String) "Search - " + query);
        bundle.putString("subforum_id", (String) "search");
        bundle.putString("query", (String) query);
        bundle.putString("background", (String) background);
        bundle.putString("icon", (String) "n/a");
        bundle.putString("inTab", (String) "N");
        loadCategory(bundle, "SEARCH_QUERY", false);

        return false;
      }
    });
    return true;
  }

  public final boolean onPrepareOptionsMenu(final Menu menu) {
    super.onPrepareOptionsMenu(menu);

    final String customChatForum = this.application.getSession().getServer().chatForum;
    final String customChatThread = this.application.getSession().getServer().chatThread;

    final MenuItem newMailItem = menu.findItem(R.id.main_menu_new_mail);
    newMailItem.setVisible(false);

    final MenuItem searchMenuItem = menu.findItem(R.id.search);
    if (this.serverUserid == null || !getString(R.string.subforum_id).contentEquals("0")) {
      searchMenuItem.setVisible(false);
    }

    final MenuItem itemLogin = menu.findItem(R.id.main_menu_open_login);
    final MenuItem itemChat = menu.findItem(R.id.main_menu_open_chat);

    if (this.serverUserid == null) {
      itemLogin.setVisible(true);
      itemChat.setVisible(false);
    } else {
      itemLogin.setVisible(false);

      if ((!getString(R.string.chat_thread).contentEquals("0")) || (!customChatForum.contentEquals("0") && !customChatThread.contentEquals("0"))) {
        itemChat.setVisible(true);
      }
    }

    if (this.seperator != null) {
      itemLogin.setVisible(false);
      itemChat.setVisible(false);
    }

    if (ThemeSetter.getForegroundDark(this.background)) {
      itemLogin.setIcon(R.drawable.ic_action_accounts_dark);
      itemChat.setIcon(R.drawable.ic_action_group_dark);
      newMailItem.setIcon(R.drawable.ic_action_email_dark);
    }
    return true;
  }

  public final boolean onOptionsItemSelected(final MenuItem item) {
    final DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
    final FrameLayout flDrawer = (FrameLayout) findViewById(R.id.left_drawer);

    switch (item.getItemId()) {
      case R.id.main_menu_new_mail:
        final Intent myIntent = new Intent(PerisMain.this, Mail.class);
        startActivity(myIntent);
        break;
      case android.R.id.home:

        if (dl.isDrawerOpen(flDrawer)) {
          dl.closeDrawer(flDrawer);
        } else {
          dl.openDrawer(flDrawer);
        }

        break;
      case R.id.main_menu_open_chat:
        if (dl.isDrawerOpen(this.flSecondary)) {
          dl.closeDrawer(this.flSecondary);
        } else {
          dl.openDrawer(this.flSecondary);
        }
        break;
      case R.id.main_menu_open_login:
        if (dl.isDrawerOpen(this.flSecondary)) {
          dl.closeDrawer(this.flSecondary);
        } else {
          dl.openDrawer(this.flSecondary);
        }
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  final void handleSendText(final Intent intent) {
    final String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
    if (sharedText != null) {
      this.incomingText = sharedText;
    }
  }

  final void handleSendImage(final Intent intent) {
    final Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
    /*
    if (imageUri != null) {
      // Update UI to reflect image being shared
    }*/
  }

  final void handleSendMultipleImages(final Intent intent) {
    final ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
    /*
    if (imageUris != null) {
      // Update UI to reflect multiple images being shared
    }*/
  }

  private void loadTopicItem(final TopicItem topicItem) {
    if (topicItem instanceof Topic) {
      final Topic topic = (Topic) topicItem;
      String lockString = "0";
      if (topic.isClosed()) {
        lockString = "1";
      }

      final Bundle bundle = new Bundle();
      bundle.putString("subject", topic.getTitle());
      bundle.putString("category_id", topic.getSubforumId());
      bundle.putString("subforum_id", topic.getSubforumId());
      bundle.putString("thread_id", topic.getId());
      bundle.putString("lock", topic.isClosed() ? "1" : "0");
      bundle.putString("background", (String) this.background);
      bundle.putString("posts", Integer.toString(topic.getPostCount()));
      bundle.putString("moderator", (String) topic.getAuthorName());

      Log.d(TAG, "Loading topic " + topic.getId());
      this.loadTopic(bundle);
    } else {
      final Category category = (Category) topicItem;
      if (category.getUrl() != null && !"".equals(category.getUrl())) {
        Log.d(TAG, "Found category url: " + category.getUrl());
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(category.getUrl()));
        startActivity(browserIntent);
      } else {
        final Bundle bundle = new Bundle();
        bundle.putString("subforum_name", (String) category.getName());
        bundle.putString("subforum_id", (String) category.getId());
        // TODO: get category color from where?
        //bundle.putString("background", (String) category.color);
        bundle.putString("icon", (String) category.getLogoUrl());
        bundle.putString("inTab", (String) "N");
        this.loadCategory(bundle, "LOAD_CATEGORIES", false);
      }
    }
  }

  @Override
  public final boolean onKeyDown(final int keyCode, final KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      final DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
      final FrameLayout flDrawer = (FrameLayout) findViewById(R.id.left_drawer);
      final boolean rv;

      if (this.seperator == null && dl.isDrawerOpen(this.flSecondary)) {
        dl.closeDrawer(this.flSecondary);
        rv = true;
      } else if (dl.isDrawerOpen(flDrawer)) {
        dl.closeDrawer(flDrawer);
        rv = true;
      } else if (this.application.getStackManager().getBackStackSize(this.backStackId) > 1) {
        Log.i(TAG, "Back pressed, backstack size = " + this.application.getStackManager().getBackStackSize(this.backStackId));
        final BackStackManager.BackStackItem item = this.application.getStackManager().navigateBack(this.backStackId);

        this.actionBar.setSubtitle(this.screenSubtitle);

        switch (item.getType()) {
          case BackStackManager.BackStackItem.BACKSTACK_TYPE_FORUM:
            this.loadCategory(item.getBundle(), "KEYDOWN_BACK", true);
            break;
          case BackStackManager.BackStackItem.BACKSTACK_TYPE_TOPIC:
            this.loadTopic(item.getBundle());
            break;
          case BackStackManager.BackStackItem.BACKSTACK_TYPE_PROFILE:
            this.loadProfile(item.getBundle());
            break;
          default:
            break;
        }
        rv = true;
      } else {
        rv = false;
      }
      return rv;
    }
    return super.onKeyDown(keyCode, event);
  }

  private void setupSidebar() {
    final String customChatForum = this.application.getSession().getServer().chatForum;
    final String customChatThread = this.application.getSession().getServer().chatThread;

    if (this.serverUserid != null && ((!getString(R.string.chat_thread).contentEquals("0")) || (!customChatForum.contentEquals("0") && !customChatThread.contentEquals("0")))) {
      final SocialFragment sf = new SocialFragment();
      sf.setOnProfileSelectedListener(this.profileSocialSelected);
      final Bundle bundle = new Bundle();
      bundle.putString("shared_text", this.incomingText);
      bundle.putString("background", this.background);
      sf.setArguments(bundle);

      final FragmentManager fragmentManager = getSupportFragmentManager();
      final FragmentTransaction ftZ = fragmentManager.beginTransaction();
      ftZ.replace(R.id.main_page_frame_right, sf);
      ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
      ftZ.commit();
    } else if (this.serverUserid == null) {
      final Login login = new Login();

      final FragmentManager fragmentManager = getSupportFragmentManager();
      final FragmentTransaction ftZ = fragmentManager.beginTransaction();
      ftZ.replace(R.id.main_page_frame_right, login);
      ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
      ftZ.commit();
    } else {
      if (this.sidebarOption) {
        final ActiveList active = new ActiveList();
        active.setOnProfileSelectedListener(this.profileActiveSelected);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ftZ = fragmentManager.beginTransaction();
        ftZ.replace(R.id.main_page_frame_right, active);
        ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        try {
          ftZ.commit();
        } catch (Exception ex) {
          if (ex.getMessage() != null) {
            Log.w(TAG, ex.getMessage());
          }
        }
      } else {
        this.flSecondary.setVisibility(View.GONE);
      }
    }
  }

  @Override
  public final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
  }

  private void loadCategory(final Bundle bundle, final String sender, final Boolean isBackNav) {

    if (!isBackNav) {
      final SharedPreferences appPreferences = getSharedPreferences("prefs", 0);
      final SharedPreferences.Editor editor = appPreferences.edit();
      editor.putString(this.storagePrefix + "forumScrollPosition" + bundle.getString("subforum_id"), "0");
      editor.commit();
    }

    final CategoriesFragment cf = new CategoriesFragment();
    cf.setOnCategorySelectedListener(this.categorySelected);
    cf.setArguments(bundle);

    final FragmentManager fragmentManager = getSupportFragmentManager();
    final FragmentTransaction ftZ = fragmentManager.beginTransaction();
    ftZ.replace(R.id.main_page_frame_primary, cf);
    ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ftZ.commit();

    this.application.getStackManager().addToBackstack(this.backStackId, BackStackManager.BackStackItem.BACKSTACK_TYPE_FORUM, bundle);

    Log.i(TAG, "Loading Forum from " + sender);
  }

  private void loadTopic(final Bundle bundle) {
    final PostsFragment pf = new PostsFragment();
    pf.setOnProfileSelectedListener(this.profileSelected);
    pf.setArguments(bundle);

    final FragmentManager fragmentManager = getSupportFragmentManager();
    final FragmentTransaction ftZ = fragmentManager.beginTransaction();
    ftZ.replace(R.id.main_page_frame_primary, pf);
    ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ftZ.commit();

    this.application.getStackManager().addToBackstack(this.backStackId, BackStackManager.BackStackItem.BACKSTACK_TYPE_TOPIC, bundle);
  }

  private void loadProfile(final Bundle bundle) {
    final ProfileFragment pf = new ProfileFragment();
    pf.setArguments(bundle);

    final FragmentManager fragmentManager = getSupportFragmentManager();
    final FragmentTransaction ftZ = fragmentManager.beginTransaction();
    ftZ.replace(R.id.main_page_frame_primary, pf);
    ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ftZ.commit();

    this.application.getStackManager().addToBackstack(this.backStackId, BackStackManager.BackStackItem.BACKSTACK_TYPE_PROFILE, bundle);
  }

  private void loadSettings() {
    final ForumSettingsFragment pf = new ForumSettingsFragment();

    final FragmentManager fragmentManager = getSupportFragmentManager();
    final FragmentTransaction ftZ = fragmentManager.beginTransaction();
    ftZ.replace(R.id.main_page_frame_primary, pf);
    ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ftZ.commit();

    final DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
    final FrameLayout flDrawer = (FrameLayout) findViewById(R.id.left_drawer);

    if (dl.isDrawerOpen(flDrawer)) {
      dl.closeDrawer(flDrawer);
    }

    this.application.getStackManager().addToBackstack(this.backStackId, BackStackManager.BackStackItem.BACKSTACK_TYPE_SETTINGS, null);
  }
}
