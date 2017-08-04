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

import java.util.List;

import de.enlightened.peris.site.OnlineUser;

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
    this.loadUserList();
  }

  private void loadUserList() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      new DownloadUserlistTask(this.profileSelected).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      new DownloadUserlistTask(this.profileSelected).execute();
    }
  }

  public final void setOnProfileSelectedListener(final OnProfileSelectedListener listener) {
    this.profileSelected = listener;
  }

  //Profile selected interface
  public interface OnProfileSelectedListener {
    void onProfileSelected(String username, String userid);
  }

  private class DownloadUserlistTask extends AsyncTask<String, Void, List<OnlineUser>> {
    private static final int NUM_MAIL_TASKS = 50;
    private final OnProfileSelectedListener profileSelected;

    DownloadUserlistTask(final OnProfileSelectedListener profileSelected) {
      this.profileSelected = profileSelected;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List<OnlineUser> doInBackground(final String... params) {
      return ActiveList.this.application.getSession().getApi().getOnlineUsers();
    }

    @SuppressWarnings("rawtypes")
    protected void onPostExecute(final List<OnlineUser> listOnlineUsers) {
      if (listOnlineUsers == null) {
        Log.d(TAG, "Null active list");
        return;
      } else {
        setListAdapter(new UserCardAdapter(ActiveList.this.application.getSession(), listOnlineUsers, getActivity()));

        getListView().setOnItemClickListener(new OnItemClickListener() {

          private OnProfileSelectedListener profileSelected;

          private OnItemClickListener initialize(final OnProfileSelectedListener profileSelectedListener) {
            this.profileSelected = profileSelectedListener;
            return this;
          }

          public void onItemClick(final AdapterView<?> adapterView, final View view, final int itemPosition, final long arg3) {
            final OnlineUser onlineUser = (OnlineUser) adapterView.getItemAtPosition(itemPosition);

            if (this.profileSelected != null) {
              this.profileSelected.onProfileSelected(onlineUser.getUserName(), onlineUser.getId());
            }
          }
        }.initialize(this.profileSelected));
      }
    }
  }
}
