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

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;


public class ForumSettingsFragment extends Fragment {

  private PerisApp application;
  private OnMenuItemClickListener forumHomeSelectedListener = new OnMenuItemClickListener() {

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public boolean onMenuItemClick(final MenuItem menuItem) {
      final String currentServerId = application.getSession().getServer().serverId;
      final String keyName = currentServerId + "_home_page";
      String valueName = getString(R.string.subforum_id);
      String displayName = "Forum Index";

      switch (menuItem.getItemId()) {
        case R.id.menu_home_favs:
          valueName = "forum_favs";
          displayName = "Favorites";
          break;
        case R.id.menu_home_index:
          valueName = getString(R.string.subforum_id);
          displayName = "Forum Index";
          break;
        case R.id.menu_home_participated:
          valueName = "participated";
          displayName = "Participated Topics";
          break;
        case R.id.menu_home_subscribed:
          valueName = "favs";
          displayName = "Subscribed Topics";
          break;
        case R.id.menu_home_unread:
          valueName = "unread";
          displayName = "Unread Topics";
          break;
        case R.id.menu_home_timeline:
          valueName = "timeline";
          displayName = "Timeline";
          break;
        default:
          break;
      }

      final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);
      final SharedPreferences.Editor editor = appPreferences.edit();
      editor.putString(keyName, valueName);
      editor.commit();

      final TextView forumSettingHomeCurrent = (TextView) getActivity().findViewById(R.id.forum_setting_home_current);
      forumSettingHomeCurrent.setText(displayName);
      return true;
    }

  };

  @Override
  public void onCreate(final Bundle bundle) {
    super.onCreate(bundle);
    this.application = (PerisApp) getActivity().getApplication();
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.forum_settings_layout, container, false);
  }

  @Override
  public void onStart() {
    super.onStart();
    this.setupHandlers();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();

    if (this.application.getForceRefresh()) {
      this.application.setForceRefresh(false);
      this.getActivity().finish();
      this.getActivity().startActivity(this.getActivity().getIntent());
    }

  }

  @Override
  public void onStop() {
    super.onStop();

  }

  private void setupHandlers() {
    final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);
    this.setupSignatureButton(appPreferences);
    this.setupThemeButton();
    this.setupHomeButton(appPreferences);
    this.setupIconAvatarButton(appPreferences);
    this.setupSidebarSettings(appPreferences);
    this.setupQuickreplySettings(appPreferences);
    this.setupDisplayNameButton();
    this.setupTextSettingsButton();
    this.setupClearCacheButton();
    this.setupAboutButton();
  }

  private void setupAboutButton() {
    //About button
    final LinearLayout forumSettingAbout = (LinearLayout) getActivity().findViewById(R.id.forum_setting_about);
    forumSettingAbout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        final Intent aboutIntent = new Intent(getActivity(), About.class);
        startActivity(aboutIntent);
      }
    });
  }

  private void setupClearCacheButton() {
    //Clear cache button
    final LinearLayout forumSettingCache = (LinearLayout) getActivity().findViewById(R.id.forum_setting_cache);
    forumSettingCache.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        CacheNuker.nukeCache(getActivity());
        final Intent intro = new Intent(getActivity(), IntroScreen.class);

        getActivity().finish();
        if (!getString(R.string.server_location).contentEquals("0")) {
          getActivity().startActivity(intro);
        }
      }
    });
  }

  private void setupTextSettingsButton() {
    //Text settings button
    final LinearLayout forumSettingText = (LinearLayout) getActivity().findViewById(R.id.forum_setting_text);
    forumSettingText.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        final TextDialogFragment newFragment = TextDialogFragment.newInstance();
        newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
      }
    });
  }

  private void setupDisplayNameButton() {
    //Display name button
    final LinearLayout forumSettingDisplayName = (LinearLayout) getActivity().findViewById(R.id.forum_setting_display_name);
    forumSettingDisplayName.setVisibility(View.GONE);
  }

  private void setupQuickreplySettings(final SharedPreferences appPreferences) {
    //Quick Reply Setting
    final LinearLayout forumSettingQuickReply = (LinearLayout) getActivity().findViewById(R.id.forum_setting_quick_reply);
    forumSettingQuickReply.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);
        boolean quickReplySetting = appPreferences.getBoolean("show_quick_reply", true);
        final TextView forumSettingQuickReplySetting = (TextView) getActivity().findViewById(R.id.forum_setting_quick_reply_setting);

        if (quickReplySetting) {
          forumSettingQuickReplySetting.setText("Off");
          quickReplySetting = false;
        } else {
          forumSettingQuickReplySetting.setText("On");
          quickReplySetting = true;
        }

        final SharedPreferences.Editor editor = appPreferences.edit();
        editor.putBoolean("show_quick_reply", quickReplySetting);
        editor.commit();
      }
    });
    final boolean quickReplySetting = appPreferences.getBoolean("show_quick_reply", true);
    final TextView forumSettingQuickReplySetting = (TextView) getActivity().findViewById(R.id.forum_setting_quick_reply_setting);
    if (quickReplySetting) {
      forumSettingQuickReplySetting.setText("On");
    } else {
      forumSettingQuickReplySetting.setText("Off");
    }
  }

  private void setupSidebarSettings(final SharedPreferences appPreferences) {
    //Sidebar Setting
    final LinearLayout forumSettingSidebar = (LinearLayout) getActivity().findViewById(R.id.forum_setting_sidebar);
    forumSettingSidebar.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);
        boolean currentSidebarSetting = appPreferences.getBoolean("show_sidebar", true);
        final TextView forumSettingSidebarSetting = (TextView) getActivity().findViewById(R.id.forum_setting_sidebar_setting);

        if (currentSidebarSetting) {
          forumSettingSidebarSetting.setText("Off");
          currentSidebarSetting = false;
        } else {
          forumSettingSidebarSetting.setText("On");
          currentSidebarSetting = true;
        }

        final SharedPreferences.Editor editor = appPreferences.edit();
        editor.putBoolean("show_sidebar", currentSidebarSetting);
        editor.commit();
      }
    });
    final boolean sidebarSetting = appPreferences.getBoolean("show_sidebar", true);
    final TextView forumSettingSidebarSetting = (TextView) getActivity().findViewById(R.id.forum_setting_sidebar_setting);
    if (sidebarSetting) {
      forumSettingSidebarSetting.setText("On");
    } else {
      forumSettingSidebarSetting.setText("Off");
    }
  }

  private void setupIconAvatarButton(final SharedPreferences appPreferences) {
    //Avatars and Icons button
    final LinearLayout forumSettingShowImages = (LinearLayout) this.getActivity().findViewById(R.id.forum_setting_show_images);
    forumSettingShowImages.setOnClickListener(new View.OnClickListener() {
      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);
        boolean currentAvatarSetting = appPreferences.getBoolean("show_images", true);

        final TextView forumSettingShowImagesReadout = (TextView) getActivity().findViewById(R.id.forum_setting_show_images_readout);

        if (currentAvatarSetting) {
          forumSettingShowImagesReadout.setText("Off");
          currentAvatarSetting = false;
        } else {
          forumSettingShowImagesReadout.setText("On");
          currentAvatarSetting = true;
        }

        final SharedPreferences.Editor editor = appPreferences.edit();
        editor.putBoolean("show_images", currentAvatarSetting);
        editor.commit();
      }
    });

    final boolean currentAvatarSetting = appPreferences.getBoolean("show_images", true);
    final TextView forumSettingShowImagesReadout = (TextView) getActivity().findViewById(R.id.forum_setting_show_images_readout);

    if (currentAvatarSetting) {
      forumSettingShowImagesReadout.setText("On");
    } else {
      forumSettingShowImagesReadout.setText("Off");
    }
  }

  private void setupHomeButton(final SharedPreferences appPreferences) {
    //Home Page button
    final LinearLayout forumSettingHome = (LinearLayout) getActivity().findViewById(R.id.forum_setting_home);
    forumSettingHome.setOnClickListener(new View.OnClickListener() {
      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        final PopupMenu popup = new PopupMenu(getActivity(), v);
        final MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.home_page_selection, popup.getMenu());
        popup.setOnMenuItemClickListener(forumHomeSelectedListener);
        popup.show();
      }
    });

    //Home Page Display Text
    final String currentServerId = this.application.getSession().getServer().serverId;
    final String keyName = currentServerId + "_home_page";
    String valueName = getString(R.string.subforum_id);
    String displayName = "Forum Index";

    valueName = appPreferences.getString(keyName, getString(R.string.subforum_id));

    if (valueName.contentEquals(getString(R.string.subforum_id))) {
      displayName = "Forum Index";
    }
    if (valueName.contentEquals("forum_favs")) {
      displayName = "Favorites";
    }
    if (valueName.contentEquals("timeline")) {
      displayName = "Timeline";
    }
    if (valueName.contentEquals("participated")) {
      displayName = "Participated Topics";
    }
    if (valueName.contentEquals("favs")) {
      displayName = "Subscribed Topics";
    }
    if (valueName.contentEquals("unread")) {
      displayName = "Unread Topics";
    }

    final TextView forumSettingHomeCurrent = (TextView) this.getActivity().findViewById(R.id.forum_setting_home_current);
    forumSettingHomeCurrent.setText(displayName);
  }

  private void setupThemeButton() {
    //Theme button
    final LinearLayout forumSettingTheme = (LinearLayout) this.getActivity().findViewById(R.id.forum_setting_theme);
    forumSettingTheme.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        final Intent themeIntent = new Intent(getActivity(), ThemeEditor.class);
        startActivity(themeIntent);
      }
    });
  }

  private void setupSignatureButton(final SharedPreferences appPreferences) {
    //Signature button
    final LinearLayout forumSettingTagline = (LinearLayout) getActivity().findViewById(R.id.forum_setting_tagline);
    if (this.application.getSession().getServer().serverUserName.contentEquals("0")) {
      forumSettingTagline.setVisibility(View.GONE);
    } else {
      final LinearLayout forumSettingTaglineBodyBuilder = (LinearLayout) getActivity().findViewById(R.id.forum_setting_tagline_body_builder);
      final boolean useShading = appPreferences.getBoolean("use_shading", false);
      final boolean useOpenSans = appPreferences.getBoolean("use_opensans", false);
      final int fontSize = appPreferences.getInt("font_size", 16);
      final Typeface opensans = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensans.ttf");
      BBCodeParser.parseCode(getActivity(), forumSettingTaglineBodyBuilder,
          this.application.getSession().getServer().serverTagline, opensans,
          useOpenSans, useShading, null, fontSize, false, "#333333", this.application);

      forumSettingTagline.setOnClickListener(new View.OnClickListener() {
        @Override
        @SuppressWarnings("checkstyle:requirethis")
        public void onClick(final View v) {
          final Intent myIntent = new Intent(getActivity(), NewPost.class);
          final Bundle bundle = new Bundle();
          bundle.putString("postid", "0");
          bundle.putString("parent", "0");
          bundle.putString("category", "0");
          bundle.putString("subforum_id", (String) "0");
          bundle.putString("original_text", (String) "");
          bundle.putString("boxTitle", (String) "Signature Editor");
          bundle.putString("picture", (String) "0");
          bundle.putString("color", (String) application.getSession().getServer().serverColor);
          bundle.putString("subject", (String) "");
          bundle.putString("post_type", NewPost.Type.Tagline.name());
          myIntent.putExtras(bundle);

          startActivity(myIntent);
        }
      });
    }
  }
}
