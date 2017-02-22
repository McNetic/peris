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

public class Chat {
  private String postid;
  private String chatid;
  private String timestamp;
  private String displayname;
  private String displayavatar;
  private String postbody;
  private String displaycolor;

  public String getPostid() {
    return this.postid;
  }

  public void setPostid(final String value) {
    this.postid = value;
  }

  public String getChatid() {
    return this.chatid;
  }

  public void setChatid(final String value) {
    this.chatid = value;
  }

  public String getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(final String value) {
    this.timestamp = value;
  }

  public String getDisplayname() {
    return this.displayname;
  }

  public void setDisplayname(final String value) {
    this.displayname = value;
  }

  public String getDisplayavatar() {
    return this.displayavatar;
  }

  public void setDisplayavatar(final String value) {
    this.displayavatar = value;
  }

  public String getPostbody() {
    return this.postbody;
  }

  public void setPostbody(final String value) {
    this.postbody = value;
  }

  public String getDisplaycolor() {
    return this.displaycolor;
  }

  public void setDisplaycolor(final String value) {
    this.displaycolor = value;
  }
}
