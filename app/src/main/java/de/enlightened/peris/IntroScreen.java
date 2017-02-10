package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.enlightened.peris.db.PerisDBHelper;
import de.enlightened.peris.db.ServerRepository;
import de.enlightened.peris.support.Net;


@SuppressLint("NewApi")
@SuppressWarnings("checkstyle:requirethis")
public class IntroScreen extends FragmentActivity {

  private AnalyticsHelper ah;

  private EditText serverInputter;
  private ListView lvServers;
  private GridView gvServers;

  private boolean isStealingLink;

  private Server selectedServer;

  private ProgressDialog progress;


  private String preinstalledServers = "http://forum.xda-developers.com";
  private PerisDBHelper dbHelper;
  private List<Server> serverList;

  private boolean incomingShortcut;
  private String shortcutServerId = "0";
  private boolean stealingLink;
  private String linkToSteal = "0";
  private String stealingType = "0";
  private String stealingLocation = "0";
  private ArrayList<ForumManifestCheckerTask> runningManifestChecks;
  private ServerAdapter sAdapterTemp;

  public static Server parseServerData(final Cursor c) {
    final Server parsedServer = new Server();

    parsedServer.serverUserId = c.getString(c.getColumnIndex("userid"));
    parsedServer.serverName = c.getString(c.getColumnIndex("servername"));
    parsedServer.serverAddress = c.getString(c.getColumnIndex("server"));
    parsedServer.serverAvatar = c.getString(c.getColumnIndex("avatar"));
    parsedServer.serverUserName = c.getString(c.getColumnIndex("username"));
    parsedServer.serverColor = c.getString(c.getColumnIndex("color"));
    parsedServer.serverId = Integer.toString(c.getInt(c.getColumnIndex("_id")));
    parsedServer.serverPassword = c.getString(c.getColumnIndex("password"));
    parsedServer.serverTheme = c.getString(c.getColumnIndex("themeInt"));
    parsedServer.serverIcon = c.getString(c.getColumnIndex("icon"));
    parsedServer.serverTagline = c.getString(c.getColumnIndex("tagline"));
    parsedServer.chatForum = c.getString(c.getColumnIndex("chatForum"));
    parsedServer.chatName = c.getString(c.getColumnIndex("chatName"));
    parsedServer.chatThread = c.getString(c.getColumnIndex("chatThread"));
    parsedServer.ffChatId = c.getString(c.getColumnIndex("ffchat"));

    if (c.getColumnIndex("analytics") > -1) {
      parsedServer.analyticsId = c.getString(c.getColumnIndex("analytics"));
    }

    if (c.getColumnIndex("mobfox") > -1) {
      parsedServer.mobfoxId = c.getString(c.getColumnIndex("mobfox"));
    }

    if (c.getColumnIndex("background") > -1) {
      parsedServer.serverBackground = c.getString(c.getColumnIndex("background"));
    }

    if (c.getColumnIndex("boxcolor") > -1) {
      parsedServer.serverBoxColor = c.getString(c.getColumnIndex("boxcolor"));
    }

    if (c.getColumnIndex("boxborder") > -1) {
      parsedServer.serverBoxBorder = c.getString(c.getColumnIndex("boxborder"));
    }

    if (c.getColumnIndex("textcolor") > -1) {
      parsedServer.serverTextColor = c.getString(c.getColumnIndex("textcolor"));
    }

    if (c.getColumnIndex("dividercolor") > -1) {
      parsedServer.serverDividerColor = c.getString(c.getColumnIndex("dividercolor"));
    }

    if (c.getColumnIndex("wallpaper") > -1) {
      parsedServer.serverWallpaper = c.getString(c.getColumnIndex("wallpaper"));
    }

    if (c.getColumnIndex("https") > -1) {
      parsedServer.serverHttps = c.getInt(c.getColumnIndex("https")) == 1;
    }

    return parsedServer;
  }

  public static Bitmap getBitmapFromURL(final String src) {
    try {
      final URL url = new URL(src);
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.connect();
      final InputStream input = connection.getInputStream();
      final Bitmap myBitmap = BitmapFactory.decodeStream(input);
      return myBitmap;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @SuppressLint("NewApi")
  public final void onCreate(final Bundle savedInstanceState) {
    this.dbHelper = new PerisDBHelper(this);
    final Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      if (bundle.containsKey("server_id")) {
        if (bundle.getString("server_id") != null) {

          this.incomingShortcut = true;
          this.shortcutServerId = bundle.getString("server_id");
        }
      }
    }

    final PerisApp app = (PerisApp) getApplication();
    app.initSession();

    startService(new Intent(this, MailService.class));
    this.initDatabase();

    final SharedPreferences appPreferences = getSharedPreferences("prefs", 0);


    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    final SharedPreferences.Editor editor = appPreferences.edit();
    editor.putString("server_address", getString(R.string.server_location));
    editor.commit();

    final String backgroundColor = app.getSession().getServer().serverColor;

    ThemeSetter.setTheme(this, backgroundColor);

    super.onCreate(savedInstanceState);

    ThemeSetter.setActionBar(this, backgroundColor);

    //Track app analytics
    this.ah = ((PerisApp) getApplication()).getAnalyticsHelper();
    this.ah.trackScreen(getString(R.string.app_name) + " v" + getString(R.string.app_version) + " for Android", true);

    setContentView(R.layout.intro_screen);

    this.serverInputter = (EditText) findViewById(R.id.intro_screen_add_server_box);

    final Button serverAdder = (Button) findViewById(R.id.intro_screen_submit_new_server);

    serverAdder.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          new ServerValidationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverInputter.getText().toString().trim());
        } else {
          new ServerValidationTask().execute(serverInputter.getText().toString().trim());
        }

      }
    });

    this.lvServers = (ListView) findViewById(R.id.intro_screen_server_list);
    this.gvServers = (GridView) findViewById(R.id.intro_screen_server_grid);

    if (this.lvServers == null) {
      registerForContextMenu(this.gvServers);
      this.gvServers.setOnItemClickListener(new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
          final Server server = serverList.get(position);
          connectToServer(server);
        }
      });
    } else {
      this.lvServers.setDivider(null);
      registerForContextMenu(this.lvServers);
      this.lvServers.setOnItemClickListener(new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
          final Server server = serverList.get(position);
          connectToServer(server);
        }
      });
    }


    final TextView tvTapaShoutout = (TextView) findViewById(R.id.intro_screen_app_title);
    tvTapaShoutout.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/McNetic/peris"));
        startActivity(browserIntent);
      }
    });

    //Check for incoming link from tapatalk :-)
    String host = "";

    final Uri data = getIntent().getData();

    if (data != null) {
      host = data.getHost();
      this.stealingType = data.getQueryParameter("location");

      if (this.stealingType == null) {
        this.stealingType = "0";
      } else {
        if (this.stealingType.contentEquals("forum")) {
          final String forumId = data.getQueryParameter("fid");

          if (forumId == null) {
            this.stealingLocation = "0";
          } else {
            this.stealingLocation = forumId;
          }
        }

        if (this.stealingType.contentEquals("topic")) {
          final String topicId = data.getQueryParameter("tid");

          if (topicId == null) {
            this.stealingLocation = "0";
          } else {
            this.stealingLocation = topicId;
          }
        }
      }

    }

    if (host.length() > 0) {
      this.linkToSteal = host;
      this.stealingLink = true;
      return;
    }

  }

  @Override
  public final void onResume() {
    super.onResume();

    final PerisApp app = (PerisApp) getApplication();
    app.initSession();
    app.setActive(false);

    final RelativeLayout connectingLayout = (RelativeLayout) findViewById(R.id.intro_connecting_layout);
    connectingLayout.setVisibility(View.GONE);

    getActionBar().show();

    if (this.stealingLink) {
      stealTapatalkLink(this.linkToSteal);
      return;
    }

    if (getString(R.string.server_location).contentEquals("0") && !this.incomingShortcut) {
      refreshList();
    } else {
      connectToServer(this.selectedServer);
    }

    final TextView tvUpgrade = (TextView) findViewById(R.id.intro_screen_remove_ads);
    tvUpgrade.setVisibility(View.GONE);
  }

  @Override
  public final void onStart() {
    super.onStart();
  }

  @Override
  public final void onStop() {
    super.onStop();
  }

  @Override
  public final void onDestroy() {
    if (getString(R.string.server_location).contentEquals("0")) {
      final SharedPreferences appPreferences = getSharedPreferences("prefs", 0);
      final Editor editor = appPreferences.edit();
      editor.putBoolean("ff_clean_close", true);
      editor.commit();
    }
    dbHelper.close();
    super.onDestroy();
  }

  private void refreshList() {
    runningManifestChecks = new ArrayList<ForumManifestCheckerTask>();
    serverList = ServerRepository.findAll(dbHelper.getReadableDatabase());

    for (final Server server : serverList) {
      final ForumManifestCheckerTask manifestCheck = new ForumManifestCheckerTask();
      runningManifestChecks.add(manifestCheck);
      manifestCheck.execute(server);
    }

    if (lvServers == null) {
      gvServers.setAdapter(new ServerAdapter(serverList, this));
    } else {
      lvServers.setAdapter(new ServerAdapter(serverList, this));
    }
  }

  public final void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {

    final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

    if (lvServers == null) {
      selectedServer = (Server) gvServers.getAdapter().getItem(info.position);
    } else {
      selectedServer = (Server) lvServers.getAdapter().getItem(info.position);
    }


    super.onCreateContextMenu(menu, v, menuInfo);
    menu.setHeaderTitle(selectedServer.getUrlString());
    final MenuInflater inflater = getMenuInflater();

    inflater.inflate(R.menu.intro_context, menu);
  }

  public final boolean onContextItemSelected(final MenuItem item) {
    final int itemId = item.getItemId();
    final boolean rv;
    if (itemId == R.id.intro_context_remove) {
      removeServer(selectedServer);
      rv = true;
    } else if (itemId == R.id.intro_shortcut) {
      createHomescreenShortcut(selectedServer);
      rv = true;
    } else if (itemId == R.id.intro_context_rename) {
      renameServer(selectedServer);
      rv = true;
    } else {
      rv = super.onContextItemSelected(item);
    }
    return rv;
  }

  @SuppressWarnings("checkstyle:nestedfordepth")
  private String checkForTapatalk(final String enteredURL) {
    final Pattern pattern = Pattern.compile(
        "^(?:(http(?:s)?)://)?"
            + "((?:www|forum[s]?|board|community|discussions).)?"
            + "([^/]+)"
            + "(/(:?forum[s]?|board|community|discussions))?"
            + "(?:(/(mobiquo(/(mobiquo.php)?)?)?)?)?$");
    final Matcher matcher = pattern.matcher(enteredURL);
    if (matcher.matches()) {
      //String protocol = matcher.group(1);
      String givenSubdomain = matcher.group(2);
      final String domain = matcher.group(3);
      String givenDirectory = matcher.group(4);
      if (givenSubdomain == null) {
        givenSubdomain = "";
      }
      if (givenDirectory == null) {
        givenDirectory = "";
      } else if (givenDirectory.endsWith("/")) {
        givenDirectory = givenDirectory.substring(0, givenDirectory.length() - 1);
      }

      final ArrayList<String> subdomains = new ArrayList(Arrays.asList("", "www.", "forum.", "forums.", "board.", "community.", "discussions."));
      subdomains.remove(givenSubdomain);
      subdomains.add(0, givenSubdomain);
      final ArrayList<String> directories = new ArrayList(Arrays.asList("", "/forum", "/forums", "/board", "/community", "/discussions"));
      directories.remove(givenDirectory);
      directories.add(0, givenDirectory);
      for (final String protocol : new String[]{"https", "http"}) {
        for (final String subdomain : subdomains) {
          for (final String directory : directories) {
            final String url = String.format("%s://%s%s%s", protocol, subdomain, domain, directory);
            Log.d("Peris", "Checking: " + url);
            if (Net.checkURL(url + "/mobiquo/mobiquo.php")) {
              return url;
            }
          }
        }
      }
    }
    return null;
  }

  private void stealTapatalkLink(final String link) {
    final String queryLink;

    if (!link.startsWith("http://")) {
      queryLink = "http://" + link;
    } else {
      queryLink = link;
    }

    if (!getString(R.string.server_location).contentEquals("0")) {
      if (queryLink.contentEquals(getString(R.string.server_location))) {
        connectToServer(selectedServer);
      } else {
        final AlertDialog.Builder builder = new AlertDialog.Builder(IntroScreen.this);
        builder.setTitle("Download Peris");
        builder.setCancelable(true);
        builder.setPositiveButton("Yep!", new DialogInterface.OnClickListener() {
          public void onClick(final DialogInterface dialog, final int which) {

            final String perisURL = "https://github.com/McNetic/peris/";
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(perisURL));
            IntroScreen.this.startActivity(intent);

            finish();
          }
        });
        builder.setNegativeButton("Nah.", new DialogInterface.OnClickListener() {
          public void onClick(final DialogInterface dialog, final int which) {
            finish();
          }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
          public void onCancel(final DialogInterface dialog) {
            finish();
          }
        });

        builder.setMessage("Do you want to download and view " + link + " using Peris, the free mobile forum reader app that " + getString(R.string.app_name) + " is based off of?");
        builder.create().show();
      }
    } else {
      selectedServer = ServerRepository.findOneByAddress(dbHelper.getReadableDatabase(), queryLink);
      if (selectedServer != null) {
        if (selectedServer.serverAddress != null) {
          connectToServer(selectedServer);
          return;
        }
      }

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
        new ServerValidationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, link.trim());
      } else {
        new ServerValidationTask().execute(link.trim());
      }
    }
  }

  private void initDatabase() {
    //Juice up the server for standalone app
    if (!"0".equals(this.getString(R.string.server_location)) || incomingShortcut) {
      final SQLiteDatabase db = dbHelper.getReadableDatabase();
      if (incomingShortcut) {
        this.selectedServer = ServerRepository.findOne(db, Long.parseLong(this.shortcutServerId));
      } else {
        this.selectedServer = ServerRepository.findOneByAddress(db, this.getString(R.string.server_location));
      }
    }
  }

  private Server createDefaultServer(final String serverUrl) {
    final Server server = new Server();
    server.serverAddress = Net.removeProtocol(serverUrl);
    server.serverHttps = serverUrl.startsWith("https://");
    server.serverColor = getString(R.string.default_color);
    server.serverBackground = getString(R.string.default_background);
    server.serverTheme = getString(R.string.default_theme);
    server.chatThread = getString(R.string.chat_thread);
    server.chatForum = getString(R.string.chat_forum);
    server.chatName = getString(R.string.chat_name);
    server.serverBoxColor = getString(R.string.default_element_background);
    server.serverBoxBorder = getString(R.string.default_element_border);
    server.serverTextColor = getString(R.string.default_text_color);
    server.serverDividerColor = getString(R.string.default_divider_color);
    server.serverWallpaper = getString(R.string.default_wallpaper_url);
    return server;
  }

  private void addNewServer(final String serverUrl) {
    final SQLiteDatabase db = dbHelper.getWritableDatabase();
    final Server server = this.createDefaultServer(serverUrl);
    ServerRepository.add(db, server);
  }

  private void connectToServer(final Server server) {

    if (runningManifestChecks != null) {
      int killedManifests = 0;

      for (ForumManifestCheckerTask cfm : runningManifestChecks) {
        if (cfm.getStatus() == Status.RUNNING) {
          cfm.cancel(true);
          killedManifests++;

        }
      }

      if (killedManifests > 0) {
        Log.i("Peris", "Killed " + killedManifests + " manifest checks!");
      }
    }

    final PerisApp app = (PerisApp) getApplication();

    app.initSession();

    if (server.serverTagline.contentEquals("[*WEBVIEW*]")) {
      app.getSession().setServer(server);
      final Intent myIntent = new Intent(IntroScreen.this, WebViewer.class);
      startActivity(myIntent);
      return;
    }

    final RelativeLayout connectingLayout = (RelativeLayout) findViewById(R.id.intro_connecting_layout);
    connectingLayout.setVisibility(View.VISIBLE);

    final TextView tvServerConnectionText = (TextView) findViewById(R.id.intro_connecting_text);
    tvServerConnectionText.setText("Logging in to\n" + server.getUrlString());

    if (server.serverColor.contains("#")) {
      connectingLayout.setBackgroundColor(Color.parseColor(server.serverColor));
      tvServerConnectionText.setTextColor(Color.parseColor(ThemeSetter.getForeground(server.serverColor)));
    }

    //getActionBar().hide();

    app.getSession().setSessionListener(new Session.SessionListener() {

      @Override
      public void onSessionConnected() {
        loadForum();
      }

      @Override
      public void onSessionConnectionFailed(final String reason) {
        final Toast toast = Toast.makeText(IntroScreen.this, "Unable to log in: " + reason, Toast.LENGTH_LONG);
        toast.show();

        loadForum();
      }

    });
    app.getSession().setServer(server);
  }

  private void loadForum() {

    final PerisApp app = (PerisApp) getApplication();
    app.freshBackstack();

    final Bundle bundle = new Bundle();
    if (stealingLink) {
      bundle.putBoolean("stealing", true);
      bundle.putString("stealing_type", stealingType);
      bundle.putString("stealing_location", stealingLocation);
    } else {
      bundle.putBoolean("stealing", false);
    }

    stealingLink = false;

    final Intent myIntent = new Intent(IntroScreen.this, PerisMain.class);
    myIntent.putExtras(bundle);
    startActivity(myIntent);

    //Close the intro screen on stand alone apps
    if (!getString(R.string.server_location).contentEquals("0") || incomingShortcut) {
      finish();
    }
  }

  private void removeServer(final Server server) {
    ServerRepository.delete(dbHelper.getWritableDatabase(), server);
    refreshList();
  }

  @Override
  public final boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.intro_menu, menu);

    return true;
  }

  public final boolean onOptionsItemSelected(final MenuItem item) {
    final int itemId = item.getItemId();

    if (itemId == R.id.intro_menu_owners) {
      final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/McNetic/peris"));
      startActivity(browserIntent);
    } else if (itemId == R.id.intro_menu_about) {
      final Intent aboutIntent = new Intent(IntroScreen.this, About.class);
      startActivity(aboutIntent);
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  private void createHomescreenShortcut(final Server server) {
    new IconMakerTask().execute(server);
  }

  private void askAboutWebview() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(IntroScreen.this);
    builder.setTitle("Tapatalk API Not Found");
    builder.setCancelable(true);
    builder.setPositiveButton("Try WebView", new DialogInterface.OnClickListener() {
      public void onClick(final DialogInterface dialog, final int which) {
        addWebViewServer(serverInputter.getText().toString().trim());

        refreshList();

        serverInputter.setText("");

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(serverInputter.getWindowToken(), 0);
      }
    });
    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(final DialogInterface dialog, final int which) {
        //do nothing
      }
    });
    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      public void onCancel(final DialogInterface dialog) {
        //do nothing
      }
    });

    builder.setMessage("The Tapatalk API cannot be found at the URL you provided.  Do you want to view this forum in a WebView instead?  If not, you can go back and try to re-enter your server information.");
    builder.create().show();

    return;
  }

  private void addWebViewServer(final String serverUrl) {
    final SQLiteDatabase db = dbHelper.getWritableDatabase();
    final Server server = this.createDefaultServer(serverUrl);
    server.serverUserName = "WebView Forum";
    server.serverTagline = "[*WEBVIEW*]";
    ServerRepository.add(db, server);
  }

  @Override
  protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

    super.onActivityResult(requestCode, resultCode, data);
  }

  private void renameServer(final Server server) {

    String oldName = server.serverName;

    if (oldName.contentEquals("0")) {
      oldName = server.getUrlString();
    }

    final EditText input = new EditText(this);
    input.setText(oldName);

    new AlertDialog.Builder(this)
        .setTitle("Rename Server")
        .setMessage("Choose a new display name for this server.")
        .setView(input)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(final DialogInterface dialog, final int whichButton) {
            runOnUiThread(new Runnable() {
              public void run() {
                final String trimmedName = input.getText().toString().trim();
                if (trimmedName.length() > 0) {
                  server.serverName = trimmedName;
                  ServerRepository.update(dbHelper.getWritableDatabase(), server);
                  refreshList();
                }
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

  private class ServerValidationTask extends AsyncTask<String, Void, String> {

    protected void onPreExecute() {
      progress = ProgressDialog.show(IntroScreen.this, "Please Wait", "Validating server information, please wait.", true);
    }

    @Override
    protected String doInBackground(final String... params) {
      final String validServer = checkForTapatalk(params[0]);

      return validServer;
    }

    protected void onPostExecute(final String result) {

      try {
        progress.dismiss();
      } catch (Exception ex) {
        Log.d("Peris", ex.getMessage());
      }

      if (result == null) {
        if (isStealingLink) {
          finish();
        } else {
          askAboutWebview();
        }
        return;
      } else {

        addNewServer(result);
        refreshList();

        serverInputter.setText("");

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(serverInputter.getWindowToken(), 0);

        if (isStealingLink) {
          final SharedPreferences appPreferences = getSharedPreferences("prefs", 0);
          final SharedPreferences.Editor editor = appPreferences.edit();

          String url = result;

          if (!url.contains("http")) {
            url = "http://" + url;
          }

          editor.putString("server_address", url);
          editor.putString(url + "_forumScrollPosition0", "0");
          editor.commit();

          final Intent myIntent = new Intent(IntroScreen.this, PerisMain.class);
          startActivity(myIntent);
        }
      }
    }
  }

  private class ForumManifestCheckerTask extends AsyncTask<Server, Void, String> {

    private Server passedServer;

    protected String doInBackground(final Server... params) {


      passedServer = params[0];

      final URL manifestUrl = passedServer.getURL("peris.json");

      if (Net.checkURL(manifestUrl)) {

        try {
          final HttpClient httpclient = new DefaultHttpClient();
          final HttpGet httpget = new HttpGet(manifestUrl.toURI());
          httpget.setHeader("User-Agent", "Peris");
          final ResponseHandler<String> responseHandler = new BasicResponseHandler();
          final String responseBody = httpclient.execute(httpget, responseHandler);
          return responseBody;
        } catch (Exception ex) {
          Log.d("Peris", ex.getMessage());
        }
      }

      return null;
    }

    protected void onPostExecute(final String result) {
      if (result == null) {
        return;
      }

      try {
        final JSONObject jo = new JSONObject(result);

        final String manifestName = jo.optString("name", "");
        final String manifestIcon = jo.optString("icon", "");
        final String manifestColor = jo.optString("color", "");
        final String manifestChatName = jo.optString("chat_name", "");
        final String manifestChatTopic = jo.optString("chat_topic", "");
        final String manifestChatForum = jo.optString("chat_forum", "");
        final String manifestFFChatId = jo.optString("ff_chat_id", "");
        final String manifestAnalytics = jo.optString("g_analytics", "");
        final String manifestMobfox = jo.optString("an_mobfox_id", "");

        int validFields = 0;

        if (manifestAnalytics.length() > 0) {
          passedServer.analyticsId = manifestAnalytics;
          validFields++;
        }

        if (manifestMobfox.length() > 0) {
          passedServer.mobfoxId = manifestMobfox;
          validFields++;
        }

        if (manifestFFChatId.length() > 0) {
          passedServer.ffChatId = manifestFFChatId;
          validFields++;
        }

        if (manifestName.length() > 0) {
          passedServer.serverName = manifestName;
          validFields++;
        }

        if (manifestColor.length() > 0 && (passedServer.serverColor.contentEquals("0") || passedServer.serverColor.contentEquals(getString(R.string.default_color)))) {
          if (manifestColor.length() == 7) {
            if (manifestColor.substring(0, 1).contentEquals("#")) {
              passedServer.serverColor = manifestColor;
            }
          }
          validFields++;
        }

        if (manifestIcon.length() > 0) {
          passedServer.serverIcon = manifestIcon;
          validFields++;
        }

        if (manifestChatName.length() > 0 && !manifestChatName.contentEquals("0")) {
          passedServer.chatName = manifestChatName;
          validFields++;
        }

        if (manifestChatForum.length() > 0 && !manifestChatForum.contentEquals("0")) {
          passedServer.chatForum = manifestChatForum;
          validFields++;
        }

        if (manifestChatTopic.length() > 0 && !manifestChatTopic.contentEquals("0")) {
          passedServer.chatThread = manifestChatTopic;
          validFields++;
        }

        final PerisApp app = (PerisApp) getApplication();
        app.getSession().updateSpecificServer(passedServer);

        if (lvServers == null) {
          sAdapterTemp = (ServerAdapter) gvServers.getAdapter();
        } else {
          sAdapterTemp = (ServerAdapter) lvServers.getAdapter();
        }

        runOnUiThread(new Runnable() {

          public void run() {
            sAdapterTemp.notifyDataSetChanged();
          }
        });

        if (validFields > 0) {
          ah.trackEvent("peris manifest", "parsed", passedServer.serverAddress, false);
        }
      } catch (Exception ex) {
        Log.e("Peris", passedServer.serverAddress + " ex1 - Invalid JSON Object!");
      }
    }
  }

  private class IconMakerTask extends AsyncTask<Server, Void, Bitmap> {
    private Server server;

    @Override
    protected Bitmap doInBackground(final Server... params) {
      server = params[0];
      final Bitmap bitmap;

      if (server.serverIcon.contains("png") || server.serverIcon.contains("ico")) {
        final int size = (int) getResources().getDimension(android.R.dimen.app_icon_size);
        bitmap = Bitmap.createScaledBitmap(getBitmapFromURL(server.serverIcon), size, size, true);
      } else {
        bitmap = null;
      }

      return bitmap;
    }

    protected void onPostExecute(final Bitmap result) {
      final Intent shortcutIntent = new Intent(IntroScreen.this, IntroScreen.class);
      shortcutIntent.putExtra("server_id", server.serverId);

      final Intent intent = new Intent();
      intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

      shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

      if (server.serverName.contentEquals("0")) {
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, server.serverAddress);
      } else {
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, server.serverName);
      }

      if (result == null) {
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(IntroScreen.this, R.drawable.ic_launcher));
      } else {
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, result);
      }

      intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
      sendBroadcast(intent);

      final Toast toast = Toast.makeText(IntroScreen.this, "Homescren Icon Created!", Toast.LENGTH_LONG);
      toast.show();
    }

  }
}
