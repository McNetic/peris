/**
 * Copyright (C) 2017 Nicolai Ehemann
 * <p>
 * This file is part of Peris.
 * <p>
 * Peris is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Peris is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Peris.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.enlightened.peris;

import android.os.AsyncTask;

import de.enlightened.peris.api.ApiResult;
import de.enlightened.peris.api.Tapatalk;

class DeleteMessageTask extends AsyncTask<String, Void, ApiResult> {

  private final Tapatalk api;
  private final String messageId;
  private final String folderId;
  private final TaskListener<ApiResult> listener;

  public DeleteMessageTask(final Tapatalk api, final String messageId, final String folderId, final TaskListener<ApiResult> listener) {
    this.api = api;
    this.messageId = messageId;
    this.folderId = folderId;
    this.listener = listener;
  }

  public DeleteMessageTask(final Tapatalk api, final String messageId, final String folderId) {
    this(api, messageId, folderId, null);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  protected ApiResult doInBackground(final String... params) {
    return this.api.deleteMessage(this.folderId, this.messageId);
  }

  protected void onPostExecute(final ApiResult result) {
    if (this.listener != null) {
      this.listener.onPostExecute(result);
    }
  }
}
