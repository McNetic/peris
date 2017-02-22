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
