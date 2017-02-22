package de.enlightened.peris.api;

import android.util.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.enlightened.peris.Post;
import de.enlightened.peris.PostAttachment;
import de.enlightened.peris.Server;
import de.enlightened.peris.site.Config;
import de.enlightened.peris.site.Identity;
import de.enlightened.peris.site.Topic;
import de.enlightened.peris.support.RPCMap;
import de.enlightened.peris.support.XMLRPCCall;
import de.timroes.axmlrpc.XMLRPCClient;

/**
 * Created by Nicolai Ehemann on 13.02.2017.
 */

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
      this.serverConfig = Config.builder()
          .serverPluginVersion(configMap.getString("version"))
          .accountManagementEnabled(configMap.getStringBoolOrDefault("inappreg"))
          .build();
      Log.i(TAG, String.format("Forum is ", this.serverConfig.getForumSystem()));
    }
    return this.serverConfig;
  }

  public Identity getIdentity() {
    return this.identity;
  }

  public LoginResult login(final String username, final String password) {
    final RPCMap loginMap = this.xmlrpc("login")
        .param(username.getBytes())
        .param(password.getBytes())
        .call();
    final LoginResult loginResult = LoginResult.builder()
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

    for (final RPCMap postMap : topicMap.getRPCMap("posts")) {
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

      for (final RPCMap attachmentMap : postMap.getRPCMap("attachements")) {
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
}
