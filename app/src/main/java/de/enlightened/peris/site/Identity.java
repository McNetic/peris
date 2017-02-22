package de.enlightened.peris.site;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by Nicolai Ehemann on 19.02.2017.
 */
@Getter
@Builder
public class Identity {
  private final String id;
  private final String avatarUrl;
  private final int postCount;
  private final boolean profileAccess;
}
