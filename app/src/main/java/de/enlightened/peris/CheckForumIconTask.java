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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.enlightened.peris.support.Net;

class CheckForumIconTask extends AsyncTask<String, Void, String> {

  private static final Pattern PATTERN_ICON_LINK = Pattern.compile("<link (rel=\"[^\"]*icon\" [^>]+href=\"[^\"]+\"|href=\"[^\"]+\" [^>]+rel=\"[^\"]*icon\") [^>]+>");
  private static final Pattern PATTERN_HREF = Pattern.compile("href=\"([^\"]+)\"");
  private static final Pattern PATTERN_SIZE = Pattern.compile("sizes=\"([0-9]+)x[0-9]+\"");

  private final Session session;
  private final int optimalIconSize;

  public CheckForumIconTask(final Session session, final int optimalIconSize) {
    this.session = session;
    this.optimalIconSize = optimalIconSize;
  }

  protected String doInBackground(final String... params) {
    String iconUrlString = null;
    if (this.session.getServer().serverIcon == null) {
      final String indexPage = Net.getHtml(this.session.getServer().getURL());
      if (indexPage != null) {
        final Matcher matcher = PATTERN_ICON_LINK.matcher(indexPage);
        int currentSize = 0;
        String currentHref = "/favicon.ico";
        while (matcher.find()) {
          final String iconLink = matcher.group();
          final Matcher sizeMatcher = PATTERN_SIZE.matcher(iconLink);
          final int size;
          if (sizeMatcher.find()) {
            size = Integer.parseInt(sizeMatcher.group(1));
          } else {
            size = 1;
          }
          if ((currentSize < this.optimalIconSize && size > currentSize)
              || (currentSize > this.optimalIconSize && size > this.optimalIconSize && size < currentSize)) {
            final Matcher hrefMatcher = PATTERN_HREF.matcher(iconLink);
            hrefMatcher.find();
            currentHref = hrefMatcher.group(1);
            currentSize = size;
          }
        }
        final URL forumIconUrl;
        try {
          if (currentHref.startsWith("//")) {
            final int slashPos = currentHref.indexOf('/', 2);
            forumIconUrl = new URL(this.session.getServer().getScheme(), currentHref.substring(2, slashPos), currentHref.substring(slashPos + 1));
          } else {
            forumIconUrl = this.session.getServer().getURL(currentHref);
          }
          if (Net.checkURL(forumIconUrl)) {
            iconUrlString = forumIconUrl.toExternalForm();
          }
        } catch (MalformedURLException e) {
          iconUrlString = null;
        }
      }
    }
    return iconUrlString;
  }

  protected void onPostExecute(final String result) {
    if (result == null) {
      return;
    }
    this.session.getServer().serverIcon = result;
    this.session.updateServer();
  }
}
