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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import de.enlightened.peris.site.Category;
import de.enlightened.peris.site.Config;
import de.enlightened.peris.site.Identity;
import de.enlightened.peris.site.ListedTopics;
import de.enlightened.peris.site.Message;
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

  //TODO: make private when legacy calls removed
  public XMLRPCClient getXMLRPCClient() {
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

  public Map<String, String> getCookies() {
    return this.getXMLRPCClient().getCookies();
  }

  private ApiResult parseApiResult(final RPCMap resultMap, final String defaultMessage) {
    if (resultMap != null) {
      return ApiResult.builder()
          .success(resultMap.getBool("result"))
          .message(resultMap.getByteStringOrDefault("result_text", defaultMessage))
          .build();
    } else {
      return null;
    }
  }

  private ApiResult parseApiResult(final RPCMap resultMap) {
    return this.parseApiResult(resultMap, null);
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
    for (String key : this.xmlRPCClient.getCookies().keySet()) {
      Log.d(TAG, "XMLRPC cookies[" + key + "] = " + this.xmlRPCClient.getCookies().get(key));
    }
    final ApiResult loginResult;
    if (loginMap != null) {
      loginResult = this.parseApiResult(loginMap, "wrong username or password");
      if (loginResult.isSuccess()) {
        this.identity = Identity.builder()
            .id(loginMap.getString("user_id"))
            .userName(loginMap.getByteStringOrDefault("login_name", username))
            .displayName(loginMap.getByteString("username"))
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
        .postCount(topicMap.getIntOrDefault("total_post_num", 0))
        .canPost(topicMap.getBoolOrDefault("can_reply", false))
        .forumName(topicMap.getByteString("forum_name"))
        .authorId(topicMap.getString("topic_author_id"))
        .authorName(topicMap.getByteString("topic_author_name"))
        .authorIcon(topicMap.getString("topic_author_avatar"))
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

  public Message getMessage(final String boxId, final String messageId, final boolean returnHtml) {
    final RPCMap messageMap = this.xmlrpc("get_message")
        .param(messageId)
        .param(boxId)
        .param(returnHtml)
        .call();
    if (messageMap != null) {
      //TODO: Needed?
      //po.subforumId = subforumId;
      //po.thread_id = thread_id;
      //po.moderator = moderator;
      // TODO: add attachments
      return Message.builder()
          .id(messageId)
          .author(messageMap.getByteString("msg_from"))
          .authorId(messageMap.getString("msg_from_id"))
          .authorOnline(messageMap.getBoolOrDefault("is_online"))
          .authorAvatar(messageMap.getString("icon_url"))
          .body(messageMap.getByteString("text_body"))
          .timestamp(messageMap.getDate("sent_date"))
          .build();
    } else {
      return null;
    }
  }

  public ApiResult deleteMessage(final String boxId, final String messageId) {
    final RPCMap resultMap = this.xmlrpc("delete_message")
        .param(messageId)
        .param(boxId)
        .call();
    return this.parseApiResult(resultMap);
  }

  private static final Map<Topic.Type, String> TOPIC_TYPE_MAP = new HashMap<>();

  static {
    TOPIC_TYPE_MAP.put(Topic.Type.Announcement, "ANN");
    TOPIC_TYPE_MAP.put(Topic.Type.Sticky, "TOP");
  }

  private ListedTopics readTopics(final RPCMap listedTopicsMap, final Topic.Type type, final String subforumId) {
    if (listedTopicsMap != null) {
      final ListedTopics listedTopics = ListedTopics.builder()
          .forumId(listedTopicsMap.getString("forum_id"))
          .forumName(listedTopicsMap.getByteString("forum_name"))
          .count(listedTopicsMap.getIntOrDefault("total_topic_num", 1))
          .unreadAnnouncementCount(listedTopicsMap.getIntOrDefault("unread_announce_count", 0))
          .unreadStickyCount(listedTopicsMap.getIntOrDefault("unread_sticky_count", 0))
          .postAllowed(listedTopicsMap.getBoolOrDefault("can_post"))
          .subscriptionAllowed(listedTopicsMap.getBoolOrDefault("can_subscribe"))
          .subscribed(listedTopicsMap.getBoolOrDefault("is_subscribed"))
          .build();
      if (listedTopics.getCount() > 0) {
        for (final RPCMap topicMap : listedTopicsMap.getRPCMapArray("topics")) {
          listedTopics.addTopic(Topic.builder()
              .id(topicMap.getString("topic_id"))
              .title(topicMap.getByteString("topic_title"))
              .type(type)
              .subforumId(topicMap.getStringOrDefault("forum_id", subforumId))
              .lastUpdate(topicMap.getDate("last_reply_time"))
              .forumName(topicMap.getByteString("forum_name"))
              .authorId(topicMap.getString("topic_author_id"))
              .authorName(topicMap.getByteString("topic_author_name"))
              .replyCount(topicMap.getIntOrDefault("reply_number", 0))
              .viewCount(topicMap.getIntOrDefault("view_number", 0))
              .hasNewPosts(topicMap.getBoolOrDefault("new_post"))
              .isClosed(topicMap.getBoolOrDefault("is_closed"))
              .authorIcon(topicMap.getString("icon_url"))
              .canStick(topicMap.getBoolOrDefault("can_stick"))
              .canDelete(topicMap.getBoolOrDefault("can_delete"))
              .canSubscribe(topicMap.getBoolOrDefault("can_subscribe", true))
              .canClose(topicMap.getBoolOrDefault("can_close"))
              .build());
        }
      }
      return listedTopics;
    } else {
      return null;
    }
  }

  public ListedTopics getTopics(final String subforumId, final int start, final int end, final Topic.Type type) {
    return this.readTopics(this.xmlrpc("get_topic")
        .param(subforumId)
        .param(start)
        .param(end)
        .optionalParam(TOPIC_TYPE_MAP.get(type))
        .call(), type, subforumId);
  }

  public ListedTopics getTopics(final String subforumId, final int start, final int end) {
    return this.getTopics(subforumId, start, end, Topic.Type.Default);
  }

  public ListedTopics getSubscribedTopics(final String subforumId, final int start, final int end) {
    return this.readTopics(this.xmlrpc("get_subscribed_topic")
        .param(start)
        .param(end)
        .call(), Topic.Type.Default, subforumId);
  }

  public ListedTopics getParticipatedTopics(final String subforumId, final int start, final int end) {
    if (this.identity == null) {
      return null;
    } else {
      return this.readTopics(this.xmlrpc("get_participated_topic")
          .param(this.identity.getUserName().getBytes())
          .param(start)
          .param(end)
          .param("")
          .param(this.identity.getId())
          .call(), Topic.Type.Default, subforumId);
    }
  }

  public ListedTopics searchTopic(final String subforumId, final String searchQuery, final int start, final int end) {
    return this.readTopics(this.xmlrpc("search_topic")
        .param(searchQuery.getBytes())
        .param(start)
        .param(end)
        .call(), Topic.Type.Default, subforumId);
  }

  public ListedTopics getLatestTopics(final String subforumId, final int start, final int end) {
    return this.readTopics(this.xmlrpc("get_latest_topic")
        .param(start)
        .param(end)
        .call(), Topic.Type.Default, subforumId);
  }

  public ListedTopics getUnreadTopics(final String subforumId) {
    return this.readTopics(this.xmlrpc("get_unread_topic")
        .call(), Topic.Type.Default, subforumId);
  }

  private List<Category> readCategories(final Map<String, Category> categories, final List<RPCMap> categoryMapList, final String parentId) {
    final List<Category> categoryList = new ArrayList<>();
    for (RPCMap categoryMap : categoryMapList) {
      final Category category = Category.builder()
          .id(categoryMap.getString("forum_id"))
          .parentId(parentId)
          .hash((parentId == null ? "" : parentId + "###") + categoryMap.getString("forum_id"))
          .name(categoryMap.getByteString("forum_name"))
          .logoUrl(categoryMap.getString("logo_url"))
          .url(categoryMap.getString("url"))
          .isSubscribed(categoryMap.getBoolOrDefault("is_subscribed"))
          .canSubscribe(categoryMap.getBoolOrDefault("can_subscribe"))
          .hasNewTopic(categoryMap.getBoolOrDefault("new_post"))
          .children(this.readCategories(categories, Arrays.asList(categoryMap.getRPCMapArray("children")), categoryMap.getString("forum_id")))
          .build();
      categoryList.add(category);
      categories.put(category.getId(), category);
    }
    return categoryList;
  }

  private List<Category> readCategories(final Map<String, Category> categories, final List<RPCMap> categoryMapList) {
    return this.readCategories(categories, categoryMapList, null);
  }

  public Map<String, Category> getCategory(final String categoryId) {
    final Map<String, Category> categories = new HashMap<>();
    categories.put(categoryId, Category.builder()
        .id(categoryId)
        .children(this.readCategories(categories, this.xmlrpc("get_forum")
            .param(Boolean.TRUE)
            .param(categoryId)
            .callAsList()))
        .build());
    return categories;
  }

  public Map<String, Category> getCategories() {
    final Map<String, Category> categories = new HashMap<>();
    categories.put(Category.ROOT_ID, Category.builder()
        .id(Category.ROOT_ID)
        .children(this.readCategories(categories, this.xmlrpc("get_forum")
            .callAsList()))
        .build());
    return categories;
  }

  public List<Category> getSubscribedCategories() {
    final List<Category> categoryList = new ArrayList<>();
    final RPCMap mapSubscribedCategories = this.xmlrpc("get_subscribed_forum").call();
    for (RPCMap categoryMap : mapSubscribedCategories.getRPCMapArray("forums")) {
      categoryList.add(Category.builder()
          .id(categoryMap.getString("forum_id"))
          .name(categoryMap.getByteString("forum_name"))
          .isSubscribed(true)
          .logoUrl(categoryMap.getString("icon_url"))
          .hasNewTopic(categoryMap.getBoolOrDefault("new_post"))
          //TODO
          //ca.subforumId = this.subforumId;
          //ca.color = this.background;
          .build());
    }
    return categoryList;
  }

  public ApiResult markAllTopicsRead() {
    return this.parseApiResult(this.xmlrpc("mark_all_as_read").call());
  }

  public ApiResult markForumTopicsRead(final String subforumId) {
    return this.parseApiResult(this.xmlrpc("mark_all_as_read")
        .param(subforumId)
        .call());
  }

  public ApiResult subscribeTopic(final String topicId) {
    return this.parseApiResult(this.xmlrpc("subscribe_topic")
        .param(topicId)
        .call());
  }

  public ApiResult unsubscribeTopic(final String topicId) {
    return this.parseApiResult(this.xmlrpc("unsubscribe_topic")
        .param(topicId)
        .call());
  }

  public ApiResult setTopicSticky(final String topicId, final boolean sticky) {
    return this.parseApiResult(this.xmlrpc("m_stick_topic")
        .param(topicId)
        .param(sticky ? 1 : 2)
        .call());
  }

  public ApiResult setTopicLocked(final String topicId, final boolean lock) {
    return this.parseApiResult(this.xmlrpc("m_close_topic")
        .param(topicId)
        .param(lock ? 1 : 2)
        .call());
  }

  public ApiResult deleteTopic(final String topicId) {
    return this.parseApiResult(this.xmlrpc("m_delete_topic")
        .param(topicId)
        .param(2)
        .call());
  }
}
