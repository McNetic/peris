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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import de.enlightened.peris.support.Net;

public class Login extends Fragment {
  private TextView tvUsername;
  private TextView tvPassword;
  private CheckBox cbAgreement;
  private CheckBox cbAge;
  private Button btnLogin;
  private Button btnNewAccount;
  private PerisApp application;
  private String serverAddress;

  private OnCheckedChangeListener agreementChangedListener = new OnCheckedChangeListener() {

    @SuppressWarnings("checkstyle:requirethis")
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
      if (cbAgreement.isChecked() && cbAge.isChecked()) {
        btnLogin.setEnabled(true);
      } else {
        btnLogin.setEnabled(false);
      }
    }
  };

  private OnClickListener createAccountListener = new OnClickListener() {

    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View v) {
      if (getString(R.string.registration_url).contentEquals("0")) {
        if (application.getSession().getApi().getConfig().getAccountManagementEnabled()) {
          final Intent myIntent = new Intent(getActivity(), NewAccount.class);
          Login.this.startActivity(myIntent);
        } else {
          final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
          builder.setTitle("Account Registration");
          builder.setMessage("Account registeration for this forum must be done on the forum website.  Hit Ok to go to the website now.");
          builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
              final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Net.uriFromURL(application.getSession().getServer().getURL()));
              startActivity(browserIntent);
              dialog.dismiss();
            }
          });
          builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
              // whatever
              dialog.dismiss();
            }
          });
          builder.create().show();
        }
      } else {
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.registration_url)));
        startActivity(browserIntent);
      }
    }
  };

  private OnClickListener loginListener = new OnClickListener() {
    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View v) {
      btnLogin.setEnabled(false);

      final String username = tvUsername.getText().toString().trim();
      final String password = tvPassword.getText().toString().trim();

      application.getSession().setSessionListener(new Session.SessionListener() {

        @Override
        public void onSessionConnected() {

          if (getString(R.string.server_location).contentEquals("0")) {
            application.sendLoginStat(application.getSession().getServer().serverAddress);
          }

          getActivity().finish();
          application.getSession().getServer().serverTab = "0";
          getActivity().startActivity(getActivity().getIntent());
        }

        @Override
        public void onSessionConnectionFailed(final String reason) {
          if (reason != null) {
            final Toast toast = Toast.makeText(getActivity(), reason, Toast.LENGTH_LONG);
            toast.show();
            btnLogin.setEnabled(true);
          }
        }
      });

      application.getSession().loginSession(username, password);
    }
  };

  @Override
  public void onCreate(final Bundle bundle) {
    super.onCreate(bundle);
    this.application = (PerisApp) getActivity().getApplication();
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.login, container, false);
  }

  @Override
  public void onStart() {
    super.onStart();
    this.serverAddress = this.application.getSession().getServer().serverAddress;
    this.linkLayouts();
  }

  @Override
  public void onResume() {
    final String userid = this.application.getSession().getServer().serverUserId;
    if (!userid.contentEquals("0")) {
      getActivity().finish();
      getActivity().startActivity(getActivity().getIntent());
    }
    super.onResume();
  }

  private void linkLayouts() {
    this.tvUsername = (TextView) getActivity().findViewById(R.id.login_username);
    this.tvPassword = (TextView) getActivity().findViewById(R.id.login_password);
    this.cbAgreement = (CheckBox) getActivity().findViewById(R.id.login_agreement);
    this.cbAge = (CheckBox) getActivity().findViewById(R.id.login_age);
    this.btnLogin = (Button) getActivity().findViewById(R.id.login_login);
    this.btnNewAccount = (Button) getActivity().findViewById(R.id.login_new_account);

    final TextView disclaimer = (TextView) getActivity().findViewById(R.id.tv_login_disclaimer);

    this.cbAgreement.setText(this.cbAgreement.getText().toString().replace("SERVERNAME",
        this.serverAddress));
    this.btnLogin.setEnabled(false);
    this.cbAgreement.setOnCheckedChangeListener(this.agreementChangedListener);
    this.cbAge.setOnCheckedChangeListener(this.agreementChangedListener);
    this.btnNewAccount.setOnClickListener(this.createAccountListener);
    this.btnLogin.setOnClickListener(this.loginListener);
  }
}
