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
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class ActiveUsersActivity extends FragmentActivity {

  private AnalyticsHelper ah;

  @SuppressLint("NewApi")
  public final void onCreate(final Bundle savedInstanceState) {

    final String backgroundColor = ((PerisApp) getApplication()).getSession().getServer().serverColor;

    ThemeSetter.setTheme(this, backgroundColor);

    super.onCreate(savedInstanceState);

    ThemeSetter.setActionBar(this, backgroundColor);

    //Track app analytics
    this.ah = ((PerisApp) getApplication()).getAnalyticsHelper();
    this.ah.trackScreen(getClass().getSimpleName(), false);

    setContentView(R.layout.single_frame_activity);

    final FragmentManager fragmentManager = getSupportFragmentManager();
    final FragmentTransaction ft = fragmentManager.beginTransaction();

    final ActiveList subforums = new ActiveList();

    ft.replace(R.id.single_frame_layout_frame, subforums, "Active Users");

    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ft.commit();

    setTitle("Active Users");

    if (getString(R.string.server_location).contentEquals("0")) {
      if (ThemeSetter.getForegroundDark(backgroundColor)) {
        getActionBar().setIcon(R.drawable.ic_ab_main_black);
      } else {
        getActionBar().setIcon(R.drawable.ic_ab_main_white);
      }
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


}
