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

package de.enlightened.peris.api;

import android.util.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.enlightened.peris.InboxItem;
import de.enlightened.peris.Post;
import de.enlightened.peris.PostAttachment;
import de.enlightened.peris.Server;
import de.enlightened.peris.site.Config;
import de.enlightened.peris.site.Identity;
import de.enlightened.peris.site.MessageBox;
import de.enlightened.peris.site.MessageFolder;
import de.enlightened.peris.site.Topic;
import de.enlightened.peris.support.RPCMap;
import de.enlightened.peris.support.XMLRPCCall;
import de.timroes.axmlrpc.XMLRPCClient;

public class Tapatalk {

  private static final String TAG = Tapatalk.class.getName();

  private XMLRPCClient xmlRPCClient;
  private Server server;
  private SSLContext sc;
  private Config serverConfig;
  private Identity identity;

  public void setServer(final Server server) {
    this.server = server;
  }

  private XMLRPCClient getXMLRPCClient() {
    if (this.xmlRPCClient == null) {
      try {
        if (this.server.serverHttps) {
          this.sc = SSLContext.getInstance("SSL");
          this.sc.init(null, this.trustAllCerts, new SecureRandom());
          HttpsURLConnection.setDefaultSSLSocketFactory(this.sc.getSocketFactory());
          HttpsURLConnection.setDefaultHostnameVerifier(this.hostnameVerifier);
        }
        this.xmlRPCClient = new XMLRPCClient(this.server.getTapatalkURL(), XMLRPCClient.FLAGS_ENABLE_COOKIES);
      } catch (NoSuchAlgorithmException | KeyManagementException ex) {
        String.format("Tapatalk call error (SSL initialization): %s", ex.getClass().getName());
        if (ex.getMessage() != null) {
          Log.e(TAG, ex.getMessage());
        } else {
          Log.e(TAG, "(no message available)");
        }
      }
    }
    return this.xmlRPCClient;
  }

  private final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
    public boolean verify(final String hostname, final SSLSession session) {
      return true;
    }
  };

  private final TrustManager[] trustAllCerts = new TrustManager[] {
      new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
          return null;
        }
        public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
          // Trust always
        }
        public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
          // Trust always
        }
      },
  };

  private XMLRPCCall xmlrpc(final String method) {
    return new XMLRPCCall(this.getXMLRPCClient(), method);
  }

  public Config getConfig() {
    if (this.serverConfig == null) {
      final RPCMap configMap = this.xmlrpc("get_config").call();
      if (configMap != null) {
        this.serverConfig = Config.builder()
            .serverPluginVersion(configMap.getString("version"))
            .accountManagementEnabled(configMap.getStringBoolOrDefault("inappreg"))
            .build();
        Log.i(TAG, String.format("Forum is %s", this.serverConfig.getForumSystem()));
      }
    }
    return this.serverConfig;
  }

  public Identity getIdentity() {
    return this.identity;
  }

  public ApiResult login(final String username, final String password) {
    final RPCMap loginMap = this.xmlrpc("login")
        .param(username.getBytes())
        .param(password.getBytes())
        .call();
    final ApiResult loginResult;
    if (loginMap != null) {
      loginResult = ApiResult.builder()
          .success(loginMap.getBool("result"))
          .message(loginMap.getByteStringOrDefault("result_text", "wrong username or password"))
          .build();
      if (loginResult.isSuccess()) {
        this.identity = Identity.builder()
            .id(loginMap.getString("user_id"))
            .avatarUrl(loginMap.getString("icon_url"))
            .postCount(loginMap.getIntOrDefault("post_count", 0))
            .profileAccess(loginMap.getBoolOrDefault("can_profile"))
            .build();
      }
    } else {
      loginResult = ApiResult.builder()
        .success(false)
        .message("unknown error")
        .build();
    }
    return loginResult;
  }

  public Topic getTopic(final String topicId, final int startNum, final int lastNum, final boolean returnHtml) {
    final RPCMap topicMap = this.xmlrpc("get_thread")
        .param(topicId)
        .param(startNum)
        .param(lastNum)
        .param(true)
        .call();
    final Topic topic = Topic.builder()
        .curTotalPosts(topicMap.getIntOrDefault("total_post_num", 0))
        .canPost(topicMap.getBoolOrDefault("can_reply", false))
        .build();

    for (final RPCMap postMap : topicMap.getRPCMapArray("posts")) {
      final Date timestamp = postMap.getDate("post_time");
      final Post po = new Post();
      /* TODO: Needed?
      po.categoryId = categoryId;
      po.subforumId = subforumId;
      po.threadId = threadId;
      */
      //po.moderator = moderator;

      if (!postMap.containsKey("post_author_id")) {
        Log.w(TAG, "There is no author id with this post!");
      }
      po.author = postMap.getByteString("post_author_name");
      po.authorId = postMap.getString("post_author_id");
      po.body = postMap.getByteString("post_content");
      po.avatar = postMap.getString("icon_url");
      po.id = postMap.getString("post_id");
      po.tagline = "tagline";

      if (timestamp != null) {
        po.timestamp = timestamp.toString();
      }

      for (final RPCMap attachmentMap : postMap.getRPCMapArray("attachements")) {
        final String attachmentType = attachmentMap.getString("content_type");
        final String attachmentUrl = attachmentMap.getString("url");
        final String attachmentName = attachmentMap.getByteStringOrDefault("filename", null);
        if (attachmentType != null) {
          Log.i(TAG, "Post has attachment of type: " + attachmentType);
        }
        if (attachmentUrl != null) {
          Log.i(TAG, "Post has attachment of url: " + attachmentUrl);
        }
        if (attachmentName != null) {
          Log.i(TAG, "Post has attachment of type: " + attachmentName);
        }
        if (attachmentType != null && attachmentUrl != null && attachmentName != null) {
          final PostAttachment pa = new PostAttachment();
          pa.contentType = attachmentType;
          pa.url = attachmentUrl;
          pa.filename = attachmentName;
          po.attachmentList.add(pa);
        }
      }
      po.userOnline = postMap.getBoolOrDefault("is_online");
      po.userBanned = postMap.getBoolOrDefault("is_ban");
      po.canDelete = postMap.getBoolOrDefault("can_delete");
      po.canBan = postMap.getBoolOrDefault("can_ban");
      po.canEdit = postMap.getBoolOrDefault("can_edit");
      po.canThank = postMap.getBoolOrDefault("can_thank");
      po.canLike = postMap.getBoolOrDefault("can_like");
      po.thanksCount = postMap.getCount("thanks_info");
      po.likeCount = postMap.getCount("likes_info");
      topic.addPost(po);
    }
    return topic;
  }

  public MessageBox getMessageBox() {
    final RPCMap messageBoxMap = this.xmlrpc("get_box_info").call();
    if (messageBoxMap != null && messageBoxMap.getBool("result")) {
      final MessageBox messageBox = MessageBox.builder()
          .remainingMessageCount(messageBoxMap.getInt("message_room_count"))
          .build();
      for (final RPCMap messageFolderMap : messageBoxMap.getRPCMapArray("list")) {
        messageBox.addMessageFolder(MessageFolder.builder()
            .id(messageFolderMap.getString("box_id"))
            .name(messageFolderMap.getByteString("box_name"))
            .messageCount(messageFolderMap.getInt("msg_count"))
            .unreadCount(messageFolderMap.getInt("unread_count"))
            .type(MessageFolder.Type.valueOf(messageFolderMap.getStringOrDefault("box_type", "DEFAULT")))
            .build());
      }
      return messageBox;
    } else {
      return null;
    }
  }

  public List<InboxItem> getMessages(final MessageFolder folder) {
    final RPCMap messagesMap = this.xmlrpc("get_box")
        .param(folder.getId())
        .call();
    if (messagesMap != null) {
      final List<InboxItem> messages = new ArrayList<InboxItem>();
      for (final RPCMap messageMap : messagesMap.getRPCMapArray("list")) {
        final InboxItem ii = new InboxItem();
        if (messageMap.containsKey("msg_state")) {
          final int state = messageMap.getInt("msg_state");
          if (state == 1) {
            ii.isUnread = true;
          }
        }

        ii.folderId = folder.getId();
        ii.sentDate = messageMap.getDate("sent_date").toString();
        ii.subject = messageMap.getByteString("msg_subject");
        ii.messageId = messageMap.getString("msg_id");
        ii.sender = messageMap.getByteString("msg_from");
        ii.senderId = messageMap.getString("msg_from_id");
        ii.senderAvatar = messageMap.getString("icon_url");
        messages.add(ii);
      }
      return messages;
    } else {
      return null;
    }
  }

  public Post getMessage(final String boxId, final String messageId, final boolean returnHtml) {
    final RPCMap messageMap = this.xmlrpc("get_message")
        .param(messageId)
        .param(boxId)
        .param(returnHtml)
        .call();
    if (messageMap != null) {
      final Post post = new Post();
      //TODO: Needed?
      //po.subforumId = subforumId;
      //po.thread_id = thread_id;
      //po.moderator = moderator;
      post.id = messageId;
      post.author = messageMap.getByteString("msg_from");
      post.authorId = messageMap.getString("msg_from_id");
      post.body = messageMap.getByteString("text_body");
      post.avatar = messageMap.getString("icon_url");
      post.tagline = "tagline";
      post.timestamp = messageMap.getDate("sent_date").toString();
      return post;
    } else {
      return null;
    }
  }

  public ApiResult deleteMessage(final String boxId, final String messageId) {
    final RPCMap resultMap = this.xmlrpc("delete_message")
        .param(messageId)
        .param(boxId)
        .call();
    return ApiResult.builder()
        .success(resultMap.getBool("result"))
        .message(resultMap.getByteString("result_text"))
        .build();
  }
}
