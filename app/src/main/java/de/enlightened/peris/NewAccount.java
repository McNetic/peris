package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Vector;

@SuppressLint("NewApi")
public class NewAccount extends FragmentActivity {
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

  private class CreateAccountTask extends AsyncTask<String, Void, Object[]> {
    @SuppressWarnings("checkstyle:requirethis")
    protected void onPreExecute() {
      btnCreate.setEnabled(false);
    }

    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final String... params) {
      final String username = tvUsername.getText().toString().trim();
      final String emailaddress = tvEmail.getText().toString().trim();
      final String password = etPassword1.getText().toString().trim();
      final Object[] result = new Object[MAX_ITEM_COUNT];

      try {
        final Vector paramz = new Vector();
        paramz.addElement(username.getBytes());
        paramz.addElement(password.getBytes());
        paramz.addElement(emailaddress.getBytes());
        result[0] = application.getSession().performNewSynchronousCall("register", paramz);
      } catch (Exception ex) {
        Log.d("Peris", ex.getMessage());
      }
      return result;
    }

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    protected void onPostExecute(final Object[] result) {
      if (result == null) {
        final Toast toast = Toast.makeText(NewAccount.this, "There was an error connecting to the server.  Please try again later.", Toast.LENGTH_LONG);
        toast.show();
        btnCreate.setEnabled(true);
        return;
      }

      if (result[0] != null) {
        final HashMap map = (HashMap) result[0];
        if (map.containsKey("result")) {
          final Boolean loginSuccess = (Boolean) map.get("result");

          if (loginSuccess) {
            final Toast toast = Toast.makeText(NewAccount.this, "Welcome to the forums, " + tvUsername.getText().toString().trim() + ".  Please log in to get started!", Toast.LENGTH_LONG);
            toast.show();

            if (getString(R.string.server_location).contentEquals("0")) {
              ah.trackEvent("account creation", "created", serverAddress, false);
            }
            finish();
          } else {
            final String regError = new String((byte[]) map.get("result_text"));
            final Toast toast = Toast.makeText(NewAccount.this, regError, Toast.LENGTH_LONG);
            toast.show();
            btnCreate.setEnabled(true);
          }
        } else {
          final Toast toast = Toast.makeText(NewAccount.this, "Server communication error!  Please try again later.", Toast.LENGTH_LONG);
          toast.show();
          btnCreate.setEnabled(true);
        }
      } else {
        final Toast toast = Toast.makeText(NewAccount.this, "Connection to the server could not be established :-(  Please try again later.", Toast.LENGTH_LONG);
        toast.show();
        btnCreate.setEnabled(true);
      }
    }
  }
}
