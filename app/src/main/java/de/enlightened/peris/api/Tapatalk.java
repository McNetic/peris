package de.enlightened.peris.api;

import android.util.Log;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Vector;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.enlightened.peris.Post;
import de.enlightened.peris.PostAttachment;
import de.enlightened.peris.Server;
import de.enlightened.peris.site.Topic;
import de.enlightened.peris.support.RPCMap;
import de.timroes.axmlrpc.XMLRPCClient;

/**
 * Created by Nicolai Ehemann on 13.02.2017.
 */

public class Tapatalk {

  private static final String TAG = Tapatalk.class.getName();

  private XMLRPCClient xmlRPCClient;
  private Server server;
  private SSLContext sc;

  public void setServer(final Server server) {
    this.server = server;
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

  public final Object xmlCall(final String method, final Vector parms) {

    Log.d(TAG, "Performing Server Call: Method = " + method + " (URL: " + this.server.getTapatalkURL() + ")");
    try {
      final Object[] parmsobject = new Object[parms.size()];
      for (int i = 0; i < parms.size(); i++) {
        parmsobject[i] = parms.get(i);
      }

      if (this.xmlRPCClient == null) {
        if (this.server.serverHttps) {
          this.sc = SSLContext.getInstance("SSL");
          this.sc.init(null, this.trustAllCerts, new SecureRandom());
          HttpsURLConnection.setDefaultSSLSocketFactory(this.sc.getSocketFactory());
          HttpsURLConnection.setDefaultHostnameVerifier(this.hostnameVerifier);
        }
        this.xmlRPCClient = new XMLRPCClient(this.server.getTapatalkURL(), XMLRPCClient.FLAGS_ENABLE_COOKIES);
      }

      return this.xmlRPCClient.call(method, parmsobject);
    } catch (Exception ex) {
      String.format("Tapatalk call error (%s) : %s", ex.getClass().getName(), method);
      if (ex.getMessage() != null) {
        Log.e(TAG, ex.getMessage());
      } else {
        Log.e(TAG, "(no message available)");
      }
    }
    return null;
  }

  public Topic getTopic(final String topicId, final int startNum, final int lastNum, final boolean returnHtml) {
    final Vector paramz = new Vector();
    paramz.addElement(topicId);
    paramz.addElement(startNum);
    paramz.addElement(lastNum);
    paramz.addElement(true);

    final Object obj = this.xmlCall("get_thread", paramz);
    final RPCMap topicMap = RPCMap.of(obj);
    final Topic topic = Topic.builder()
        .curTotalPosts(topicMap.getIntOrDefault("total_post_num", 0))
        .canPost(topicMap.getBoolOrDefault("can_reply", false))
        .build();
    /*
    forum_id class java.lang.String value 5027
    topic_author_avatar class java.lang.String value
    can_report class java.lang.Boolean value true
    topic_title class [B value [B@4caf54a
    real_topic_id class java.lang.String value 3545900
    can_subscribe class java.lang.Boolean value false
    position class java.lang.Integer value 1
    can_merge_post class java.lang.Boolean value false
    topic_author_name class [B value [B@2632cbb
    can_merge class java.lang.Boolean value false
    topic_author_id class java.lang.String value 8025907
    view_number class java.lang.Integer value 186
    posts class [Ljava.lang.Object; value [Ljava.lang.Object
    can_upload class java.lang.Boolean value false
    topic_id class java.lang.String value 3545900
    is_approved class java.lang.Boolean value true
    forum_name class [B value [B@bc7ed31
    is_moved class java.lang.Boolean value false
    is_poll class java.lang.Boolean value false
    breadcrumb class [Ljava.lang.Object; value [Ljava.lang.O
    */

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
      /*
      key icon_url class java.lang.String value
      key timestamp class java.lang.String value 1485513284
      key post_time class java.util.Date value Fri Jan 27 11:34:44 GMT+01:00
      key post_author_id class java.lang.String value 8025907
      key post_title class [B value [B@88dd997
      key allow_smilies class java.lang.Boolean value true
      key attachments class [Ljava.lang.Object; value [Ljava.lang.Object;@e7
      key editor_id class java.lang.String value 8025907
      key post_author_name class [B value [B@a7d2b6d
      key post_content class [B value [B@dd4e3a2
      key post_count class java.lang.Integer value 1
      key attachment_authority class java.lang.Integer value 0
      key editor_name class [B value [B@24c7833
      key user_type class [B value [B@1a0e7f0
      key edit_reason class [B value [B@bce1169
      key post_id class java.lang.String value 70752573
      key edit_time class java.lang.String value 1485713863
    */
    }
    return topic;
  }
}
