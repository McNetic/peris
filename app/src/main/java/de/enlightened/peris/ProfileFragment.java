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
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class ProfileFragment extends Fragment {

  private static final String TAG = ProfileFragment.class.getName();
  private static final double DESIRED_PIC_SIZE = 800;

  private static final int CAMERA_PIC_REQUEST = 1337;
  private static final int GALLERY_PIC_REQUEST = 1338;
  private static final int THUMBNAIL_SIZE = 100;
  private static final int MAX_ITEM_COUNT = 50;
  private static final int PHOTO_PADDING = 12;
  private String categoryId;
  private TextView tvCreated;
  private TextView tvPostCount;
  private TextView tvActivity;
  private TextView tvTagline;
  private TextView tvAbout;
  private ImageView ivProfilePic;
  private String userName;

  private FragmentActivity activity;
  private PerisApp application;
  private String uploadPicPath;
  private Bitmap uploadPic;

  @Override
  public void onCreate(final Bundle bundle) {
    super.onCreate(bundle);

    this.activity = (FragmentActivity) getActivity();
    this.application = (PerisApp) this.activity.getApplication();

    this.setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.view_edit_profile, container, false);
  }

  @Override
  public void onStart() {
    super.onStart();

    final Bundle bundle = getArguments();
    this.categoryId = bundle.getString("userid");
    this.userName = bundle.getString("username");
    this.setupElements();

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      new DownloadProfile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      new DownloadProfile().execute();
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    this.activity.getActionBar().setTitle(this.userName);
  }

  private void setupElements() {
    this.tvCreated = (TextView) this.activity.findViewById(R.id.profileCreated);
    this.tvPostCount = (TextView) this.activity.findViewById(R.id.profilePostCount);
    this.tvActivity = (TextView) this.activity.findViewById(R.id.profileLastActivity);
    this.tvTagline = (TextView) this.activity.findViewById(R.id.profileTagline);
    this.tvAbout = (TextView) this.activity.findViewById(R.id.profileAbout);
    this.ivProfilePic = (ImageView) this.activity.findViewById(R.id.profilePicture);

    final String userid = this.application.getSession().getServer().serverUserId;
    final LinearLayout avatarButtons = (LinearLayout) this.activity.findViewById(R.id.profile_avatar_editor_buttons);

    if (this.categoryId == null) {
      avatarButtons.setVisibility(View.GONE);
    } else {
      if (!userid.contentEquals(this.categoryId)) {
        avatarButtons.setVisibility(View.GONE);
      }
    }

    final Button btnPicFromCamera = (Button) this.activity.findViewById(R.id.profile_upload_avatar_camera);
    final Button btnPicFromGallery = (Button) this.activity.findViewById(R.id.profile_upload_avatar_gallery);
    if (!this.canHandleCameraIntent()) {
      btnPicFromCamera.setVisibility(View.GONE);
    }
    btnPicFromGallery.setOnClickListener(new View.OnClickListener() {
      public void onClick(final View v) {
        final Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_PIC_REQUEST);
      }
    });

    btnPicFromCamera.setOnClickListener(new View.OnClickListener() {
      public void onClick(final View v) {
        final Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final File imagesFolder = new File(Environment.getExternalStorageDirectory(), "temp");
        imagesFolder.mkdirs();
        final File image = new File(imagesFolder, "temp.jpg");
        final Uri uriSavedImage = Uri.fromFile(image);
        imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
        startActivityForResult(imageIntent, CAMERA_PIC_REQUEST);
      }
    });

  }

  @SuppressLint("NewApi")
  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    inflater.inflate(R.menu.profile_menu, menu);
    if (ThemeSetter.getForegroundDark(this.application.getSession().getServer().serverColor)) {
      final MenuItem item = menu.findItem(R.id.profile_menu_message);
      item.setIcon(R.drawable.ic_action_new_email_dark);
    }
    if (this.userName == null || this.userName.contentEquals(this.application.getSession().getServer().serverUserName)) {
      final MenuItem msgitem = menu.findItem(R.id.profile_menu_message);
      msgitem.setVisible(false);
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(final Menu menu) {
    super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.profile_menu_message:
        this.sendMessage();
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  private void sendMessage() {
    final Intent myIntent = new Intent(this.activity, NewPost.class);
    final Bundle bundle = new Bundle();
    bundle.putString("postid", (String) "0");
    bundle.putString("parent", (String) "0");
    bundle.putString("category", this.userName);
    bundle.putString("subforum_id", (String) "0");
    bundle.putString("original_text", (String) "");
    bundle.putString("boxTitle", (String) "Message " + this.userName);
    bundle.putString("picture", (String) "0");
    bundle.putString("color", (String) getString(R.string.default_color));
    bundle.putString("subject", (String) "");
    bundle.putString("post_type", NewPost.Type.Message.name());
    myIntent.putExtras(bundle);

    this.startActivity(myIntent);
  }

  public void finishUpSubmission() {
    this.application.getSession().setSessionListener(new Session.SessionListener() {

      @Override
      public void onSessionConnectionFailed(final String reason) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          new DownloadProfile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
          new DownloadProfile().execute();
        }
      }

      @Override
      public void onSessionConnected() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          new DownloadProfile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
          new DownloadProfile().execute();
        }
      }
    });
    this.application.getSession().refreshLogin();
  }

  @Override
  public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == CAMERA_PIC_REQUEST) {
        try {
          final File imagesFolder = new File(Environment.getExternalStorageDirectory(), "temp");
          imagesFolder.mkdirs();
          final File image = new File(imagesFolder, "temp.jpg");

          this.uploadPicPath = image.getPath();
          BitmapFactory.Options options = new BitmapFactory.Options();
          options.inJustDecodeBounds = true;
          final int imageWidth = options.outWidth;
          options = new BitmapFactory.Options();

          if (imageWidth > (DESIRED_PIC_SIZE * 8)) {
            options.inSampleSize = 8;
          } else if (imageWidth > (DESIRED_PIC_SIZE * 4)) {
            options.inSampleSize = 4;
          } else if (imageWidth > (DESIRED_PIC_SIZE * 2)) {
            options.inSampleSize = 2;
          } else {
            options.inSampleSize = 1;
          }

          Bitmap thumbnail2 = BitmapFactory.decodeFile(this.uploadPicPath, options);
          thumbnail2 = Bitmap.createScaledBitmap(thumbnail2, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);
          this.uploadPic = thumbnail2;

          Log.d(TAG, "Avatar Size: " + this.uploadPic.getWidth() + "x" + this.uploadPic.getHeight());

          this.submitpic();
        } catch (Exception e) {
          final Toast toast = Toast.makeText(this.activity, "Error loading image!" + e.getMessage(), Toast.LENGTH_LONG);
          toast.show();
          return;
        }
      } else {
        if (requestCode == GALLERY_PIC_REQUEST) {
          try {
            final Uri currImageURI;
            currImageURI = data.getData();
            final String[] proj = {MediaStore.Images.Media.DATA};
            final Cursor cursor = this.activity.managedQuery(currImageURI,
                proj, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
            final int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            this.uploadPicPath = cursor.getString(columnIndex);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            final int imageWidth = options.outWidth;
            options = new BitmapFactory.Options();

            if (imageWidth > DESIRED_PIC_SIZE * 8) {
              options.inSampleSize = 8;
            } else if (imageWidth > DESIRED_PIC_SIZE * 4) {
              options.inSampleSize = 4;
            } else if (imageWidth > DESIRED_PIC_SIZE * 2) {
              options.inSampleSize = 2;
            } else {
              options.inSampleSize = 1;
            }
            Bitmap thumbnail2 = BitmapFactory.decodeFile(this.uploadPicPath, options);
            thumbnail2 = Bitmap.createScaledBitmap(thumbnail2, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);
            this.uploadPic = thumbnail2;

            Log.d(TAG, "Avatar Size: " + this.uploadPic.getWidth() + "x" + this.uploadPic.getHeight());

            this.submitpic();
          } catch (Exception e) {
            final Toast toast = Toast.makeText(this.activity, "Can only upload locally stored content!", Toast.LENGTH_LONG);
            toast.show();
            return;
          }
        }
      }
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private void submitpic() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      new UploadImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      new UploadImageTask().execute();
    }
  }

  protected boolean canHandleCameraIntent() {
    final Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    final List<ResolveInfo> results = this.activity.getPackageManager().queryIntentActivities(intent, 0);
    return results.size() > 0;
  }

  private class DownloadProfile extends AsyncTask<String, Void, Object[]> {
    @SuppressWarnings({"rawtypes", "unchecked", "checkstyle:requirethis"})
    @Override
    protected Object[] doInBackground(final String... params) {

      if (activity != null) {
        try {
          final Object[] result = new Object[MAX_ITEM_COUNT];
          Log.d(TAG, "Viewing profile of " + userName);

          final Vector paramz = new Vector();
          paramz.addElement(userName.getBytes());
          if (categoryId != null) {
            paramz.addElement(categoryId);
            Log.i(TAG, "Loading profile for " + userName + " (" + categoryId + ")");
          } else {
            Log.i(TAG, "Loading profile for " + userName + " (null)");
          }
          result[0] = application.getSession().performSynchronousCall("get_user_info", paramz);
          return result;
        } catch (Exception e) {
          Log.w(TAG, e.getMessage());
        }
      }
      return null;
    }

    @SuppressWarnings({"rawtypes", "checkstyle:requirethis"})
    protected void onPostExecute(final Object[] result) {
      if (result == null || result[0] == null) {
        Log.e(TAG, "No response for profile!");
        if (result != null) {
          Log.e(TAG, Integer.toString(result.length));
        }
        return;
      }
      final HashMap topicMap = (HashMap) result[0];

      if (topicMap == null) {
        Log.e(TAG, "No topicmap!");
        Log.e(TAG, result[0].toString());
        return;
      } else {
        Log.i(TAG, result[0].toString());
      }

      Date timestamp = null;

      if (topicMap.containsKey("reg_time")) {
        timestamp = (Date) topicMap.get("reg_time");
      }

      final Date lastactive = (Date) topicMap.get("last_activity_time");

      if (timestamp != null) {
        tvCreated.setText("Member Since: " + timestamp.toString());
      }

      if (topicMap.containsKey("post_count")) {
        tvPostCount.setText("Post Count: " + Integer.toString((Integer) topicMap.get("post_count")));
      } else {
        tvPostCount.setVisibility(View.GONE);
      }

      if (lastactive == null) {
        tvActivity.setVisibility(View.GONE);
      } else {
        tvActivity.setText("Last Activity: " + lastactive.toString());
      }

      if (topicMap.get("current_activity") != null) {
        tvTagline.setText(new String((byte[]) topicMap.get("current_activity")));
      }

      if (topicMap.containsKey("icon_url")) {
        if (((String) topicMap.get("icon_url")).contains("http://")) {
          ImageLoader.getInstance().displayImage((String) topicMap.get("icon_url"), ivProfilePic);
        }
      }

      final Object[] fieldsMap = (Object[]) topicMap.get("custom_fields_list");
      String aboutSection = "";

      if (fieldsMap != null) {
        for (Object t : fieldsMap) {
          final HashMap m = (HashMap) t;
          final String tName = new String((byte[]) m.get("name"));
          final String tValue = new String((byte[]) m.get("value"));
          aboutSection = aboutSection + "<b>" + tName + ":</b> " + tValue + "<br /><br />";
        }

        tvAbout.setText(Html.fromHtml(aboutSection));
        Linkify.addLinks(tvAbout, Linkify.ALL);
      }
    }
  }

  //This background thread class uploads an image to the server.
  @SuppressWarnings("checkstyle:requirethis")
  private class UploadImageTask extends AsyncTask<String, Void, String> {
    private Dialog errorBox;

    //This method is performed before the thread is executed.
    protected void onPreExecute() {
      this.errorBox = new Dialog(activity);
      this.errorBox.setTitle("Uploading...");
      this.errorBox.show();

      final TextView errorMessage = new TextView(activity);
      errorMessage.setPadding(PHOTO_PADDING, PHOTO_PADDING, PHOTO_PADDING, PHOTO_PADDING);
      errorMessage.setText("Uploading photo, please wait...");
      this.errorBox.setContentView(errorMessage);
    }

    //This method that is done in the background thread.
    protected String doInBackground(final String... args) {
      final URL uploadURL = application.getSession().getServer().getUploadURL();
      final String result = new AvatarUploader().uploadBitmap(activity, uploadURL, uploadPic, application);

      return result;
    }

    //This method is executed after the thread has completed.
    protected void onPostExecute(final String result) {
      try {
        this.errorBox.dismiss();
      } catch (Exception e) {
        Log.d(TAG, e.getMessage());
      }
      if (result == null) {
        return;
      }

      if (result.contentEquals("fail")) {
        Log.w(TAG, "Image upload failure");
      } else {
        finishUpSubmission();
      }
      return;
    }
  }
}
