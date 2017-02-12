package de.enlightened.peris;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class SettingsFragment extends Fragment {

  private static final String TAG = SettingsFragment.class.getName();
  private static final int MAX_ITEM_COUNT = 50;

  private boolean editingProfile = false;
  private String storagePrefix = "";
  private String serverAddress;
  private int unreadMail = 0;
  private PerisApp application;
  private String background;
  private ListView lvMain;
  private ArrayList<Setting> settingsOptions;
  private ProfileSelectedListener profileSelected = null;
  private IndexRequestedListener indexRequested = null;
  private SettingsRequestedListener settingsRequested = null;
  private CategorySelectedListener categorySelected = null;

  @Override
  public void onCreate(final Bundle bundle) {
    super.onCreate(bundle);
    this.application = (PerisApp) getActivity().getApplication();
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View v = inflater.inflate(R.layout.settings_fragment, container, false);
    return v;
  }

  @Override
  public void onStart() {
    super.onStart();
    this.juiceUpMenu();
  }

  public void onResume() {
    this.unreadMail = 0;

    if (this.editingProfile) {
      this.getActivity().finish();
      this.getActivity().startActivity(this.getActivity().getIntent());
    }

    super.onResume();

    if (!this.application.getSession().getServer().serverUserId.contentEquals("0")) {
      new CheckUnreadMailTask().execute();
    }
  }

  private void themeChanger() {
    final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);
    int themeInt = appPreferences.getInt(this.storagePrefix + "loggedThemeInt", Integer.parseInt(getString(R.string.default_theme)));

    themeInt++;
    if (themeInt > 6) {
      themeInt = 4;
    }

    this.application.getSession().getServer().serverTheme = Integer.toString(themeInt);
    this.application.getSession().updateServer();

    this.getActivity().finish();
    this.getActivity().startActivity(this.getActivity().getIntent());
  }

  private void changeTextSettings() {
    final TextDialogFragment newFragment = TextDialogFragment.newInstance();
    newFragment.show(this.getActivity().getSupportFragmentManager(), "dialog");
  }

  private void setAccentColor() {
    final ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
    newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.ColorSelectedListener() {

      @SuppressWarnings("checkstyle:requirethis")
      public void onColorSelected(final String color) {
        setColor(color);
      }
    });
    newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
  }

  private void setColor(final String color) {

    this.application.getSession().getServer().serverColor = color;
    this.application.getSession().updateServer();

    this.getActivity().finish();
    this.getActivity().startActivity(this.getActivity().getIntent());
  }

  private void clearCache() {
    CacheNuker.nukeCache(this.getActivity());
    final Intent intro = new Intent(this.getActivity(), IntroScreen.class);
    this.getActivity().finish();

    if (!getString(R.string.server_location).contentEquals("0")) {
      this.getActivity().startActivity(intro);
    }
  }

  @SuppressWarnings("rawtypes")
  private void doLogout() {
    final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);

    try {
      final Vector paramz = new Vector();
      this.application.getSession().performSynchronousCall("logout_user", paramz);
    } catch (Exception ex) {
      Log.d(TAG, ex.getMessage());
    }

    final SharedPreferences.Editor editor = appPreferences.edit();
    editor.putInt("cookie_count", 0);
    editor.putString(this.storagePrefix + "logged_userid", "0");
    editor.putString(this.storagePrefix + "logged_password", "0");
    editor.putString(this.storagePrefix + "logged_userlevel", "0");
    editor.putString(this.storagePrefix + "logged_modpower", "0");
    editor.putString(this.storagePrefix + "logged_postcount", "0");
    editor.putString(this.storagePrefix + "logged_bgColor", getString(R.string.default_color));
    editor.putInt(this.storagePrefix + "loggedThemeInt", Integer.parseInt(getString(R.string.default_theme)));
    editor.putInt(this.storagePrefix + "last_main_tab", 0);
    editor.commit();
    getActivity().finish();
    getActivity().startActivity(getActivity().getIntent());
  }

  private void taglineEditor() {
    final Bundle bundle = new Bundle();
    bundle.putString("postid", "0");
    bundle.putString("parent", "0");
    bundle.putString("category", "0");
    bundle.putString("subforum_id", (String) "0");
    bundle.putString("original_text", (String) "");
    bundle.putString("boxTitle", (String) "Signature Editor");
    bundle.putString("picture", (String) "0");
    bundle.putString("color", (String) this.background);
    bundle.putString("subject", (String) "");
    bundle.putInt("post_type", (Integer) 6);
    final Intent myIntent = new Intent(this.getActivity(), NewPost.class);
    myIntent.putExtras(bundle);

    startActivity(myIntent);
  }

  private void launchUsersList() {
    final SharedPreferences appPreferences = this.getActivity().getSharedPreferences("prefs", 0);
    final String accent;
    if (appPreferences.getString(this.storagePrefix + "logged_bgColor", getString(R.string.default_color)).contains("#")) {
      accent = appPreferences.getString(this.storagePrefix + "logged_bgColor", getString(R.string.default_color));
    } else {
      accent = getString(R.string.default_color);
    }
    final Intent myIntent = new Intent(this.getActivity(), ActiveUsersActivity.class);
    final Bundle bundle = new Bundle();
    bundle.putString("background", (String) accent);
    myIntent.putExtras(bundle);
    this.startActivity(myIntent);
  }

  private void loadMyWall() {
    final String userid = this.application.getSession().getServer().serverUserId;
    final String username = this.application.getSession().getServer().serverUserName;

    if (this.profileSelected != null) {
      this.profileSelected.onProfileSelected(username, userid);
    }
  }

  private void setupUserCard() {
    if (this.getActivity() == null) {
      return;
    }

    final LinearLayout userLayout = (LinearLayout) getActivity().findViewById(R.id.settings_user_box);
    if (this.application.getSession().getServer().serverUserId.contentEquals("0")) {
      userLayout.setVisibility(View.GONE);
    } else {
      final ImageView ivAvatar = (ImageView) getActivity().findViewById(R.id.settings_user_avatar);
      final TextView tvUsername = (TextView) getActivity().findViewById(R.id.settings_user_name);
      final ImageView ivLogout = (ImageView) getActivity().findViewById(R.id.settings_user_logout);

      tvUsername.setText(this.application.getSession().getServer().serverUserName);
      if (this.application.getSession().getServer().serverAvatar.contains("http")) {
        ImageLoader.getInstance().displayImage(this.application.getSession().getServer().serverAvatar, ivAvatar);
      } else {
        ivAvatar.setImageResource(R.drawable.no_avatar);
      }

      ivLogout.setOnClickListener(new View.OnClickListener() {

        @Override
        @SuppressWarnings("checkstyle:requirethis")
        public void onClick(final View v) {
            logOut();
        }
      });
      userLayout.setOnClickListener(new View.OnClickListener() {

        @Override
        @SuppressWarnings("checkstyle:requirethis")
        public void onClick(final View v) {
          loadMyWall();
        }
      });
    }
  }

  private void logOut() {
    this.application.getSession().logOutSession();
    final Intent intent = new Intent(this.getActivity(), IntroScreen.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    final Bundle bundle = new Bundle();
    bundle.putBoolean("reboot", true);
    intent.putExtras(bundle);

    this.startActivity(intent);
  }

  public void setOnProfileSelectedListener(final ProfileSelectedListener l) {
    this.profileSelected = l;
  }

  public void setOnIndexRequestedListener(final IndexRequestedListener l) {
    this.indexRequested = l;
  }

  public void setOnSettingsRequestedListener(final SettingsRequestedListener l) {
    this.settingsRequested = l;
  }

  public void setOnCategorySelectedListener(final CategorySelectedListener l) {
    this.categorySelected = l;
  }

  private void juiceUpMenu() {
    this.setupUserCard();

    if (this.getActivity() == null) {
      return;
    }

    this.lvMain = (ListView) getActivity().findViewById(R.id.settings_list);
    this.lvMain.setDivider(null);

    final Bundle bundle = getArguments();
    if (bundle.containsKey("background")) {
      this.background = bundle.getString("background");
    }

    this.serverAddress = this.application.getSession().getServer().serverAddress;

    if (getString(R.string.server_location).contentEquals("0")) {
      this.storagePrefix = this.serverAddress + "_";
    }

    final String userid = this.application.getSession().getServer().serverUserId;
    this.settingsOptions = this.createSettingsOptions(userid);
    this.lvMain.setAdapter(new SettingsAdapter(this.settingsOptions, getActivity()));

    this.lvMain.setTextFilterEnabled(true);
    this.lvMain.setOnItemClickListener(new OnItemClickListener() {
      @SuppressWarnings("checkstyle:requirethis")
      public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        if (settingsOptions == null) {
          return;
        }
        final String theResult = settingsOptions.get(position).settingName;

        if (theResult.contentEquals("Unread Topics")) {
          final Category ca = new Category();
          ca.name = "Unread Topics";
          ca.subforumId = "0";
          ca.id = "unread";
          ca.description = "Review all of the topics with posts you haven't seen yet.";
          ca.type = "S";
          ca.color = background;

          if (categorySelected != null) {
            categorySelected.onCategorySelected(ca);
          }
        } else if (theResult.contentEquals("Participated Topics")) {
          final Category ca = new Category();
          ca.name = "Participated Topics";
          ca.subforumId = "0";
          ca.id = "participated";
          ca.description = "Check out all of the topics that you have participated in.";
          ca.type = "S";
          ca.color = background;

          if (categorySelected != null) {
            categorySelected.onCategorySelected(ca);
          }
        } else if (theResult.contentEquals("Subscribed Topics")) {
          final Category ca = new Category();
          ca.name = "Subscribed Topics";
          ca.subforumId = "0";
          ca.id = "favs";
          ca.description = "Check out all of your subcriptions!";
          ca.type = "S";
          ca.color = background;

          if (categorySelected != null) {
            categorySelected.onCategorySelected(ca);
          }
        } else if (theResult.contentEquals("Favorites")) {
          final Category ca = new Category();
          ca.name = "Favorites";
          ca.subforumId = "0";
          ca.id = "forum_favs";
          ca.description = "Your favorite forums.";
          ca.type = "S";
          ca.color = background;

          if (categorySelected != null) {
            categorySelected.onCategorySelected(ca);
          }
        } else if (theResult.contentEquals("Timeline")) {
          final Category ca = new Category();
          ca.name = "Timeline";
          ca.subforumId = "0";
          ca.id = "timeline";
          ca.description = "The latest posts forum-wide.";
          ca.type = "S";
          ca.color = background;

          if (categorySelected != null) {
            categorySelected.onCategorySelected(ca);
          }
        } else if (theResult.contentEquals("Inbox")) {
          final Intent myIntent = new Intent(getActivity(), Mail.class);
          startActivity(myIntent);
        } else if (theResult.contentEquals("My Profile")) {
          loadMyWall();
        } else if (theResult.contentEquals("Edit Signature")) {
          taglineEditor();
        } else if (theResult.contentEquals("Active Users")) {
          launchUsersList();
        } else if (theResult.contentEquals("Toggle Theme")) {
          themeChanger();
        } else if (theResult.contentEquals("Change Theme Color")) {
          setAccentColor();
        } else if (theResult.contentEquals("License Agreement")) {
          Eula.showDisclaimer(getActivity());
        } else if (theResult.contentEquals("Clear Cache")) {
          clearCache();
        } else if (theResult.contentEquals("Logout")) {
          doLogout();
        } else if (theResult.contentEquals("Text Options")) {
          changeTextSettings();
        } else if (theResult.contentEquals("Close Forum")) {
          getActivity().finish();
        } else if (theResult.contentEquals("About")) {
          final Intent aboutIntent = new Intent(getActivity(), About.class);
          startActivity(aboutIntent);
        } else if (theResult.contentEquals("Forum Index")) {
          if (indexRequested != null) {
            indexRequested.onIndexRequested();
          }
        } else if (theResult.contentEquals("Settings")) {
          if (settingsRequested != null) {
            settingsRequested.onSettingsRequested();
          }
        }
      }
    });
  }

  private ArrayList<Setting> createSettingsOptions(final String userid) {
    final ArrayList<Setting> newSettingsOptions = new ArrayList<Setting>();
    /*if (userid.contentEquals("0")) {
      //To be implemented in future!
      //settingsOptions.add("Login");
    } else {*/
      newSettingsOptions.add(new Setting("Inbox", R.drawable.drawer_inbox, this.unreadMail));
    /*}*/
    newSettingsOptions.add(new Setting("Forum Index", R.drawable.drawer_index));
    /*if (userid.contentEquals("0")) {
      //To be implemented in future!
      //settingsOptions.add("Login");
    } else {*/
      newSettingsOptions.add(new Setting("Timeline", R.drawable.drawer_timeline));
      newSettingsOptions.add(new Setting("Favorites", R.drawable.drawer_favorites));
      newSettingsOptions.add(new Setting("Unread Topics", R.drawable.drawer_unread));
      newSettingsOptions.add(new Setting("Participated Topics", R.drawable.drawer_participated));
      newSettingsOptions.add(new Setting("Subscribed Topics", R.drawable.drawer_subscribed));
      //settingsOptions.add(new Setting("My Profile", R.drawable.drawer_favorites));
    /*}*/
    newSettingsOptions.add(new Setting("Settings", R.drawable.drawer_settings));

    if (getString(R.string.server_location).contentEquals("0")) {
      newSettingsOptions.add(new Setting("Close Forum", R.drawable.drawer_close_forum));
    }
    return newSettingsOptions;
  }

  //Profile selected interface
  public interface ProfileSelectedListener {
    void onProfileSelected(String username, String userid);
  }

  //IndexRequest Interface
  public interface IndexRequestedListener {
    void onIndexRequested();
  }

  //SettingsRequest Interface
  public interface SettingsRequestedListener {
    void onSettingsRequested();
  }

  //Category Selected Interface
  public interface CategorySelectedListener {
    void onCategorySelected(Category ca);
  }

  private class CheckUnreadMailTask extends AsyncTask<String, Void, Object[]> {
    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final String... params) {
      final Object[] result = new Object[MAX_ITEM_COUNT];

      try {
        final Vector paramz = new Vector();

        result[0] = application.getSession().performSynchronousCall("get_inbox_stat", paramz);
      } catch (Exception e) {
        Log.w(TAG, e.getMessage());
        return null;
      }
      return result;
    }

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    protected void onPostExecute(final Object[] result) {
      if (result == null) {
        final Toast toast = Toast.makeText(getActivity(), "No response from the server!", Toast.LENGTH_LONG);
        toast.show();
        return;
      }

      for (Object o : result) {
        if (o != null) {
          final HashMap map = (HashMap) o;

          if (map.get("inbox_unread_count") != null) {
            unreadMail = (Integer) map.get("inbox_unread_count");
          }
        }
      }

      juiceUpMenu();
    }
  }
}
