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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.enlightened.peris.site.MessageBox;
import de.enlightened.peris.site.MessageFolder;

@SuppressLint("NewApi")
public class MailFragment extends ListFragment {

  private static final String TAG = MailFragment.class.getName();
  private static final int MAX_ITEM_COUNT = 50;

  private String rogueTitle;
  private String ourInboxId = "0";
  private DownloadMailTask mailDownloader;
  private PerisApp application;
  private InboxItem selectedItem;

  @Override
  public void onCreate(final Bundle bundle) {
    super.onCreate(bundle);
    this.application = (PerisApp) this.getActivity().getApplication();
    this.setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();
    this.setEmptyText("No messages available.");
    if (!(this.application.getSession().getServer().serverBackground.contentEquals(
        this.application.getSession().getServer().serverBoxColor)
        && this.application.getSession().getServer().serverBoxBorder.contentEquals("0"))) {
      this.getListView().setDivider(null);
    }
  }

  @Override
  public void onResume() {
    this.loadMail();
    super.onResume();
  }

  private void loadMail() {
    this.mailDownloader = new DownloadMailTask();

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      this.mailDownloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      this.mailDownloader.execute();
    }
  }

  @Override
  public void onStop() {
    super.onStop();

    //Stop any running tasks
    if (this.mailDownloader != null) {
      if (this.mailDownloader.getStatus() == Status.RUNNING) {
        this.mailDownloader.cancel(true);
      }
    }
  }

  private void loadConversation(final InboxItem sender) {
    final Intent myIntent = new Intent(getActivity(), MessageActivity.class);
    final Bundle bundle = new Bundle();
    bundle.putString("id", (String) sender.senderId);
    bundle.putString("boxid", (String) this.ourInboxId);
    bundle.putString("name", (String) sender.sender);
    bundle.putString("moderator", (String) sender.moderatorId);
    bundle.putString("background", (String) this.application.getSession().getServer().serverColor);
    myIntent.putExtras(bundle);

    MailFragment.this.startActivity(myIntent);
  }

  public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
    if (this.rogueTitle != null) {
      return;
    }

    final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    this.selectedItem = (InboxItem) getListView().getItemAtPosition(info.position);

    super.onCreateContextMenu(menu, v, menuInfo);
    final MenuInflater inflater = getActivity().getMenuInflater();
    inflater.inflate(R.menu.delete_mail, menu);
  }

  public boolean onContextItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.mail_delete:
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          new DeleteMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
          new DeleteMessageTask().execute();
        }
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  private class DownloadMailTask extends AsyncTask<Object, Object, List<InboxItem>> {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected List<InboxItem> doInBackground(final Object... params) {
      final MessageBox messageBox = MailFragment.this.application.getSession().getApi().getMessageBox();
      if (messageBox != null) {
        final MessageFolder inboxFolder = messageBox.getInboxFolder();
        MailFragment.this.ourInboxId = inboxFolder.getId();
        return MailFragment.this.application.getSession().getApi()
            .getMessages(inboxFolder);
      } else {
        return new ArrayList<>();
      }
    }

    @SuppressWarnings("rawtypes")
    protected void onPostExecute(final List<InboxItem> inboxItemList) {
      setListAdapter(new InboxAdapter(inboxItemList, getActivity(), MailFragment.this.application));
      registerForContextMenu(getListView());
      getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {
          MailFragment.this.loadConversation((InboxItem) arg0.getItemAtPosition(arg2));
        }
      });
    }
  }

  private class DeleteMessageTask extends AsyncTask<String, Void, Object[]> {

    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final String... params) {
      final Object[] result = new Object[MAX_ITEM_COUNT];
      try {
        final Vector paramz = new Vector();
        paramz.addElement(selectedItem.senderId);
        paramz.addElement(ourInboxId);
        result[0] = application.getSession().performSynchronousCall("delete_message", paramz);
      } catch (Exception e) {
        Log.w(TAG, e.getMessage());
        return null;
      }
      return result;
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final Object[] result) {
      loadMail();
    }
  }
}
