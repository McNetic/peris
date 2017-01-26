package de.enlightened.peris;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("checkstyle:returncount")
public class BBCodeParser {

  private static final Map<Pattern, Integer> EMOTICONS = new HashMap<Pattern, Integer>();

  private static final int JPEG_COMPRESSION_QUALITY = 80;
  private static final OnClickListener PIC_CLICK_LISTENER = new OnClickListener() {

    @Override
    public void onClick(final View v) {
      final PopupMenu popup = new PopupMenu(v.getContext(), v);
      final MenuInflater inflater = popup.getMenuInflater();
      inflater.inflate(R.menu.menu_post_picture, popup.getMenu());
      popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(final MenuItem item) {
          if (item.getItemId() == R.id.menu_post_picture_save) {
            final Bitmap bitmap = ((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
            final File saveDirectory = new File(Environment.getExternalStorageDirectory(), v.getContext().getString(R.string.app_name));

            if (!saveDirectory.exists() && !saveDirectory.mkdirs()) {
              Log.d("Peris", "failed to create directory");
              return false;
            } else {
              final Date data = new Date();
              final long timestamp = data.getTime();
              final String cacheName = timestamp + ".jpg";

              final File file = new File(saveDirectory.getPath() + File.separator + cacheName);

              try {
                final OutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESSION_QUALITY, os);
                os.close();
              } catch (Exception ex) {
                return false;
              }

              Toast.makeText(v.getContext(), "Picture saved to SD Card!", Toast.LENGTH_SHORT).show();

              MediaScannerConnection.scanFile(v.getContext(), new String[]{file.getPath()}, null, null);

              //ApeImageCacher.DownloadImageToSDCard(tag, , v.getContext());
            }
            return true;
          } else {
            return false;
          }
        }

      });
      popup.show();
    }

  };

  static {
    //addPattern(EMOTICONS, ":)", R.drawable.emo_im_happy);
    addPattern(EMOTICONS, ":-)", R.drawable.emo_im_happy);
    //addPattern(EMOTICONS, ";)", R.drawable.emo_im_winking);
    addPattern(EMOTICONS, ";-)", R.drawable.emo_im_winking);
    //addPattern(EMOTICONS, ":(", R.drawable.emo_im_sad);
    addPattern(EMOTICONS, ":-(", R.drawable.emo_im_sad);
    //addPattern(EMOTICONS, ";(", R.drawable.emo_im_crying);
    addPattern(EMOTICONS, ";-(", R.drawable.emo_im_crying);
    addPattern(EMOTICONS, ":'(", R.drawable.emo_im_crying);
    addPattern(EMOTICONS, ":'-(", R.drawable.emo_im_crying);
    //addPattern(EMOTICONS, ":/", R.drawable.emo_im_undecided);
    addPattern(EMOTICONS, ":-/", R.drawable.emo_im_undecided);
    //addPattern(EMOTICONS, ":\\", R.drawable.emo_im_undecided);
    addPattern(EMOTICONS, ":-\\", R.drawable.emo_im_undecided);
    //addPattern(EMOTICONS, "O:)", R.drawable.emo_im_angel);
    addPattern(EMOTICONS, "O:-)", R.drawable.emo_im_angel);
    //addPattern(EMOTICONS, "0:)", R.drawable.emo_im_angel);
    addPattern(EMOTICONS, "0:-)", R.drawable.emo_im_angel);
    //addPattern(EMOTICONS, "B)", R.drawable.emo_im_cool);
    addPattern(EMOTICONS, "B-)", R.drawable.emo_im_cool);
    //addPattern(EMOTICONS, ":[", R.drawable.emo_im_embarrassed);
    addPattern(EMOTICONS, ":-[", R.drawable.emo_im_embarrassed);
    //addPattern(EMOTICONS, ":!", R.drawable.emo_im_foot_in_mouth);
    addPattern(EMOTICONS, ":-!", R.drawable.emo_im_foot_in_mouth);
    //addPattern(EMOTICONS, ":*", R.drawable.emo_im_kissing);
    addPattern(EMOTICONS, ":-*", R.drawable.emo_im_kissing);
    //addPattern(EMOTICONS, ":D", R.drawable.emo_im_laughing);
    addPattern(EMOTICONS, ":-D", R.drawable.emo_im_laughing);
    //addPattern(EMOTICONS, ":X", R.drawable.emo_im_lips_are_sealed);
    addPattern(EMOTICONS, ":-X", R.drawable.emo_im_lips_are_sealed);
    //addPattern(EMOTICONS, ":$", R.drawable.emo_im_money_mouth);
    addPattern(EMOTICONS, ":-$", R.drawable.emo_im_money_mouth);
    //addPattern(EMOTICONS, ":O", R.drawable.emo_im_yelling);
    addPattern(EMOTICONS, ":-O", R.drawable.emo_im_surprised);
    //addPattern(EMOTICONS, ":0", R.drawable.emo_im_surprised);
    addPattern(EMOTICONS, ":-0", R.drawable.emo_im_surprised);
    //addPattern(EMOTICONS, ":P", R.drawable.emo_im_tongue_sticking_out);
    addPattern(EMOTICONS, ":-P", R.drawable.emo_im_tongue_sticking_out);
    addPattern(EMOTICONS, "o.O", R.drawable.emo_im_wtf);
  }

  protected BBCodeParser() {
  }

  @SuppressWarnings("checkstyle:parameternumber")
  public static final void parseCode(
      final Context c,
      final LinearLayout layout,
      final String text,
      final Typeface opensans,
      final boolean useOpenSans,
      final boolean useShading,
      final ArrayList<PostAttachment> attachmentList,
      final float fontSize,
      final boolean useMovementMethod,
      final String textColor,
      final PerisApp app) {

    layout.removeAllViews();

    String postContent = text;

    postContent = postContent.replace("[img]", " [img]");
    postContent = postContent.replace("[/img]", "[/img] ");
    postContent = postContent.replace("[IMG]", " [img]");
    postContent = postContent.replace("[/IMG]", "[/img] ");

    final String[] postWords = postContent.split(" ");

    String currentSection = "";

    for (String s : postWords) {

      if (s.contains("[img]") && s.contains("[/img]")) {

        insertTextSection(c, currentSection, layout, opensans, useOpenSans, useShading, fontSize, useMovementMethod, textColor, app);

        currentSection = "";

        final ImageView ivNewPic = new ImageView(c);
        final String inlineImageUrl = s.replace("[img]", "").replace("[/img]", "").replace("/>", "").replace("<br", "");
        layout.addView(ivNewPic);
        ivNewPic.setTag(inlineImageUrl);
        ivNewPic.setOnClickListener(PIC_CLICK_LISTENER);

        if (inlineImageUrl.contains(".php")) {
          ApeImageCacher.downloadImage(inlineImageUrl, ivNewPic, app, c);
        } else {
          ImageLoader.getInstance().displayImage(inlineImageUrl, ivNewPic);
        }


      } else {
        currentSection = currentSection + s + " ";
      }

    }

    final TextView tbPart = new TextView(c);
    tbPart.setTextColor(Color.parseColor(textColor));

    if (app.getSession().getServer().serverColor.contains("#")) {
      tbPart.setLinkTextColor(Color.parseColor(app.getSession().getServer().serverColor));
    }

    insertTextSection(c, currentSection, layout, opensans, useOpenSans, useShading, fontSize, useMovementMethod, textColor, app);

    if (attachmentList != null) {

      for (PostAttachment pa : attachmentList) {

        if (pa.content_type.contentEquals("image") || pa.content_type.contains("ImageUploadedByTapatalk")) {
          final ImageView ivNewPic = new ImageView(c);
          final String inlineImageUrl = pa.url;
          layout.addView(ivNewPic);
          ivNewPic.setTag(inlineImageUrl);
          ivNewPic.setOnClickListener(PIC_CLICK_LISTENER);

          if (inlineImageUrl.contains(".php")) {
            ApeImageCacher.downloadImage(inlineImageUrl, ivNewPic, app, c);
          } else {
            ImageLoader.getInstance().displayImage(inlineImageUrl, ivNewPic);
          }
        } else {
          final String htmlString = "<b>Attachment: </b><a href=\"" + pa.url + "\">" + pa.filename + "</a>";
          insertTextSection(c, htmlString, layout, opensans, useOpenSans, useShading, fontSize, useMovementMethod, textColor, app);
        }


      }
    }

    if (useShading) {
      tbPart.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
    }

  }

  private static String fixPost(final String oldPost) {
    String fixedPost = oldPost;

    fixedPost = fixedPost.replace("[code]", "<blockquote><font face=\"monospace\">");
    fixedPost = fixedPost.replace("[/code]", "</font></blockquote>");
    fixedPost = fixedPost.replace("[CODE]", "<blockquote><font face=\"monospace\">");
    fixedPost = fixedPost.replace("[/CODE]", "</font></blockquote>");
    fixedPost = fixedPost.replace("[b]", "<b>");
    fixedPost = fixedPost.replace("[/b]", "</b>");
    fixedPost = fixedPost.replace("[i]", "<i>");
    fixedPost = fixedPost.replace("[/i]", "</i>");
    fixedPost = fixedPost.replace("[u]", "<u>");
    fixedPost = fixedPost.replace("[/u]", "</u>");
    fixedPost = fixedPost.replace("[/color]", "</font>");
    fixedPost = fixedPost.replaceAll("\\[color=(.*?)\\]", "<font color=\"$1\">");
    fixedPost = fixedPost.replace("[quote]", "<blockquote>");
    fixedPost = fixedPost.replace("[/quote]", "</blockquote>");
    fixedPost = fixedPost.replace("[QUOTE]", "<blockquote>");
    fixedPost = fixedPost.replace("[/QUOTE]", "</blockquote>");
    fixedPost = fixedPost.replaceAll("\\[quote=\"(.*?)\"\\]", "<blockquote><b>$1</b> wrote:<br /><br /><br />");
    fixedPost = fixedPost.replaceAll("\\[quote uid=(.*?) name=\"(.*?)\"(.*?)\\]", "<blockquote><b>$2</b> wrote:<br /><br />");
    fixedPost = fixedPost.replaceAll("\\[quote name=\"(.*?)\"(.*?)\\]", "<blockquote><b>$1</b> wrote:<br /><br />");
    fixedPost = fixedPost.replaceAll("\\[quote(.*?)\\]", "<blockquote>");
    fixedPost = fixedPost.replaceAll("\\[QUOTE=\"(.*?)\"\\]", "<blockquote><b>$1</b> wrote:<br /><br /><br />");
    fixedPost = fixedPost.replaceAll("\\[QUOTE(.*?)\\]", "<blockquote>");

    fixedPost = fixedPost.replace("%40", "@");
    fixedPost = fixedPost.replace("<", " <");
    fixedPost = fixedPost.replace(">", "> ");

    fixedPost = fixedPost.replaceAll("\\[url=\"(.*?)\"\\](.*?)\\[/url\\]", "<a href=\"$1\">$2</a>");
    fixedPost = fixedPost.replaceAll("\\[URL=\"(.*?)\"\\](.*?)\\[/URL\\]", "<a href=\"$1\">$2</a>");
    fixedPost = fixedPost.replaceAll("\\[url=(.*?)\\](.*?)\\[/url\\]", "<a href=\"$1\">$2</a>");
    fixedPost = fixedPost.replaceAll("\\[URL=(.*?)\\](.*?)\\[/URL\\]", "<a href=\"$1\">$2</a>");
    fixedPost = fixedPost.replaceAll("\\[url=(.*?)\\](.*?)\\[/url\\]", "$1");
    fixedPost = fixedPost.replaceAll("\\[URL=(.*?)\\](.*?)\\[/URL\\]", "$1");
    fixedPost = fixedPost.replaceAll("\\[url\\](.*?)\\[/url\\]", "$1");
    fixedPost = fixedPost.replaceAll("\\[URL\\](.*?)\\[/URL\\]", "$1");

    //Get all other hyperlinks hopefully
    fixedPost = fixedPost.replaceAll("(?<![=\"\\/>])http(s)?://([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\!\\@\\#\\$\\%\\^\\&amp;\\*\\(\\)_\\-\\=\\+\\\\\\/\\?\\.\\:\\;\\'\\,]*)?", "<a href=\"$0\">$0</a>");

    //Axe rogue url tags
    fixedPost = fixedPost.replaceAll("\\[url=\"(.*?)\"\\]", "<a href=\"$1\">$1</a>");
    fixedPost = fixedPost.replaceAll("\\[url=(.*?)\\]", "<a href=\"$1\">$1</a>");
    fixedPost = fixedPost.replaceAll("\\[/url\\]", "");
    fixedPost = fixedPost.replaceAll("\\[URL=\"(.*?)\"\\]", "<a href=\"$1\">$1</a>");
    fixedPost = fixedPost.replaceAll("\\[URL=(.*?)\\]", "<a href=\"$1\">$1</a>");
    fixedPost = fixedPost.replaceAll("\\[/URL\\]", "");

    return fixedPost;
  }

  @SuppressWarnings("checkstyle:parameternumber")
  private static void insertTextSection(
      final Context c,
      final String text,
      final LinearLayout llPostBodyHolder,
      final Typeface opensans,
      final boolean useOpenSans,
      final boolean useShading,
      final float fontSize,
      final boolean useMovementMethod,
      final String textColor,
      final PerisApp app) {
    final TextView tbPart = new TextView(c);
    tbPart.setTextColor(Color.parseColor(textColor));

    if (app.getSession().getServer().serverColor.contains("#")) {
      tbPart.setLinkTextColor(Color.parseColor(app.getSession().getServer().serverColor));
    }

    if (useOpenSans) {
      tbPart.setTypeface(opensans);
    }

    if (useMovementMethod) {
      tbPart.setMovementMethod(LinkMovementMethod.getInstance());
    }

    tbPart.setText(getSmiledText(c, fixPost(text)));

    tbPart.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
    llPostBodyHolder.addView(tbPart);

    if (useShading) {
      tbPart.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
    }
  }

  private static void addPattern(final Map<Pattern, Integer> map, final String smile, final int resource) {
    map.put(Pattern.compile(Pattern.quote(smile)), resource);
  }

  public static boolean addSmiles(final Context context, final Spannable spannable) {
    boolean hasChanges = false;
    for (Entry<Pattern, Integer> entry : EMOTICONS.entrySet()) {
      final Matcher matcher = entry.getKey().matcher(spannable);
      while (matcher.find()) {
        boolean set = true;
        for (ImageSpan span : spannable.getSpans(matcher.start(),
            matcher.end(), ImageSpan.class)) {
          if (spannable.getSpanStart(span) >= matcher.start()
              && spannable.getSpanEnd(span) <= matcher.end()) {
            spannable.removeSpan(span);
          } else {
            set = false;
            break;
          }
        }
        if (set) {
          hasChanges = true;
          spannable.setSpan(new ImageSpan(context, entry.getValue()),
              matcher.start(), matcher.end(),
              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
    }
    return hasChanges;
  }

  public static Spannable getSmiledText(final Context context, final String text) {
    final Spannable spannable = Spannable.Factory.getInstance().newSpannable(Html.fromHtml(text));
    addSmiles(context, spannable);
    return spannable;
  }
}
