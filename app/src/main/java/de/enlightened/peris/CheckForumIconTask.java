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

import android.os.AsyncTask;

import java.net.URL;

import de.enlightened.peris.support.Net;

class CheckForumIconTask extends AsyncTask<String, Void, String> {

  private final Session session;

  public CheckForumIconTask(final Session session) {
    this.session = session;
  }

  @SuppressWarnings("checkstyle:requirethis")
  protected String doInBackground(final String... params) {
    if (this.session.getServer().serverIcon == null) {
      final URL forumIconUrl = this.session.getServer().getURL("/favicon.ico");
      if (Net.checkURL(forumIconUrl)) {
        return forumIconUrl.toExternalForm();
      }
    }
    return null;
  }

  @SuppressWarnings("checkstyle:requirethis")
  protected void onPostExecute(final String result) {
    if (result == null) {
      return;
    }
    this.session.getServer().serverIcon = result;
    this.session.updateServer();
  }
}
