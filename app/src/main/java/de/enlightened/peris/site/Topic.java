package de.enlightened.peris.site;

import java.util.ArrayList;

import de.enlightened.peris.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Nicolai Ehemann on 14.02.2017.
 */
@Getter
@Setter
@Builder
public class Topic {
  private int curTotalPosts;
  private boolean canPost;
  private final ArrayList<Post> posts = new ArrayList<Post>();

  public void addPost(final Post post) {
    this.posts.add(post);
  }
}
