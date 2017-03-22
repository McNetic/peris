/*
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

package de.enlightened.peris.site;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import de.enlightened.peris.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Topic extends TopicItem implements Serializable {
  private String id;
  private String title;
  private Type type;
  //private String obscureType = "C";
  private String subforumId;
  private Date lastUpdate;
  private String authorId;
  private String forumName;
  private String authorName;
  private int postCount = 1;
  private int replyCount = 0;
  private int viewCount = 0;
  private boolean canPost = true;
  private boolean hasNewPosts;
  private boolean isClosed = false;
  private String authorIcon;
  private boolean canStick = false;
  private boolean canSubscribe = true;
  private boolean canDelete = false;
  private boolean canClose = false;
  private final ArrayList<Post> posts = new ArrayList<Post>();

  @Override
  public String getHeading() {
    return this.title;
  }

  @Override
  public boolean hasNewItems() {
    return this.hasNewPosts;
  }

  public void addPost(final Post post) {
    this.posts.add(post);
  }

  public enum Type {
    Default,
    Announcement,
    Sticky
  }
}
