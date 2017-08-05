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
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Vector;

public class BanDialogFragment extends DialogFragment {

  private static final String TAG = BanDialogFragment.class.getName();;

  private TextView tvIntro;
  private EditText etReason;
  private Button submitButton;
  private ProgressBar banWorking;
  private PerisApp application;
  private String banId;
  private String banReason;
  private OnClickListener submitBanListener = new OnClickListener() {

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View v) {
      banReason = etReason.getText().toString().trim();
      submitButton.setEnabled(false);
      banWorking.setVisibility(View.VISIBLE);
      new SubmitBanTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, banId, banReason);
    }

  };

  static BanDialogFragment newInstance() {
    final BanDialogFragment f = new BanDialogFragment();
    final Bundle args = new Bundle();
    args.putString("username", "cylon");
    f.setArguments(args);

    return f;
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setStyle(STYLE_NO_TITLE, getTheme());
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View v = inflater.inflate(R.layout.ban_submission, container, false);
    this.tvIntro = (TextView) v.findViewById(R.id.tvBanIntro);
    this.etReason = (EditText) v.findViewById(R.id.etBanReason);
    this.submitButton = (Button) v.findViewById(R.id.btnBanSubmit);
    this.banWorking = (ProgressBar) v.findViewById(R.id.ban_dialog_working);
    this.banWorking.setVisibility(View.GONE);

    final Bundle args = getArguments();
    this.tvIntro.setText("Ban " + args.getString("username") + " for the following reason:");
    this.banId = args.getString("username");
    this.submitButton.setOnClickListener(this.submitBanListener);
    this.application = (PerisApp) getActivity().getApplication();

    return v;
  }

  private class SubmitBanTask extends AsyncTask<String, Void, String> {
    // parm[0] - (string)topic_id

    @SuppressLint("UseValueOf")
    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected String doInBackground(final String... params) {
      if (getActivity() == null) {
        return null;
      }

      try {
        Vector paramz;
        paramz = new Vector();
        paramz.addElement(params[0].getBytes());
        paramz.addElement(1);
        paramz.addElement(params[1].getBytes());
        application.getSession().performSynchronousCall("m_ban_user", paramz);
      } catch (Exception ex) {
        Log.w(TAG, ex.getMessage());
      }
      return "";
    }

    protected void onPostExecute(final String result) {
      if (getActivity() == null) {
        return;
      }
      BanDialogFragment.this.dismiss();
    }
  }
}
