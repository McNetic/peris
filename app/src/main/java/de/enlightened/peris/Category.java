package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.view.View;

import java.util.ArrayList;

@SuppressLint("NewApi")
@SuppressWarnings("checkstyle:visibilitymodifier")
public class Category {
  public String description = "Category Description";
  public String name = "Category Name";
  public String id = "0";
  public String subforumId = "0";
  public String lastUpdate = null;
  public String lastThread = "Thread Name";
  public String threadCount = "0";
  public String viewCount = "0";
  public View subforumSeperator;
  public String moderator;
  public String color = "#000000";
  public String icon = "n/a";
  public String mature = "N";
  public String type = "C";
  public String onUnified = "Y";
  public boolean canSticky = false;
  public boolean canLock = false;
  public boolean canDelete = false;
  public boolean canSubscribe = false;
  public boolean isSubscribed = false;
  public boolean isLocked = false;
  public String url = "n/a";
  public boolean hasNewTopic = false;
  public boolean hasChildren = false;
  public String topicSticky = "N";
  public ArrayList<Category> children;
}
