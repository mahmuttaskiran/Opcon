package com.opcon.notifier.operations;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.opcon.R;
import com.opcon.components.Message;
import com.opcon.components.Order;
import com.opcon.components.Post;
import com.opcon.database.PostBase;
import com.opcon.firebaseclient.ComponentSender;
import com.opcon.firebaseclient.FirebaseDatabaseManagement;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.utils.ImageStorageUtils;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.ui.activities.WaitingPostActivity;
import com.opcon.ui.fragments.occs.OperationPost;

import java.util.List;

/**
 * Created by Mahmut Taşkiran on 18/03/2017.
 */

public class PostProcessor implements OperationProcessor {

  public volatile static PostProcessor INSTANCE = new PostProcessor();
  public static PostProcessor getInstance() {
    if (INSTANCE == null) {
      synchronized (PostProcessor.class) {
        if (INSTANCE == null){
          INSTANCE = new PostProcessor();
        }
      }
    }
    return INSTANCE;
  }

  @Override public void process(Context context, Notifier notifier, @Nullable SpecialPacket sp, @Nullable OperationProcessManager.OperationProcessListener listener) {


    if (notifier.isTargetAmI()) {
      Order order = Order.newOrderForNotifierSid(notifier.getSid());
      PresenceManager pm = PresenceManager.getInstance(context.getApplicationContext());
      boolean connected = pm
          .isConnected();
      if (!connected) {
        pm.login();
        FirebaseDatabaseManagement.getInstance(context.getApplicationContext());
      }
      ComponentSender componentSender = new ComponentSender("order/" + notifier.getSender(), order);
      componentSender.sent();
    } else {

      if (notifier.getOperation().isPacketExists() && (sp == null || sp.isEmpty())) {
        if (listener != null) {
          listener.onFatalOperation(notifier);
        }
        // packet cannot provided!
        return;
      }

      Post.Builder postBuilder = new Post.Builder();

      Post.Builder builder1 = postBuilder.setOwner(PresenceManager.uid())
          .setRelationalNotifier(notifier.getSid())
          .setText(notifier.getOperation().getString(OperationPost.TEXT))
          .setTimestamp(System.currentTimeMillis())
          .rcs(notifier.getOperation().getBoolean(OperationPost.RCS))
          .setPacketType(notifier.getOperation().getPacketType());

      if (notifier.getOperation().isPacketExists() && sp != null && notifier.getOperation().getPacketType() == Packets._LAST_IMAGE) {
        String path = sp.getString(Message.Picture.FILE);

        if (TextUtils.isEmpty(path)) {
          return;
        }

        builder1.setUploadedFilename(ImageStorageUtils
            .getRandomFileNameForTimestamp(System.currentTimeMillis()))
            .setImageLocalePath(path);

      }

      Post post = postBuilder.built();


      if (post.getPacketType() == Packets._LOCATION) {
        post.setLatitude(sp.getDouble(Message.Location.LATITUDE));
        post.setLongitude(sp.getDouble(Message.Location.LATITUDE));
        post.setAddress(sp.getString(Message.Location.ADDRESS));
      } else if (post.getPacketType() == Packets._BATTERY_LEVEL) {
        if (sp != null && !sp.isEmpty()) {
          post.setBatteryDegree(sp.getInt(Message.Battery.PERCENT));
        }
      }

      // post is ready. so, check it!
      List<Post> all = PostBase.Utils.getAll(context);

      boolean process = true;
      if (!all.isEmpty()) {
        if (post.getPacketType() == Packets._LOCATION) {
          for (Post p : all) {
            if (p.getLatitude() == post.getLatitude() && p.getLongitude() == post.getLongitude()) {
              process = false;
              break;
            }
          }
        } else if (post.getPacketType() == Packets._BATTERY_LEVEL) {
          int bd = post.getBatteryDegree();
          for (Post p : all) {
            int pbd = p.getBatteryDegree();
            int max, min;
            max = Math.max(bd, pbd);
            min = Math.min(pbd, bd);
            int extract = max - min;
            if (extract <= 5) {
              process = false;
              break;
            }
          }
        } else if (post.getPacketType() == Packets._LAST_IMAGE){
          for (Post p : all) {
            if (p.getImageLocalePath() != null && p.getImageLocalePath().equals(post.getImageLocalePath())){
              process = false;
              break;
            }
          }
        }
      }

      if (!process) {
        return;
      }

      post.setId(PostBase.Utils.newPost(context, post));
      if (notifier.isNotificationOn(context)) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(context.getString(R.string.there_are_waiting_posts));
        builder.setContentText(context.getString(R.string.click_to_seen_details));
        builder.setSmallIcon(R.drawable.opcon_small_icon);
        Intent i = new Intent(context, WaitingPostActivity.class);
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, i, 0));
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(WaitingPostActivity.NOTIFICATION_ID, builder.getNotification());
      }
      WaitingPostActivity.WaitingPostEventDispatcher.getInstance().dispatchNewPost(post);
    }
  }
}
