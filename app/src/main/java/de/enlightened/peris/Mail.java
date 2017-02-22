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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;


public class Mail extends FragmentActivity {

  @SuppressLint("NewApi")
  public void onCreate(final Bundle savedInstanceState) {
    final PerisApp application = (PerisApp) getApplication();
    final String background = application.getSession().getServer().serverColor;

    ThemeSetter.setTheme(this, background);
    super.onCreate(savedInstanceState);
    ThemeSetter.setActionBar(this, background);

    setTitle("Inbox");
    setContentView(R.layout.single_frame_activity);

    //Setup forum background
    final String forumWallpaper = application.getSession().getServer().serverWallpaper;
    final String forumBackground = application.getSession().getServer().serverBackground;
    final FrameLayout sfaHolder = (FrameLayout) findViewById(R.id.sfa_holder);
    final ImageView sfaWallpaper = (ImageView) findViewById(R.id.sfa_wallpaper);
    if (forumBackground != null && forumBackground.contains("#") && forumBackground.length() == 7) {
      sfaHolder.setBackgroundColor(Color.parseColor(forumBackground));
    } else {
      sfaHolder.setBackgroundColor(Color.parseColor(getString(R.string.default_background)));
    }

    if (forumWallpaper != null && forumWallpaper.contains("http")) {
      final String imageUrl = forumWallpaper;
      ImageLoader.getInstance().displayImage(imageUrl, sfaWallpaper);
    } else {
      sfaWallpaper.setVisibility(View.GONE);
    }

    final MailFragment mf = new MailFragment();
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final FragmentTransaction ftZ = fragmentManager.beginTransaction();
    ftZ.replace(R.id.single_frame_layout_frame, mf);
    ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ftZ.commit();
  }
}
