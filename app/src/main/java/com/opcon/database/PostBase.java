package com.opcon.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.opcon.components.Post;
import com.opcon.libs.utils.SQLCloseUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 18/03/2017.
 */

public class PostBase extends SQLiteOpenHelper {

  public volatile static PostBase singleton;

  public static PostBase getInstance(Context context){
    context = context.getApplicationContext();
    if (SQLCloseUtils.isPoolOrCloseOrNull(singleton)) {
      synchronized (PostBase.class) {
        if (SQLCloseUtils.isPoolOrCloseOrNull(singleton)) {
          if (singleton != null) {
            singleton.close();
          }
          singleton = new PostBase(context);
        }
      }
    }
    return singleton;
  }

  private PostBase(Context context) {
    super(context, "PostBase.db", null, 9);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table posts (" +
        "id integer primary key autoincrement not null," +
        "text varchar(300)," +
        "timestamp integer," +
        "packetParams varchar(400)," +
        "owner varchar(30)," +
        "isRcs integer," +
        "privacy integer," +
        "packetType integer," +
        "imageUri varchar(255)," +
        "relationNotifier varchar(30)," +
        "uploadedFilename varchar(40)," +
        "latitude integer," +
        "longitude integer," +
        "address varchar(200)," +
        "degree integer," +
        "accepted integer" +
        ");");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("drop table if exists posts;");
    onCreate(db);
  }

  public static class Utils {

    public static synchronized void removeSelf(Context context) {
      PostBase postBase = new PostBase(context);
      SQLiteDatabase writableDatabase = postBase.getWritableDatabase();
      writableDatabase.delete("posts", null, null);
      writableDatabase.close();
      postBase.close();
    }

    public static synchronized int newPost(Context context, Post post) {
      PostBase pb = PostBase.getInstance(context);
      SQLiteDatabase wb = pb.getWritableDatabase();
      ContentValues v = new ContentValues();
      v.put("isRcs", post.isRcs() ? 1: 0);
      v.put("privacy", post.getPrivacy());
      v.put("degree", post.getBatteryDegree());
      v.put("address", post.getAddress());
      v.put("longitude", post.getLongitude());
      v.put("accepted", 0);
      v.put("latitude", post.getLatitude());
      v.put("text", post.getText());
      v.put("uploadedFilename", post.getUploadedFilename());
      v.put("owner", post.getOwner());
      v.put("timestamp", post.getTimestamp());
      v.put("packetType", post.getPacketType());
      v.put("relationNotifier", post.getRelationNotifierSid());
      v.put("imageUri", TextUtils.isEmpty(post.getImageLocalePath()) ? null : post.getImageLocalePath());
      long result = wb.insert("posts", null, v);
      wb.close();
      return (int) result;
    }

    public static Post parse(Cursor c) {

      boolean ask = c.getInt(c.getColumnIndex("isRcs")) == 1;
      int privacy = c.getInt(c.getColumnIndex("privacy"));

      int packetType = c.getInt(c.getColumnIndex("packetType"));
      String uri = c.getString(c.getColumnIndex("imageUri"));
      String notifier = c.getString(c.getColumnIndex("relationNotifier"));

      Post.Builder builder = new Post.Builder();
      builder
          .setImageLocalePath(uri)
          .setPrivacy(privacy)
          .setPacketType(packetType)
          .setOwner(c.getString(c.getColumnIndex("owner")))
          .rcs(ask)
          .setUploadedFilename(c.getString(c.getColumnIndex("uploadedFilename")))
          .setTimestamp(c.getLong(c.getColumnIndex("timestamp")))
          .setText(c.getString(c.getColumnIndex("text")))
          .setRelationalNotifier(notifier);


      Post parsed = builder.built();
      parsed.setId(c.getInt(c.getColumnIndex("id")));
      parsed.setLatitude(c.getDouble(c.getColumnIndex("latitude")));
      parsed.setLongitude(c.getDouble(c.getColumnIndex("longitude")));
      parsed.setAddress(c.getString(c.getColumnIndex("address")));
      parsed.setBatteryDegree(c.getInt(c.getColumnIndex("degree")));
      parsed.setAccepted(c.getInt(c.getColumnIndex("accepted")) == 1);
      return parsed;
    }

    public static @NonNull synchronized List<Post> getAll(Context context) {
      List<Post> posts = new ArrayList<>();
      PostBase postBase = PostBase.getInstance(context);
      SQLiteDatabase readableDatabase = postBase.getReadableDatabase();
      Cursor c = readableDatabase.query("posts", null, null, null, null, null, null);
      if (c != null && c.getCount() > 0 ){
        while (c.moveToNext()) {
          Post parse = parse(c);
          if (parse.isRcs()) {
            parse.setRelationNotifier(NotifierProvider.Utils.get(context, parse.getRelationNotifierSid()));
          }
          posts.add(parse);
        }
      }
      if (c != null) {
        c.close();
      }
      readableDatabase.close();
      return posts;
    }

    public static synchronized void delete(Context context, int id) {
      Timber.d("PostPoster: will be delete: " + id);
      PostBase instance = PostBase.getInstance(context);
      SQLiteDatabase w = instance.getWritableDatabase();
      w.delete("posts", "id =" + id, null);
      w.close();
    }

    public static synchronized void updatePrivacy(Context context, int id, int privacy) {
      PostBase instance = PostBase.getInstance(context);
      SQLiteDatabase w = instance.getWritableDatabase();
      ContentValues v = new ContentValues();
      v.put("privacy", privacy);
      w.update("posts", v,  "id =" + id, null);
      w.close();
    }

    public static synchronized List<Post> getIfHasImage(Context context) {
      List<Post> posts = new ArrayList<>();
      PostBase postBase = PostBase.getInstance(context);
      SQLiteDatabase readableDatabase = postBase.getReadableDatabase();
      Cursor c = readableDatabase.query("posts", null, "imageUri <> ''", null, null, null, null);
      if (c != null && c.getCount() > 0 ){
        while (c.moveToNext()) {
          Post parse = parse(c);
          if (parse.isRcs()) {
            parse.setRelationNotifier(NotifierProvider.Utils.get(context, parse.getRelationNotifierSid()));
          }
          posts.add(parse);
        }
      }
      if (c != null) {
        c.close();
      }
      readableDatabase.close();
      return posts;
    }

    public static void deletePostThatRelationWithRemovedImages(Context context) {
      List<Post> all = getAll(context);
      try {
        for (Post post : all) {
          if (post.getImageLocalePath() != null) {
            String path = Uri.parse(post.getImageLocalePath()).getPath();
            if (!new File(path).exists()) {
              Timber.d("PostPoster deleted: %d",  post.getId());
              delete(context, post.getId());
            }
          }
        }
      } catch (SecurityException e) {
        e.printStackTrace();
      }
    }

    public static synchronized void update(Context context, Post post) {
      if (post.getId() == 0) {
        throw new IllegalArgumentException("post id isn't unique.");
      }

      PostBase postBase = PostBase.getInstance(context);
      SQLiteDatabase w = postBase.getWritableDatabase();
      ContentValues v = new ContentValues();
      v.put("isRcs", post.isRcs() ? 1: 0);
      v.put("privacy", post.getPrivacy());
      v.put("text", post.getText());
      v.put("degree", post.getBatteryDegree());
      v.put("address", post.getAddress());
      v.put("accepted", post.isAccepted());
      v.put("longitude", post.getLongitude());
      v.put("latitude", post.getLatitude());
      v.put("uploadedFilename", post.getUploadedFilename());
      v.put("owner", post.getOwner());
      v.put("timestamp", post.getTimestamp());
      v.put("packetType", post.getPacketType());
      v.put("relationNotifier", post.getRelationNotifierSid());
      v.put("imageUri", TextUtils.isEmpty(post.getImageLocalePath()) ? null : Uri.fromFile(new File(post.getImageLocalePath())).toString());

      w.update("posts", v, "id =" + post.getId(), null);
      w.close();

    }

    public static void deleteAll(Context c) {

      SQLiteDatabase wd = getInstance(c).getWritableDatabase();
      wd.delete("posts", "accepted = 0", null);
      wd.close();

    }

    public static List<Post> getAcceptedLocationalPosts(Context context) {
      List<Post> posts = new ArrayList<>();

      SQLiteDatabase rb = getInstance(context).getReadableDatabase();
      Cursor q = rb.query("posts", null, "accepted = 1 and (longitude <> 0 and longitude <> -1) and (latitude <> 0 and latitude <> -1)", null, null, null, null);

      if (q != null) {
        while (q.moveToNext()) {
          Post parse = parse(q);
          if (parse != null) {
            posts.add(parse);
          }
        }
        q.close();
      }

      rb.close();

      return posts;
    }

  }

}
