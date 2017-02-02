package de.enlightened.peris;

import android.view.View;

import java.util.ArrayList;

@SuppressWarnings("checkstyle:visibilitymodifier")
public class Post {
  public String tagline = "tagline";
  public String author = "Author";
  public String body = "Post body goes here!";
  public String avatar = "n/a";
  public String id = "0";
  public String categoryId = "0";
  public String subforumId = "0";
  public String threadId = "0";
  public String authorId = "0";
  public String timestamp = "00-00-0000";
  public String color = "#000000";
  public String authorLevel = "0";
  public String picture = "0";
  public String parent = "0";
  public View subforumSeperator;
  public String categoryModerator = "0";
  public String attachmentExtension = "jpg";
  public boolean userOnline = false;
  public boolean userBanned = false;
  public boolean canBan = false;
  public boolean canDelete = false;
  public boolean canEdit = false;
  public boolean canThank = false;
  public boolean canLike = false;
  public int thanksCount = 0;
  public int likeCount = 0;
  public final ArrayList<PostAttachment> attachmentList = new ArrayList<PostAttachment>();
}
