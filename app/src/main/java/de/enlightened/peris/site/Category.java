package de.enlightened.peris.site;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Copyright (C) 2017 Nicolai Ehemann
 * <p>
 * This file is part of Peris.
 * <p>
 * Peris is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Peris is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Peris.  If not, see <http://www.gnu.org/licenses/>.
 */

@Getter
@Setter
@Builder
public class Category extends TopicItem implements Serializable {

  public static final String ROOT_ID = "---ROOT_ID---"; //"\u0001\u0002ROOT_ID\u0001\u0002";

  private String id;
  private String name;
  private String parentId;
  private String hash;
  //private String obscureType = "S";
  private String logoUrl;
  private String url;
  private boolean isSubscribed = false;
  private boolean canSubscribe = false;
  private boolean hasNewTopic = false;
  private boolean subOnly = false;
  private List<Category> children = new ArrayList<>();

  @Override
  public String getHeading() {
    return this.name;
  }

  @Override
  public boolean hasNewItems() {
    return this.hasNewTopic;
  }
}
