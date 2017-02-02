package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

@SuppressLint("NewApi")
public class SocialFragment extends Fragment {

  private static final int MAX_ITEM_COUNT = 50;
  private static final int POSTS_PER_PAGE = 20;
  private static final int SOCIAL_RATE = 30_000;
  private static final long COUNTDOWN_INTERVAL = 1000;

  private ListView socialList;
  private EditText newStatus;
  private Post selectedPost;
  private String chatForum = "0";
  private String chatThread = "0";
  private Button updateStatusButton;
  private DownloadStatusesTask socialLoader;
  private PerisApp application;
  private SocialTimer socialTimer;
  private String newChatId = "0";
  private OnClickListener clickListener = new OnClickListener() {
    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View v) {
      submitPost();
    }
  };
  private ProfileSelectedListener profileSelectedListener = null;
  private AdapterView.OnItemClickListener socailItemTapped = new AdapterView.OnItemClickListener() {
    @SuppressWarnings("checkstyle:requirethis")
    public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {
      final Post sender = (Post) arg0.getItemAtPosition(arg2);
      if (profileSelectedListener != null) {
        profileSelectedListener.onProfileSelected(sender.author, sender.authorId);
      }
    }
  };

  @Override
  public void onCreate(final Bundle bundle) {
    super.onCreate(bundle);

    this.application = (PerisApp) getActivity().getApplication();

    this.newChatId = this.application.getSession().getServer().ffChatId;

    Log.d("Peris", "newChatId is " + this.newChatId);

    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.social, container, false);
    }

  @Override
  public void onStart() {

    super.onStart();

    this.chatForum = this.application.getSession().getServer().chatForum;
    this.chatThread = this.application.getSession().getServer().chatThread;
    this.socialList = (ListView) getActivity().findViewById(R.id.social_list_view);
    this.socialList.setDivider(null);
    this.updateStatusButton = (Button) getActivity().findViewById(R.id.social_submit_status);
    this.newStatus = (EditText) getActivity().findViewById(R.id.social_status);

    if (this.application.getSession().getServer().serverColor.contains("#")) {
      this.updateStatusButton.setTextColor(Color.parseColor(this.application.getSession().getServer().serverColor));
    }

    if (this.application.getSession().getServer().serverTextColor.contains("#")) {
      this.newStatus.setTextColor(Color.parseColor(this.application.getSession().getServer().serverTextColor));

      if (this.application.getSession().getServer().serverColor.contentEquals(this.application.getSession().getServer().serverBoxColor)) {
        this.updateStatusButton.setTextColor(Color.parseColor(this.application.getSession().getServer().serverTextColor));
      }
    }

    final Bundle bundle = getArguments();
    final String sharedText = bundle.getString("shared_text");
    if (sharedText.length() > 1) {
      this.newStatus.setText(sharedText);
    }
    this.newStatus.setOnEditorActionListener(new OnEditorActionListener() {

      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          submitPost();
          return true;
        }
        return false;
      }

    });

    this.updateStatusButton.setOnClickListener(this.clickListener);

    String boxColor = getString(R.string.default_element_background);

    if (this.application.getSession().getServer().serverBoxColor != null) {
      boxColor = this.application.getSession().getServer().serverBoxColor;
    }

    if (boxColor.contains("#")) {
      final LinearLayout chatInputArea = (LinearLayout) getActivity().findViewById(R.id.chat_input_area);
      chatInputArea.setBackgroundColor(Color.parseColor(boxColor));
    }

  }

  @Override
  public void onDestroy() {

    if (this.socialTimer != null) {
      this.socialTimer.cancel();
      this.socialTimer = null;
    }

    super.onDestroy();
  }

  @Override
  public void onPause() {
    if (this.socialTimer != null) {
      this.socialTimer.cancel();
    }

    super.onPause();
  }

  @Override
  public void onResume() {
    /*
    SharedPreferences app_preferences = getActivity().getSharedPreferences("prefs", 0);
    String cached_social = app_preferences.getString("social_list", "n/a");

        if(!(cached_social.contentEquals("n/a"))) {
          try {
          Object[] forumObject = GsonHelper.customGson.fromJson(cached_social, Object[].class);
            parseCachedSocial(forumObject);
          } catch(Exception ex) {
            //don't do anything
          }
        }
    */
    this.loadStatuses();
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      this.getActivity().invalidateOptionsMenu();
    }
    super.onResume();

    this.socialTimer = new SocialTimer(this.SOCIAL_RATE, COUNTDOWN_INTERVAL);
    this.socialTimer.start();
  }

  @Override
  public void onStop() {
    super.onStop();

    //Stop any running tasks
    if (this.socialLoader != null) {
      if (this.socialLoader.getStatus() == Status.RUNNING) {
        this.socialLoader.cancel(true);
      }
    }
  }

  private void loadStatuses() {
    this.socialLoader = new DownloadStatusesTask();

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      this.socialLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      this.socialLoader.execute();
    }

  }

  private void submitPost() {
    this.newStatus.setEnabled(false);
    this.updateStatusButton.setEnabled(false);

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      new SocialPostTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      new SocialPostTask().execute();
    }
  }

  @SuppressLint("NewApi")
  @SuppressWarnings("deprecation")
  private void storePostInClipboard() {

    //Copy text support for all Android versions
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
      final ClipData cd = ClipData.newHtmlText(this.selectedPost.author + "'s Social Post", this.selectedPost.body, this.selectedPost.body);
      clipboard.setPrimaryClip(cd);
    } else {
      final android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
      clipboard.setText(this.selectedPost.body);
    }

    final Toast toast = Toast.makeText(this.getActivity(), "Text copied!", Toast.LENGTH_SHORT);
    toast.show();
  }

  public void setOnProfileSelectedListener(final ProfileSelectedListener l) {
    this.profileSelectedListener = l;
  }
    /*
    @SuppressLint("NewApi")
  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

      if(getString(R.string.chat_forum).contentEquals("0")) {
        inflater.inflate(R.menu.chat_menu, menu);

        if(ForegroundColorSetter.getForegroundDark(background)) {
          MenuItem removeItem = menu.findItem(R.id.menu_chat_remove);
          removeItem.setIcon(R.drawable.ic_action_remove_dark);
        }
      }

      super.onCreateOptionsMenu(menu, inflater);

  }

    @Override
  public boolean onOptionsItemSelected (MenuItem item) {
    switch (item.getItemId()) {
        case R.id.menu_chat_remove:
          removeChat();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
  }

    private void removeChat() {
      application.getSession().getServer().chatForum = "0";
      application.getSession().getServer().chatThread = "0";
      application.getSession().getServer().chatName = "0";
      application.getSession().updateServer();

        getActivity().finish();
      getActivity().startActivity(getActivity().getIntent());
    }
    */

  //Profile selected interface
  public interface ProfileSelectedListener {
    void onProfileSelected(String username, String userid);
  }

  private class SocialPostTask extends AsyncTask<String, Void, Object[]> {
    @SuppressWarnings({"rawtypes", "unchecked", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final String... params) {
      //String tagline = application.getSession().getServer().serverTagline;
      final String comment = newStatus.getText().toString().trim();
      final String subject = "RE: Social";

      /*
      if(tagline.length() > 0) {
        comment = comment + "\n\n" + tagline;
      }
      */
      final Object[] result = new Object[MAX_ITEM_COUNT];
      try {
        final Vector paramz = new Vector();
        paramz.addElement(chatForum);
        paramz.addElement(chatThread);
        paramz.addElement(subject.getBytes());
        paramz.addElement(comment.getBytes());
        result[0] = application.getSession().performSynchronousCall("reply_post", paramz);
      } catch (Exception e) {
        Log.w("Peris", e.getMessage());
        return null;
      }
      return result;
    }

    @SuppressWarnings("checkstyle:requirethis")
    protected void onPostExecute(final Object[] result) {
      if (result == null) {
        final Toast toast = Toast.makeText(getActivity(), "Error connecting to the server!  erSPE", Toast.LENGTH_SHORT);
        toast.show();
        return;
      }

      loadStatuses();
      newStatus.setText("");
      updateStatusButton.setEnabled(true);
      newStatus.setEnabled(true);
    }
  }

  private class DownloadStatusesTask extends AsyncTask<String, Void, Object[]> {
    @SuppressWarnings({"unchecked", "rawtypes", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final String... params) {
      final Object[] result = new Object[MAX_ITEM_COUNT];
      int minPost = 0;
      int maxPost = POSTS_PER_PAGE - 1;

      try {
        Vector paramz = new Vector();
        paramz.addElement(chatThread);
        paramz.addElement(minPost);
        paramz.addElement(minPost);
        paramz.addElement(true);

        final HashMap map = (HashMap) application.getSession().performSynchronousCall("get_thread", paramz);
        maxPost = (Integer) map.get("total_post_num");
        minPost = maxPost - POSTS_PER_PAGE;

        paramz = new Vector();
        paramz.addElement(chatThread);
        paramz.addElement(minPost);
        paramz.addElement(maxPost);
        paramz.addElement(true);

        result[0] = application.getSession().performSynchronousCall("get_thread", paramz);
      } catch (Exception e) {
        if (e.getMessage() != null) {
          Log.w(getString(R.string.app_name), e.getMessage());
        } else {
          Log.w(getString(R.string.app_name), "Chat connection error!");
        }
        return null;
      }
      return result;
    }

    @SuppressWarnings({"rawtypes", "checkstyle:nestedifdepth", "checkstyle:requirethis"})
    protected void onPostExecute(final Object[] result) {
      if (result == null) {
        final Toast toast = Toast.makeText(getActivity(), "Cannot connect to chat!", Toast.LENGTH_SHORT);
        toast.show();
      } else {
        final String objectString = GsonHelper.customGson.toJson(result);
        final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);
        final String cachedForum = appPreferences.getString("social_list", "n/a");

        if (objectString.contentEquals(cachedForum)) {
          return;
        } else {
          final SharedPreferences.Editor editor = appPreferences.edit();
          editor.putString("social_list", objectString);
          editor.commit();
        }

        if (getActivity() != null) {
          final ArrayList<Post> postList = new ArrayList<Post>();
          for (Object o : result) {
            if (o != null) {
              final HashMap map = (HashMap) o;

              if (map.containsKey("posts")) {
                final Object[] topics = (Object[]) map.get("posts");
                for (Object t : topics) {
                  final HashMap topicMap = (HashMap) t;
                  final Date timestamp = (Date) topicMap.get("post_time");
                  final Post po = new Post();
                  po.categoryId = "108";
                  po.subforumId = "108";
                  po.threadId = "21";

                  if (topicMap.containsKey("is_online")) {
                    po.userOnline = (Boolean) topicMap.get("is_online");
                  }

                  po.author = new String((byte[]) topicMap.get("post_author_name"));
                  po.authorId = (String) topicMap.get("post_author_id");
                  po.body = new String((byte[]) topicMap.get("post_content"));
                  po.avatar = (String) topicMap.get("icon_url");
                  po.id = (String) topicMap.get("post_id");
                  po.tagline = "tagline";
                  po.timestamp = timestamp.toString();
                  postList.add(0, po);
                }
              }
            }
          }

          final int position = socialList.getFirstVisiblePosition();
          socialList.setOnItemClickListener(socailItemTapped);
          socialList.setItemsCanFocus(true);
          socialList.setAdapter(new PostAdapter(postList, getActivity(), application, -1));
          socialList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
          socialList.setSelectionFromTop(position, 0);
        }
      }
    }
  }

  public class SocialTimer extends CountDownTimer {

    public SocialTimer(final long millisInFuture, final long countDownInterval) {
      super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(final long millisUntilFinished) {
      //whatever
    }

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onFinish() {
      loadStatuses();
      socialTimer = new SocialTimer(SOCIAL_RATE, COUNTDOWN_INTERVAL);
      socialTimer.start();
    }
  }
}
