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

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

public class About extends Activity {

  private AnalyticsHelper ah;

  protected final void onCreate(final Bundle savedInstanceState) {

    ThemeSetter.setThemeNoTitlebar(this, getString(R.string.default_color));

    super.onCreate(savedInstanceState);

    ThemeSetter.setNavAndStatusBar(this, getString(R.string.default_color));

    this.ah = ((PerisApp) getApplication()).getAnalyticsHelper();
    this.ah.trackScreen(getClass().getSimpleName(), false);

    setContentView(R.layout.about_layout);

    final TextView tvChangelog = (TextView) findViewById(R.id.about_tv_changelog);

    try {
      final Resources res = getResources();
      final InputStream inputStream = res.openRawResource(R.raw.changelog);

      final byte[] b = new byte[inputStream.available()];
      inputStream.read(b);
      tvChangelog.setText(new String(b));
    } catch (Exception e) {
      // e.printStackTrace();
      tvChangelog.setText("Error: can't show help.");
    }

    final TextView tvEdition = (TextView) findViewById(R.id.about_tv_edition);
    //tvEdition.setText("Community Edition (OSP)");

    final ImageView ivPowered = (ImageView) findViewById(R.id.about_powered_by);

    if (getString(R.string.server_location).contentEquals("0")) {
      ivPowered.setVisibility(View.GONE);
    }
  }
}
