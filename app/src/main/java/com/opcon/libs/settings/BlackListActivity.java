package com.opcon.libs.settings;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.components.Component;
import com.opcon.database.ContactBase;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.utils.AndroidEnvironmentsUtils;
import com.opcon.ui.activities.ContactsActivity;
import com.opcon.ui.dialogs.DialogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agency.tango.android.avatarview.views.AvatarView;
import butterknife.ButterKnife;
import butterknife.BindView;


public class BlackListActivity extends AppCompatActivity {

  @BindView(R.id.recyclerview)
  RecyclerView mRecyclerView;
  @BindView(R.id.note)
  TextView mNote;
  @BindView(R.id.activity_black_list)
  RelativeLayout activityBlackList;

  ProgressDialog mProgress;
  List<BlackUser> blackUsers;
  BlackListAdapter mAdapter;

  public static class BlackUser extends Component {
    public BlackUser(String name, String phone) {
      put("name", name);
      put("phone", phone);
    }
    public String getName() {
      return getString("name");
    }
    public String getPhone() {
      return getString("phone");
    }
    String getAvatar(Context context) {
      if (getString(3) != null) {
        return getString(3);
      }
      put(3, ContactBase.Utils.getValidAvatar(context, getString(2)));
      return getString(3);
    }
    @Override public int hashCode() {
      String pn = getPhone();
      if (pn!=null)
        return pn.hashCode();
      else
        return super.hashCode();
    }
    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      BlackUser object = (BlackUser) o;
      return getPhone() != null ? getPhone().equals(object.getPhone()) : object.getPhone() == null;
    }
  }

  public static class BlackLocaleBase extends SQLiteOpenHelper {
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("drop table if exists users;");
      onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("create table users (name varchar(50) not null, phone varchar(50) not null);");
    }

    public BlackLocaleBase(Context context) {
      super(context, "BlackUsers.db", null, 2);
    }

    public void add(String name, String phone) {
      ContentValues v = new ContentValues();
      v.put("name", name);
      v.put("phone", phone);
      SQLiteDatabase wd = getWritableDatabase();
      wd.insert("users", null, v);
      wd.close();
    }

    public void remove(String phone) {
      SQLiteDatabase wd = getWritableDatabase();
      wd.delete("users", "phone=?", new String[]{phone});
      wd.close();
    }

    public boolean isAlreadyBlack(String phone) {
      SQLiteDatabase rd = getReadableDatabase();
      Cursor cursor = rd.query("users", null, "phone=?", new String[]{phone}, null, null, null);
      boolean ret = cursor != null && cursor.moveToNext();
      if (cursor != null)
        cursor.close();
      rd.close();
      return ret;
    }

    public List<BlackUser> get() {
      List<BlackUser> list = new ArrayList<>();
      SQLiteDatabase rd = getReadableDatabase();
      Cursor c = rd.query("users", null, null, null, null, null, null);
      while (c != null && c.moveToNext()) {
        list.add(new BlackUser(c.getString(c.getColumnIndex("name")),
            c.getString(c.getColumnIndex("phone"))));
      }
      if (c!=null) {
        c.close();
      }
      rd.close();
      return list;
    }

    public static void removeSelf(Context context) {
      BlackLocaleBase blackLocaleBase = new BlackLocaleBase(context);
      SQLiteDatabase writableDatabase = blackLocaleBase.getWritableDatabase();
      writableDatabase.delete("users", null,null);
      writableDatabase.close();
      blackLocaleBase.close();
    }
  }

  private class BlackListAdapter extends RecyclerView.Adapter {
    List<BlackUser> list;

    BlackListAdapter(List<BlackUser> list) {
      this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new H(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_black_user, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
      H h = (H) holder;
      h.ffor(list.get(position));
    }

    @Override
    public int getItemCount() {
      return list.size();
    }

    private class H extends RecyclerView.ViewHolder implements View.OnClickListener {
      private TextView name, phone;
      private AvatarView avatar;

      H(View itemView) {
        super(itemView);
        avatar = (AvatarView) itemView.findViewById(R.id.avatar);
        name = (TextView) itemView.findViewById(R.id.title);
        phone = (TextView) itemView.findViewById(R.id.phone);
        itemView.setOnClickListener(this);
      }

      @Override
      public void onClick(View v) {
        if (v == itemView && getAdapterPosition() != -1) {
          AlertDialog.Builder aDB = new AlertDialog.Builder(BlackListActivity.this);
          aDB.setItems(new CharSequence[]{getString(R.string.remove_from_black_list) + list.get(getAdapterPosition()).getName()}, new DialogInterface.OnClickListener() {
            String phone = list.get(getAdapterPosition()).getPhone();
            @Override
            public void onClick(DialogInterface dialog, int which) {
              remove(phone);
            }
          });
          aDB.show();
        }
      }

      void ffor(BlackUser u) {
        AvatarLoader.load(avatar, u.getAvatar(getBaseContext()), u.getName());
        name.setText(u.getName());
        phone.setText(u.getPhone());
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_black_list);
    ButterKnife.bind(this);
    BlackLocaleBase blb = new BlackLocaleBase(this);
    blackUsers = blb.get();
    blb.close();
    LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    mRecyclerView.setLayoutManager(llm);
    mRecyclerView.hasFixedSize();
    mAdapter = new BlackListAdapter(blackUsers);
    mRecyclerView.setAdapter(this.mAdapter);

    noteVisibility();

    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(null);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp);
      getSupportActionBar().setElevation(0);
      getSupportActionBar(). setTitle(R.string.black_activity_title);
    }

  }

  private void noteVisibility() {
    if (blackUsers.isEmpty()) {
      mNote.setVisibility(View.VISIBLE);
    } else {
      mNote.setVisibility(View.GONE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.black_list, menu);
    return super.onCreateOptionsMenu(menu);
  }
  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.add) {
      add();
    } else if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }
  void add() {
    Intent intent = new Intent(this, ContactsActivity.class);
    intent.putExtra(ContactsActivity.ONLY_USER, true);
    startActivityForResult(intent, 1);
  }
  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1 && resultCode == RESULT_OK) {
      BlackLocaleBase bb = new BlackLocaleBase(this);
      String phone = data.getStringExtra(ContactsActivity.SELECTED_CONTACT_NUMBER);
      if (bb.isAlreadyBlack(phone)) {
        DialogUtils.alertOnlyOk(this, null, getString(R.string.already_in_black));
      } else{
        String name = data.getStringExtra(ContactsActivity.SELECTED_CONTACT_NAME);
        add(phone, name);
      }
      bb.close();
    }
  }

  private void add(final String phone, final String name) {
    if (!PresenceManager.getInstance(this).isJoined() || !AndroidEnvironmentsUtils.hasActiveInternetConnection(getApplicationContext())) {
      DialogUtils.alertOnlyOk(this, null, getString(R.string.not_internet_connection));
      return;
    }
    DatabaseReference blackRef = FirebaseDatabase.getInstance()
        .getReference("black/" + PresenceManager.uid());

    Map<String, Object> map = new HashMap<>();
    map.put(phone, new BlackUser(name, phone).toMap());

    mProgress = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false);
    blackRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(@NonNull Task<Void> task) {
        mProgress.dismiss();
        if (task.isSuccessful()) {
          addLocale(phone, name);
        } else {
          Snackbar.make(activityBlackList, R.string.cannot_do_later, Snackbar.LENGTH_SHORT);
        }
      }
    });
  }
  private void addLocale(String phone, String name) {
    BlackLocaleBase blb = new BlackLocaleBase(getApplicationContext());
    blb.add(name, phone);
    blb.close();
    mNote.setVisibility(View.GONE);
    blackUsers.add(new BlackUser(name, phone));
    mAdapter.notifyItemInserted(blackUsers.size());
  }
  private void remove(final String phone) {
    if (!PresenceManager.getInstance(this).isOnline()) {
       DialogUtils.alertOnlyOk(this, null, getString(R.string.not_internet_connection));
       return;
    }
    mProgress = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false);

    FirebaseDatabase.getInstance().getReference("black/" + PresenceManager.uid())
        .child(phone)
        .setValue(null)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            mProgress.dismiss();
            if (task.isSuccessful()) {
              removeLocale(phone);
            } else {
              Snackbar.make(activityBlackList, R.string.cannot_do_later, Snackbar.LENGTH_SHORT);
            }
          }
        });

  }
  private void removeLocale(String phone) {
    BlackLocaleBase bb = new BlackLocaleBase(this);
    bb.remove(phone);
    bb.close();
    int index = blackUsers.indexOf(new BlackUser(null, phone));
    blackUsers.remove(index);
    mAdapter.notifyItemRemoved(index);
    noteVisibility();
  }

}
