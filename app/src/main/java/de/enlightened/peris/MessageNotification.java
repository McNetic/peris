package de.enlightened.peris;

import de.enlightened.peris.db.DBEntity;

/**
 * Created by Nicolai Ehemann on 11.02.2017.
 */

public class MessageNotification extends DBEntity {

  private final long idServer;
  private final int messageId;

  public MessageNotification(final long idServer, final int messageId) {
    this.idServer = idServer;
    this.messageId = messageId;
  }

  public long getIdServer() {
    return this.idServer;
  }

  public int getMessageId() {
    return this.messageId;
  }
}
