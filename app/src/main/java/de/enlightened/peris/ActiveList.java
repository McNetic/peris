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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

@SuppressLint("NewApi")
public class ActiveList extends ListFragment {

  private static final String TAG = ActiveList.class.getName();

  private PerisApp application;
  private OnProfileSelectedListener profileSelected;

  @Override
  public final void onCreate(final Bundle bundle) {
    super.onCreate(bundle);

    this.application = (PerisApp) getActivity().getApplication();

    setHasOptionsMenu(true);
  }

  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public final void onStart() {

    super.onStart();

    getListView().setDivider(null);

    this.loadMail();
  }

  private void loadMail() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      new DownloadMailTask(this.profileSelected).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      new DownloadMailTask(this.profileSelected).execute();
    }
  }

  public final void setOnProfileSelectedListener(final OnProfileSelectedListener l) {
    this.profileSelected = l;
  }

  //Profile selected interface
  public interface OnProfileSelectedListener {
    void onProfileSelected(String username, String userid);
  }

  private class DownloadMailTask extends AsyncTask<String, Void, Object[]> {
    private static final int NUM_MAIL_TASKS = 50;
    private final OnProfileSelectedListener profileSelected;

    DownloadMailTask(final OnProfileSelectedListener profileSelected) {
      this.profileSelected = profileSelected;
    }

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final String... params) {

      final Object[] result = new Object[NUM_MAIL_TASKS];

      try {

        final Vector paramz = new Vector();

        result[0] = (HashMap) application.getSession().performSynchronousCall("get_online_users", paramz);
      } catch (Exception e) {
        Log.w(TAG, e.getMessage());
        return null;
      }
      return result;
    }

    @SuppressWarnings("rawtypes")
    protected void onPostExecute(final Object[] result) {
      if (result == null) {
        Log.d(TAG, "Null active list");
        return;
      }


      try {
        try {
          final ArrayList<IgnoreItem> inboxList = new ArrayList<IgnoreItem>();


          for (Object o : result) {

            if (o != null) {
              final HashMap map = (HashMap) o;

              if (map.containsKey("list")) {
                final Object[] topics = (Object[]) map.get("list");
                for (Object t : topics) {

                  final HashMap topicMap = (HashMap) t;

                  final IgnoreItem ii = new IgnoreItem();

                  if (topicMap.containsKey("username")) {
                    ii.ignoreItemUsername = new String((byte[]) topicMap.get("username"));
                  } else {
                    if (topicMap.containsKey("user_name")) {
                      ii.ignoreItemUsername = new String((byte[]) topicMap.get("user_name"));
                    }
                  }

                  if (topicMap.containsKey("icon_url")) {
                    ii.ignoreItemAvatar = (String) topicMap.get("icon_url");
                  }

                  if (topicMap.containsKey("display_text")) {
                    ii.ignoreItemDate = new String((byte[]) topicMap.get("display_text"));
                  }

                  if (topicMap.containsKey("user_id")) {
                    ii.ignoreUserId = (String) topicMap.get("user_id");
                  }


                  ii.ignoreProfileColor = "#000000";

                  inboxList.add(ii);

                }
              }
            }
          }


          setListAdapter(new UserCardAdapter(inboxList, getActivity()));
          //registerForContextMenu(getListView());

          getListView().setOnItemClickListener(new OnItemClickListener() {

            private OnProfileSelectedListener profileSelected;

            private OnItemClickListener initialize(final OnProfileSelectedListener profileSelectedListener) {
              this.profileSelected = profileSelectedListener;
              return this;
            };

            public void onItemClick(final AdapterView<?> adapterView, final View view, final int itemPosition, final long arg3) {
              final IgnoreItem sender = (IgnoreItem) adapterView.getItemAtPosition(itemPosition);

              if (this.profileSelected != null) {
                this.profileSelected.onProfileSelected(sender.ignoreItemUsername, sender.ignoreUserId);
              }
            }
          }.initialize(this.profileSelected));

        } catch (Exception ex) {
          Log.d(TAG, "ex1 - " + ex.getMessage());
        }
      } catch (Exception e) {
        Log.d(TAG, "ex2 - " + e.getMessage());
      }
    }
  }
}
