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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;
import java.util.Vector;

import de.enlightened.peris.db.PerisDBHelper;
import de.enlightened.peris.db.ServerRepository;

@SuppressLint("NewApi")
public class NewPost extends FragmentActivity {

  private static final String TAG = NewPost.class.getName();
  private static final int MAX_ITEM_COUNT = 50;
  private static final int MAX_SUBJECT_LENGTH = 45;

  private Type postType = Type.NewThread;
  //private String server_address;
  private String parent = "0";
  private String category = "0";
  private String originalText = "";
  private String subforum = "0";
  private String picture = "0";
  private EditText subjectInputter;
  private EditText bodyInputter;
  private int colorSelectionStart = 0;
  private int colorSelectionEnd = 0;
  private String postId = "0";
  private String theSubject = "0";
  private Button submitter;
  private Button pictureAttacher;
  private String tagline;
  private PerisApp application;
  private boolean colorPickerOpen = false;
  private boolean postSubmitted = false;
  private Session mailSession;
  private AnalyticsHelper ah;
  private PerisDBHelper dbHelper;

  public static enum Type {
    NewThread,
    Reply,
    EditPost,
    Message,
    Five,
    Tagline,
    Instapost
  }

  private View.OnClickListener clickListener = new View.OnClickListener() {
    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View v) {

      colorSelectionStart = bodyInputter.getSelectionStart();
      colorSelectionEnd = bodyInputter.getSelectionEnd();

      colorPickerOpen = true;

      final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(bodyInputter.getWindowToken(), 0);

      final ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
      newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.ColorSelectedListener() {

        public void onColorSelected(final String color) {
          setColor(color);
        }
      });
      newFragment.show(getSupportFragmentManager(), "dialog");
    }
  };

  public static enum BBStyle {
    BOLD("b"),
    ITALITIC("i"),
    UNDERLINE("u");

    private final String bbTag;

    private BBStyle(final String bbTag) {
      this.bbTag = bbTag;
    }

    public String openTag() {
      return String.format("[%s]", this.bbTag);
    }

    public String closeTag() {
      return String.format("[/%s]", this.bbTag);
    }
  }

  @SuppressWarnings("checkstyle:requirethis")
  private void setStyleOnInputterSelection(final BBStyle bbStyle) {
    final int startSelection = bodyInputter.getSelectionStart();
    final int endSelection = bodyInputter.getSelectionEnd();
    final String selectedText = bodyInputter.getText().toString().substring(startSelection, endSelection).trim();
    final String firstPart = bodyInputter.getText().toString().substring(0, startSelection);
    final String secondPart = bodyInputter.getText().toString().substring(endSelection, bodyInputter.getText().toString().length());
    bodyInputter.setText(firstPart + bbStyle.openTag() + selectedText + bbStyle.closeTag() + secondPart);
    bodyInputter.setSelection(endSelection + 3);
  }

  private View.OnClickListener setBold = new View.OnClickListener() {
    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View v) {
      setStyleOnInputterSelection(BBStyle.BOLD);
    }
  };
  private View.OnClickListener setItalic = new View.OnClickListener() {
    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View v) {
      setStyleOnInputterSelection(BBStyle.ITALITIC);
    }
  };
  private View.OnClickListener setUnderline = new View.OnClickListener() {
    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View v) {
      setStyleOnInputterSelection(BBStyle.UNDERLINE);
    }
  };
  private View.OnClickListener launchSubmit = new View.OnClickListener() {
    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View v) {
      submitter.setEnabled(false);

      if (postType == Type.Tagline) {
        postSubmitted = true;
        final String comment = bodyInputter.getText().toString();
        mailSession.getServer().serverTagline = comment;
        mailSession.updateServer();
        finish();
        return;
      }

      final Toast toast = Toast.makeText(NewPost.this, "Submitting, please wait!", Toast.LENGTH_SHORT);
      toast.show();

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
        new PostDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      } else {
        new PostDataTask().execute();
      }
    }
  };
  private View.OnClickListener submissionOptionsListener = new View.OnClickListener() {
    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View v) {

      final EditText input = new EditText(NewPost.this);

      new AlertDialog.Builder(NewPost.this)
          .setTitle("Insert Image")
          .setMessage("Enter the URL of the image you would like to post.")
          .setView(input)
          .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int whichButton) {
              runOnUiThread(new Runnable() {
                public void run() {
                  bodyInputter.setText(bodyInputter.getText() + "[img]" + input.getText().toString().trim() + "[/img]");
                }
              });

              dialog.dismiss();
            }
          }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(final DialogInterface dialog, final int whichButton) {
          // Do nothing.
        }
      }).show();
    }
  };

  public final void onCreate(final Bundle savedInstanceState) {
    this.application = (PerisApp) getApplication();
    this.dbHelper = new PerisDBHelper(this);

    final Bundle bundle = getIntent().getExtras();
    this.subforum = bundle.getString("subforum_id");
    this.postType = Type.valueOf(bundle.getString("post_type"));
    this.parent = bundle.getString("parent");
    this.category = bundle.getString("category");
    this.originalText = bundle.getString("original_text");
    this.picture = bundle.getString("picture");
    this.postId = bundle.getString("postid");
    final String boxTitle = bundle.getString("boxTitle");
    this.theSubject = bundle.getString("subject");

    if (this.postType == Type.Message && this.theSubject.length() > 0) {
      this.theSubject = "Re: " + this.theSubject;
    }

    if (bundle.containsKey("server")) {
      Log.i(TAG, "Mail bundle contains server!");
      final Server server = ServerRepository.findOneByAddress(
          this.dbHelper.getReadableDatabase(), bundle.getString("server"));
      if (server == null) {
        Log.i(TAG, "Conversation Server is null!");
        return;
      }

      this.mailSession = new Session(this, (PerisApp) getApplication(), this.dbHelper);
      this.mailSession.setSessionListener(new Session.SessionListener() {

        @Override
        public void onSessionConnected() {
          return;
        }

        @Override
        public void onSessionConnectionFailed(final String reason) {
          return;
        }

      });
      this.mailSession.setServer(server);
    } else {
      this.mailSession = this.application.getSession();
    }

    this.tagline = this.mailSession.getServer().serverTagline;
    final String accent = this.mailSession.getServer().serverColor;
    ThemeSetter.setTheme(this, accent);
    super.onCreate(savedInstanceState);
    ThemeSetter.setActionBar(this, accent);

    //Track app analytics
    this.ah = ((PerisApp) getApplication()).getAnalyticsHelper();
    this.ah.trackScreen(getClass().getName(), false);
    this.setResult(0);
    setContentView(R.layout.new_post);
    setTitle(boxTitle);

    this.subjectInputter = (EditText) findViewById(R.id.new_post_subject);
    this.bodyInputter = (EditText) findViewById(R.id.new_post_body);
    this.submitter = (Button) findViewById(R.id.new_post_submit);
    this.pictureAttacher = (Button) findViewById(R.id.new_post_picture);
    this.bodyInputter.setOnFocusChangeListener(new View.OnFocusChangeListener() {

      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onFocusChange(final View v, final boolean hasFocus) {
        if (hasFocus) {
          postSubmitted = false;
        }
      }
    });

    if (this.postType == Type.Five) {
      this.pictureAttacher.setVisibility(View.GONE);
    }
    final Button bold = (Button) findViewById(R.id.new_post_bold);
    final Button itialic = (Button) findViewById(R.id.new_post_italic);
    final Button underline = (Button) findViewById(R.id.new_post_underline);
    final Button picker = (Button) findViewById(R.id.new_post_color);

    picker.setTextColor(Color.parseColor(accent));

    this.submitter.setOnClickListener(this.launchSubmit);
    this.pictureAttacher.setOnClickListener(this.submissionOptionsListener);
    bold.setOnClickListener(this.setBold);
    itialic.setOnClickListener(this.setItalic);
    underline.setOnClickListener(this.setUnderline);
    picker.setOnClickListener(this.clickListener);

    if (this.postType != Type.NewThread && this.postType != Type.Message) {
      this.subjectInputter.setVisibility(View.GONE);
      this.subjectInputter.setText(this.theSubject);
    } else if (this.postType == Type.Message) {
      this.subjectInputter.setText(this.theSubject);
      this.bodyInputter.setSelection(0);
    }

    this.originalText = this.originalText.replace("</blockquote>", "[/quote]").replace("<blockquote>", "[quote]").replace("<u>", "[u]").replace("</u>", "[/u]").replace("<i>", "[i]").replace("</i>", "[/i]").replace("<b>", "[b]").replace("</b>", "[/b]").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&amp;", "&").replace("<br />", "\n");
    this.originalText = this.originalText.replaceAll("\\<font color=\"([^<]*)\"\\>([^<]*)\\</font\\>", "[color=$1]$2[/color]");
    this.bodyInputter.setText(this.originalText);

    if (this.postType == Type.Tagline) {
      this.bodyInputter.setText(this.tagline);
    } else if (this.postType == Type.Instapost) {
      this.postType = Type.Reply;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
        new PostDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      } else {
        new PostDataTask().execute();
      }
    } else if (this.postType == Type.Reply && this.originalText.length() > 0) {
      this.bodyInputter.setSelection(this.originalText.length() - 1);
    }
  }

  @Override
  protected void onDestroy() {
    this.dbHelper.close();
    super.onDestroy();
  }

  @Override
  public final void onPause() {
    final SharedPreferences appPreferences = getSharedPreferences("prefs", 0);
    final Editor editor = appPreferences.edit();

    String postContent = "0";
    String postSubject = "0";

    if (!this.postSubmitted && this.postType != Type.Tagline) {
      postContent = this.bodyInputter.getText().toString().trim();
      postSubject = this.subjectInputter.getText().toString().trim();

      if (postContent.length() > 0) {
        final Toast toast = Toast.makeText(NewPost.this, "Draft Saved", Toast.LENGTH_SHORT);
        toast.show();
      } else {
        postContent = "0";
        postSubject = "0";
      }
    }
    editor.putString(this.mailSession.getServer().serverAddress + "_" + this.subforum + "_" + this.postType + "_" + this.parent + "_" + this.category + "_" + this.postId + "_draft_subject", postSubject);
    editor.putString(this.mailSession.getServer().serverAddress + "_" + this.subforum + "_" + this.postType + "_" + this.parent + "_" + this.category + "_" + this.postId + "_draft", postContent);
    editor.commit();

    super.onPause();
  }

  @Override
  public final void onResume() {
    super.onResume();

    final SharedPreferences appPreferences = getSharedPreferences("prefs", 0);
    final String savedDraft = appPreferences.getString(this.mailSession.getServer().serverAddress + "_" + this.subforum + "_" + this.postType + "_" + this.parent + "_" + this.category + "_" + this.postId + "_draft", "0");
    final String savedSubject = appPreferences.getString(this.mailSession.getServer().serverAddress + "_" + this.subforum + "_" + this.postType + "_" + this.parent + "_" + this.category + "_" + this.postId + "_draft_subject", "0");

    //Restore draft
    if (!savedDraft.contentEquals("0")) {
      this.bodyInputter.setText(savedDraft);
      this.subjectInputter.setText(savedSubject);
    }
  }

  protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    if (resultCode != Activity.RESULT_OK) {
      return;
    }
  }

  private void setColor(final String color) {

    final LinearLayout llPicker = (LinearLayout) findViewById(R.id.profileColorPicker);
    llPicker.setVisibility(View.GONE);


    final String selectedText = this.bodyInputter.getText().toString().substring(this.colorSelectionStart, this.colorSelectionEnd).trim();

    final String firstPart = this.bodyInputter.getText().toString().substring(0, this.colorSelectionStart);
    final String secondPart = this.bodyInputter.getText().toString().substring(this.colorSelectionEnd, this.bodyInputter.getText().toString().length());
    this.bodyInputter.setText(firstPart + "[color=" + color + "]" + selectedText + "[/color]" + secondPart);
    this.bodyInputter.setSelection(this.colorSelectionEnd + ("[color=" + color + "]").length());

    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput(this.bodyInputter, 0);
    this.colorPickerOpen = false;
  }

  @Override
  public final boolean onKeyDown(final int keyCode, final KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (this.colorPickerOpen) {

        final LinearLayout llPicker = (LinearLayout) findViewById(R.id.profileColorPicker);
        llPicker.setVisibility(View.GONE);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this.bodyInputter, 0);

        this.colorPickerOpen = false;
        return true;
      }
    }

    return super.onKeyDown(keyCode, event);
  }

  protected final boolean canHandleCameraIntent() {
    final Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    final List<ResolveInfo> results = getPackageManager().queryIntentActivities(intent, 0);
    return results.size() > 0;
  }

  @Override
  public final boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.post_editor_menu, menu);
    return true;
  }

  @Override
  public final boolean onOptionsItemSelected(final MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.menu_editor_preview:
        this.showPreview();
        break;
      case R.id.menu_editor_select_all:
        this.bodyInputter.selectAll();
        break;
      case R.id.menu_editor_clear_all:
        this.bodyInputter.setText("");
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  private void showPreview() {
    final String previewText = this.bodyInputter.getText().toString().trim().replace("\n", "<br />");

    final Bundle bundle = new Bundle();
    bundle.putString("text", previewText);

    final PreviewDialogFragment newFragment = PreviewDialogFragment.newInstance();
    newFragment.setArguments(bundle);
    newFragment.show(getSupportFragmentManager(), "preview");

  }

  private class PostDataTask extends AsyncTask<String, Void, Object[]> {
    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    protected Object[] doInBackground(final String... args) {
      String comment = bodyInputter.getText().toString().trim();
      if (comment.length() > 1) {
        String subject = theSubject;
      /*
        CookieManager cookiemanager = new CookieManager();
        cookiemanager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookiemanager);

        for(HttpCookie c:mailSession.getCookies()) {
          try {
          URI cookieUri = new URI(c.getDomain());
            cookiemanager.getCookieStore().add(cookieUri, c);
          } catch(Exception ex) {
            //nobody cares
          }
        }
        */
        final Object[] result = new Object[MAX_ITEM_COUNT];
        if (postType == Type.NewThread || postType == Type.Message) {
          subject = subjectInputter.getText().toString();
        }
        subject = subject.trim();

        if (subject.length() > MAX_SUBJECT_LENGTH) {
          subject = subject.substring(0, MAX_SUBJECT_LENGTH - 1);
        }
        if (subject.length() < 1) {
          subject = "no subject";
        }
        if ((postType == Type.NewThread || postType == Type.Reply | postType == Type.Message) && tagline.length() > 0) {
          comment = comment + "\n\n" + tagline;
        }

        try {
          /*
          XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
          config.setUserAgent("Peris");
          config.setServerURL(new URL(mailSession.getServer().serverAddress + "/mobiquo/mobiquo.php"));
          XmlRpcClient client = new XmlRpcClient();
          client.setConfig(config);
          cookiemanager.getCookieStore();

          XmlRpcTransportFactory tFactory = new XmlRpcSun15HttpTransportFactory(client);
          client.setTransportFactory(tFactory);
          */

          if (postType == Type.NewThread) {
            final Vector paramz = new Vector();
            paramz.addElement(category);
            paramz.addElement(subject.getBytes());
            paramz.addElement(comment.getBytes());
            result[0] = mailSession.performSynchronousCall("new_topic", paramz);
          } else if (postType == Type.Reply) {
            final Vector paramz = new Vector();
            paramz.addElement(category);
            paramz.addElement(parent);
            paramz.addElement(subject.getBytes());
            paramz.addElement(comment.getBytes());
            result[0] = mailSession.performSynchronousCall("reply_post", paramz);
          } else if (postType == Type.EditPost) {
            final Vector paramz = new Vector();
            paramz.addElement(postId);
            paramz.addElement(subject.getBytes());
            paramz.addElement(comment.getBytes());
            result[0] = mailSession.performSynchronousCall("save_raw_post", paramz);
          } else if (postType == Type.Message) {
            final byte[][] toname = new byte[1][MAX_ITEM_COUNT];
            toname[0] = category.getBytes();
            Log.d(TAG, "Sending message to " + parent);

            final Vector paramz = new Vector();
            paramz.addElement(toname);
            paramz.addElement(subject.getBytes());
            paramz.addElement(comment.getBytes());
            result[0] = mailSession.performSynchronousCall("create_message", paramz);
          }
          //cookiemanager.getCookieStore();
          return result;
        } catch (Exception e) {
          Log.w(TAG, e.getMessage());
        }
      }
      return null;
    }

    //This method is executed after the thread has completed.
    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final Object[] result) {
      if (result == null) {
        submitter.setEnabled(true);

        final Toast toast = Toast.makeText(NewPost.this, "Submission error, please retry :-(", Toast.LENGTH_LONG);
        toast.show();
        postSubmitted = false;
        return;
      }

      postSubmitted = true;
      NewPost.this.setResult(1);
      finish();
    }
  }
}
