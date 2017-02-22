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
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

@SuppressLint("NewApi")
public class MailFragment extends ListFragment {

  private static final String TAG = MailFragment.class.getName();
  private static final int MAX_ITEM_COUNT = 50;

  private String rogueTitle;
  private String ourInboxId = "0";
  private String accent;
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
    if (!(this.application.getSession().getServer().serverBackground.contentEquals(
        this.application.getSession().getServer().serverBoxColor)
        && this.application.getSession().getServer().serverBoxBorder.contentEquals("0"))) {
      this.getListView().setDivider(null);
    }
    this.accent = this.application.getSession().getServer().serverColor;
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
    final Intent myIntent = new Intent(getActivity(), Conversation.class);
    final Bundle bundle = new Bundle();
    bundle.putString("id", (String) sender.senderId);
    bundle.putString("boxid", (String) this.ourInboxId);
    bundle.putString("name", (String) sender.sender);
    bundle.putString("moderator", (String) sender.moderatorId);
    bundle.putString("background", (String) this.accent);
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

  private class DownloadMailTask extends AsyncTask<String, Void, Object[]> {
    @SuppressWarnings({"rawtypes", "unchecked", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final String... params) {
      final Object[] result = new Object[MAX_ITEM_COUNT];
      try {
        Vector paramz = new Vector();
        final HashMap map = (HashMap) application.getSession().performSynchronousCall("get_box_info", paramz);
        final Object[] boxes = (Object[]) map.get("list");

        ourInboxId = "0";
        for (Object o : boxes) {
          final HashMap boxMap = (HashMap) o;
          final String boxType = (String) boxMap.get("box_type");
          Log.d(TAG, "Found Mailbox: " + boxType);

          if (boxType.contentEquals("INBOX")) {
            ourInboxId = (String) boxMap.get("box_id");
          }
        }

        paramz = new Vector();
        paramz.addElement(ourInboxId);
        result[0] = application.getSession().performSynchronousCall("get_box", paramz);
      } catch (Exception e) {
        //null response
        return null;
      }
      return result;
    }

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    protected void onPostExecute(final Object[] result) {
      if (result == null) {
        //Toast toast = Toast.makeText(getActivity(), "Server connection timeout :-(", Toast.LENGTH_SHORT);
        //toast.show();
        return;
      }
      try {
        try {
          final ArrayList<InboxItem> inboxList = new ArrayList<InboxItem>();

          for (Object o : result) {
            if (o != null) {
              final HashMap map = (HashMap) o;
              if (map.containsKey("list")) {
                final Object[] topics = (Object[]) map.get("list");
                for (Object t : topics) {
                  final HashMap topicMap = (HashMap) t;
                  final Date timestamp = (Date) topicMap.get("sent_date");
                  final InboxItem ii = new InboxItem();

                  if (topicMap.containsKey("msg_state")) {
                    final int state = (Integer) topicMap.get("msg_state");
                    if (state == 1) {
                      ii.isUnread = true;
                    }
                  }

                  ii.unread = timestamp.toString();
                  ii.sender = new String((byte[]) topicMap.get("msg_subject"));
                  ii.senderId = (String) topicMap.get("msg_id");
                  ii.moderator = new String((byte[]) topicMap.get("msg_from"));
                  ii.moderatorId = (String) topicMap.get("msg_from_id");
                  ii.id = ourInboxId;

                  if (topicMap.containsKey("icon_url")) {
                    ii.senderAvatar = (String) topicMap.get("icon_url");
                  }

                  ii.senderColor = accent;
                  inboxList.add(ii);
                }
              }
            }
          }

          setListAdapter(new InboxAdapter(inboxList, getActivity(), application));
          registerForContextMenu(getListView());
          getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {
              loadConversation((InboxItem) arg0.getItemAtPosition(arg2));
            }
          });
        } catch (Exception ex) {
          Log.d(TAG, ex.getMessage());
        }
      } catch (Exception e) {
        Log.d(TAG, e.getMessage());
      }
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
