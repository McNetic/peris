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
