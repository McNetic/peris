package de.enlightened.peris;

import android.os.AsyncTask;

import java.net.URL;

import de.enlightened.peris.support.Net;

/**
 * Created by Nicolai Ehemann on 08.02.2017.
 */
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
