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
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.enlightened.peris.api.ApiDataResult;

@SuppressLint("NewApi")
public class NewAccount extends FragmentActivity {

  private static final String TAG = NewAccount.class.getName();
  private static final int MAX_ITEM_COUNT = 50;

  private EditText tvUsername;
  private EditText tvEmail;
  private EditText etPassword1;
  private String serverAddress;
  private PerisApp application;
  private Button btnCreate;
  private AnalyticsHelper ah;

  private OnCheckedChangeListener agreementChangedListener = new OnCheckedChangeListener() {
    @SuppressWarnings("checkstyle:requirethis")
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
      if (isChecked) {
        btnCreate.setEnabled(true);
      } else {
        btnCreate.setEnabled(false);
      }
    }
  };

  private OnClickListener createAccountListener = new OnClickListener() {
    public void onClick(final View v) {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
        new CreateAccountTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      } else {
        new CreateAccountTask().execute();
      }
    }
  };

  public void onCreate(final Bundle savedInstanceState) {
    this.application = (PerisApp) getApplication();
    final String accent = this.application.getSession().getServer().serverColor;
    ThemeSetter.setTheme(this, accent);
    super.onCreate(savedInstanceState);
    ThemeSetter.setActionBar(this, accent);

    //Track app analytics
    this.ah = this.application.getAnalyticsHelper();
    this.ah.trackScreen(getClass().getName(), false);
    this.serverAddress = this.application.getSession().getServer().serverAddress;
    this.setTitle("New Account");
    this.setContentView(R.layout.new_account);
    this.linkLayouts();
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  private void linkLayouts() {
    this.tvUsername = (EditText) findViewById(R.id.newact_username);
    this.tvEmail = (EditText) findViewById(R.id.newact_email);
    this.etPassword1 = (EditText) findViewById(R.id.newact_password_1);

    final TextView disclaimer = (TextView) findViewById(R.id.tv_new_account_disclaimer);

    disclaimer.setText(disclaimer.getText().toString().replace("SERVERNAME",
        this.serverAddress));

    final CheckBox cbAge = (CheckBox) findViewById(R.id.cb_new_account_age);
    cbAge.setOnCheckedChangeListener(this.agreementChangedListener);
    this.btnCreate = (Button) findViewById(R.id.newact_create);
    this.btnCreate.setOnClickListener(this.createAccountListener);
  }

  private class CreateAccountTask extends AsyncTask<Object, Object, ApiDataResult<String>> {
    protected void onPreExecute() {
      NewAccount.this.btnCreate.setEnabled(false);
    }

    @Override
    protected ApiDataResult<String> doInBackground(final Object... params) {
      final String userName = NewAccount.this.tvUsername.getText().toString().trim();
      final String emailAddress = NewAccount.this.tvEmail.getText().toString().trim();
      final String password = NewAccount.this.etPassword1.getText().toString().trim();
      return NewAccount.this.application.getSession().getApi().register(userName, password, emailAddress);
    }

    protected void onPostExecute(final ApiDataResult<String> result) {
      final Toast toast;
      if (result == null) {
        toast = Toast.makeText(NewAccount.this, "There was an error connecting to the server.  Please try again later.", Toast.LENGTH_LONG);
      } else {
        if (result.isSuccess()) {
          toast = Toast.makeText(NewAccount.this, "Welcome to the forums, " + NewAccount.this.tvUsername.getText().toString().trim() + ".  Please log in to get started!", Toast.LENGTH_LONG);

          if (NewAccount.this.getString(R.string.server_location).contentEquals("0")) {
            NewAccount.this.ah.trackEvent("account creation", "created", NewAccount.this.serverAddress, false);
          }
          NewAccount.this.finish();
        } else {
          toast = Toast.makeText(NewAccount.this, result.getMessage(), Toast.LENGTH_LONG);
        }
      }
      toast.show();
      NewAccount.this.btnCreate.setEnabled(true);
    }
  }
}
