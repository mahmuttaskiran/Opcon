package com.opcon.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.Phonenumber;
import com.opcon.R;
import com.opcon.components.Contact;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.registration.RegistrationManagement;
import com.opcon.libs.utils.SQLCloseUtils;
import com.opcon.ui.fragments.ContactFragment;
import com.opcon.utils.MobileNumberUtils;
import com.opcon.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import timber.log.Timber;

/**
 *
 * Created by Mahmut Taşkiran on 02/12/2016.
 *
 */

public class ContactBase extends SQLiteOpenHelper {

    private volatile static ContactBase instance;

    public static final String TABLE_CONTACTS = "contacts";

    private static final String ID = "_id";
    private static final String NAME = "name";
    public static final String NUMBER = "internationalPhone";
    private static final String RAVATAR = "remoteAvatar";
    private static final String LAVATAR = "localeAvatar";
    public static final String USER = "isUser";

    public static final String VERSION = "version";

    private static final int VR = 21;

    private int counter;
    private SQLiteDatabase database;

    public synchronized SQLiteDatabase openConnection() {
        counter ++;
        if (counter == 1 || !database.isOpen()) {
            database = getWritableDatabase();
        }
        return database;
    }

    public synchronized void closeConnection() {
        counter --;
        if(counter == 0) {
            database.close();
        }
    }

    public static ContactBase getInstance(Context context) {
        if (SQLCloseUtils.isPoolOrCloseOrNull(instance)) {
            synchronized (ContactBase.class) {
                if (SQLCloseUtils.isPoolOrCloseOrNull(instance)) {
                    if (instance != null) {
                        instance.close();
                    }
                    instance = new ContactBase(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private ContactBase(Context context) {
        super(context, "LocalContacts.db", null, VR);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table contacts (" +
            "_id integer primary key autoincrement not null, " +
            "name varchar(50)," +
            "internationalPhone varchar(50)," +
            "remoteAvatar varchar(250)," +
            "localeAvatar varchar(250)," +
            "isUser integer, " +
            "jid varchar(150)," +
            "version integer" +
            ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists contacts;");
        onCreate(db);
    }

    private static boolean isNumEntriesSmallerThan(Context context, int min) {
        long num = DatabaseUtils.queryNumEntries(getInstance(context).openConnection(), TABLE_CONTACTS, null);
        getInstance(context).closeConnection();
        return num < min;
    }

    /**
     *
     * Created by Mahmut Taşkiran on 03/12/2016.
     *
     */

    public static class Utils {

        private static final String PREF_CONTACT_COUNT = "totalContact";
        private static final String PREF_LAST_UPDATE = "ContactsUpdateTimeStamp";

        private static final String TEST_TAG = "PerformanceOfCaching->";

        private static ContactFragment.ContactCache mContactCache = ContactFragment.ContactCache.getInstance();

        public static synchronized @NonNull String getName(Context c, String phone){
          if (phone == null) {
            return "No name";
          }
          if (phone.equals(PresenceManager.uid())) {
            return RegistrationManagement.getInstance().getName(c, R.string.iam);
          }
          String find_name;
          Contact contact = mContactCache.getContact(phone);
          if (contact != null) {
            find_name = contact.name;
          } else {
            find_name = getSingleString(c, phone, NAME);
          }
          return TextUtils.isEmpty(find_name) ? MobileNumberUtils.toInternational(phone, null, true) : find_name;
        }

        public static synchronized @NonNull String getPureName(Context c, String phone) {
            if (phone == null) {
                return "No name";
            }

            if (phone.equals(PresenceManager.uid())) {
                return RegistrationManagement.getInstance().getName(c, R.string.iam);
            }

            String name = getSingleString(c, phone, NAME);
            return TextUtils.isEmpty(name) ? phone: name;
        }

        private static synchronized long getLastUpdateTimestamp(Context context) {
            return PreferenceUtils.getLong(context, PREF_LAST_UPDATE, 0);
        }

        public synchronized static Contact getContact(Context context, String internationalPhone) {
            Cursor cursor = getInstance(context).openConnection().query(TABLE_CONTACTS,
                    null, NUMBER + "=?", new String[]{internationalPhone}, null, null, null);
            Contact contact = null;
            if (cursor != null && cursor.moveToNext()) {
                contact = new Contact();
                contact.name = cursor.getString(cursor.getColumnIndex(NAME));
                contact.number = cursor.getString(cursor.getColumnIndex(NUMBER));
                contact.profileUri = detectProfileUri(cursor.getString(cursor.getColumnIndex(LAVATAR)), cursor.getString(cursor.getColumnIndex(RAVATAR)));
                contact.hasOpcon = cursor.getInt(cursor.getColumnIndex(USER)) == 1;
                contact.lid = cursor.getInt(cursor.getColumnIndex(ID));
            }
            if (cursor != null)
                cursor.close();
            getInstance(context).closeConnection();
            return contact;
        }

        public synchronized static Contact getContactFull(Context context, int id) {

            Cursor cursor = getInstance(context).openConnection().query(TABLE_CONTACTS,
                    null, ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
            Contact contact = null;
            if (cursor != null && cursor.moveToNext()) {
                contact = new Contact();
                contact.name = cursor.getString(cursor.getColumnIndex(NAME));
                contact.number = cursor.getString(cursor.getColumnIndex(NUMBER));
                contact.profileUri = detectProfileUri(cursor.getString(cursor.getColumnIndex(LAVATAR)), cursor.getString(cursor.getColumnIndex(RAVATAR)));
                contact.hasOpcon = cursor.getInt(cursor.getColumnIndex(USER)) == 1;
                contact.lid = cursor.getInt(cursor.getColumnIndex(ID));
            }
            if (cursor != null)
                cursor.close();
            getInstance(context).closeConnection();
            return contact;
        }

        private synchronized static Contact getContact(SQLiteDatabase db, String ipn) {
            Cursor cursor = db.query(TABLE_CONTACTS,
                null, NUMBER + "=?", new String[]{ipn}, null, null, null);
            Contact contact = null;
            if (cursor != null && cursor.moveToNext()) {
                contact = new Contact();
                contact.name = cursor.getString(cursor.getColumnIndex(NAME));
                contact.number = cursor.getString(cursor.getColumnIndex(NUMBER));
                contact.profileUri = detectProfileUri(cursor.getString(cursor.getColumnIndex(LAVATAR)), cursor.getString(cursor.getColumnIndex(RAVATAR)));
                contact.hasOpcon = cursor.getInt(cursor.getColumnIndex(USER)) == 1;
                contact.lid = cursor.getInt(cursor.getColumnIndex(ID));
            }
            if (cursor != null)
                cursor.close();
            return contact;
        }

        public synchronized static Contact getContact(Context context, int id) {
            Cursor cursor = getInstance(context).openConnection().query(TABLE_CONTACTS,
                    null, ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
            Contact contact = null;
            if (cursor != null && cursor.moveToNext()) {
                contact = new Contact();
                contact.name = cursor.getString(cursor.getColumnIndex(NAME));
                String lavatar = cursor.getString(cursor.getColumnIndex(LAVATAR)),
                ravatar = cursor.getString(cursor.getColumnIndex(RAVATAR));
                contact.profileUri = detectProfileUri(lavatar, ravatar);
                contact.number = cursor.getString(cursor.getColumnIndex(NUMBER));
                contact.hasOpcon = cursor.getInt(cursor.getColumnIndex(USER)) == 1;
            }
            if (cursor != null)
                cursor.close();
            getInstance(context).closeConnection();
            return contact;
        }

        public synchronized static void update(Context context) {
            final long lastUpdate = getLastUpdateTimestamp(context);
            final long NOW = System.currentTimeMillis();

            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY + " COLLATE LOCALIZED asc");

            String userLocale = RegistrationManagement.getInstance().getLocale(context);
            if (TextUtils.isEmpty(userLocale)) userLocale = Locale.getDefault().getCountry();

            userLocale = userLocale.toUpperCase();

            SQLiteDatabase sqLiteDatabase = getInstance(context).openConnection();

            if (cursor != null && cursor.getCount() > 0) {
                int PHONE_INDEX = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int NAME_INDEX = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int PROFIlE_URI_INDEX = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);
                while (cursor.moveToNext()) {
                    String __phone = cursor.getString(PHONE_INDEX);
                    String __name = cursor.getString(NAME_INDEX);
                    String __profileUri = cursor.getString(PROFIlE_URI_INDEX);
                    if (TextUtils.isEmpty(__name) || TextUtils.isEmpty(__phone))
                        continue;

                    String lcl;
                    if ((lcl = MobileNumberUtils.checkIsValidAndGetValidLocale(__phone, userLocale)) != null) {
                        __phone = MobileNumberUtils.toInternational(__phone, lcl, false);
                        Contact contact = getContact(getInstance(context).openConnection(), __phone);
                        if (contact == null) {
                            Contact newContact = add(sqLiteDatabase, __name, __phone, __profileUri, false, NOW);
                            mContactCache.put(newContact.lid, newContact);
                        } else {
                            update(sqLiteDatabase, contact.lid, __name, __phone, __profileUri,
                                contact.hasOpcon, NOW);
                        }
                    }
                }
            }

            if (cursor != null)
                cursor.close();

            sqLiteDatabase.delete(TABLE_CONTACTS, VERSION + "=" + lastUpdate, null);
            sqLiteDatabase.delete(TABLE_CONTACTS, NUMBER + "=?", new String[]{PresenceManager.uid()});
            getInstance(context).closeConnection();
            PreferenceUtils.putLong(context, PREF_LAST_UPDATE, NOW);
            int totalContacts = getUserContactsSize(context);
            PreferenceUtils.putInt(context, PREF_CONTACT_COUNT, totalContacts);
        }

        private synchronized static Contact add(SQLiteDatabase db, String name, String phone, String lavatar, boolean hasopcon, long lastUpdateTimestamp) {
            ContentValues values = new ContentValues();
            values.put(NAME, name);
            values.put(NUMBER, phone);
            values.put(LAVATAR, lavatar);
            values.put(USER, hasopcon ? 1: 0);
            values.put(VERSION, lastUpdateTimestamp);

            Contact contact = new Contact();
            contact.name = name;
            contact.number = phone;
            contact.hasOpcon = hasopcon;
            contact.profileUri = lavatar;
            contact.lid = (int) db.insert(TABLE_CONTACTS, null, values);
            return contact;
        }

        private synchronized static void update(SQLiteDatabase db, int id, String name, String phone, String lavatar, boolean hasopcon, long lastUpdateTimestamp) {

            ContentValues values = new ContentValues();
            values.put(NAME, name);
            values.put(NUMBER, phone);
            values.put(LAVATAR, lavatar);
            values.put(USER, hasopcon ? 1: 0);
            values.put(VERSION, lastUpdateTimestamp);
            db.update(TABLE_CONTACTS, values, ID + "=?", new String[]{String.valueOf(id)});
        }


        private synchronized static String getSingleString(Context context, String internationalPhone, String columnName) {

            Cursor cursor = getInstance(context).openConnection().query(TABLE_CONTACTS, new String[]{columnName},
                    NUMBER + "=?", new String[]{internationalPhone}, null, null, null);
            String __ret = null;
            if (cursor.moveToNext()) {
                __ret = cursor.getString(cursor.getColumnIndex(columnName));
            }
            cursor.close();
            getInstance(context).closeConnection();
            return __ret;
        }

        static String detectProfileUri(String lavatar, String ravatar) {
            if (TextUtils.isEmpty(ravatar)) {
                return lavatar;
            }
            return ravatar;
        }

        public synchronized static String getValidAvatar(Context context, String phoneNumber) {

          Contact contact = mContactCache.getContact(phoneNumber);
          if (contact != null) {
            Timber.d(TEST_TAG + "getted from cache %s", contact.number);

            return contact.profileUri;

          }

          if (phoneNumber == null) {
                return null;
            }
            if (phoneNumber.equals(PresenceManager.uid())) {
                return PresenceManager.getAvatar(context);
            }
            String lavatar,ravatar;
            lavatar = getSingleString(context, phoneNumber, LAVATAR);
            ravatar = getSingleString(context, phoneNumber, RAVATAR);
            return detectProfileUri(lavatar, ravatar);
        }

        public synchronized static List<Integer> searchIds(Context context, String what) {

            List<Integer> ids = new ArrayList<>();



            Cursor cursor = getInstance(context).openConnection().query(TABLE_CONTACTS, new String[]{String.valueOf(ID)},
                    NAME + " like ?", new String[] {"%" + what + "%"}, null, null, NAME + " COLLATE LOCALIZED asc");

            if (cursor == null) {
                getInstance(context).closeConnection();
                return ids;
            }

            if (cursor.getCount() > 0) {
                final int indexOfId = cursor.getColumnIndex(ID);
                while (cursor.moveToNext()) {
                    ids.add(cursor.getInt(indexOfId));
                }
            }

            cursor.close();
            getInstance(context).closeConnection();
            return ids;

        }

        public synchronized static int getContactsSize(Context context) {
            long count = DatabaseUtils.queryNumEntries(getInstance(context).openConnection(), TABLE_CONTACTS);
            getInstance(context).closeConnection();
            return (int) count;
        }

        private synchronized static List<Integer> getIds(Context context, String selection, String[] selectionArgs) {
            List<Integer> ids = new ArrayList<>();

            Cursor cursor = getInstance(context).openConnection().query(TABLE_CONTACTS,
                    new String[]{ID},
                    selection, selectionArgs, null, null, NAME + " COLLATE LOCALIZED asc");

            if (cursor == null) {
                getInstance(context).closeConnection();
                return ids;
            }

            if (cursor.getCount() > 0) {
                final int indexOfId = cursor.getColumnIndex(ID);
                while (cursor.moveToNext()) {
                    ids.add(cursor.getInt(indexOfId));
                }
            }
            cursor.close();
            getInstance(context).closeConnection();
            return ids;

        }
        public synchronized static List<Integer> getAllContactIds(Context context) {
            List<Integer> users = getIds(context, USER + "=?", new String[]{"1"});
            List<Integer> others = getIds(context, USER + "=?", new String[]{"0"});
            List<Integer> all = new ArrayList<>();
            all.addAll(users);
            all.addAll(others);
            return all;
        }

        public synchronized static boolean isUpdateNecessary(Context c, boolean force) {

            int queryCount;
            if (force) {
              queryCount = PreferenceUtils.getInt(c, "refreshQueryCount", 0);
              if (queryCount >= 5){
                PreferenceUtils.putInt(c, "refreshQueryCount", 0);
              } else {
                PreferenceUtils.putInt(c, "refreshQueryCount", queryCount + 1);
              }
            } else {
              queryCount = 0;
            }

            int lTotalContacts = PreferenceUtils.getInt(c, PREF_CONTACT_COUNT, 0);
            int totalContacts = getUserContactsSize(c);
            return ((queryCount >= 4 && force) || (totalContacts != lTotalContacts) || (isNumEntriesSmallerThan(c, 1)));
        }

        private synchronized static int getUserContactsSize(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            int count = 0;
            if (cursor != null) {
                count = cursor.getCount();
                cursor.close();
            }
            return count;
        }

        public synchronized static List<String> getContactsNumbers(Context context) {
            List<String> phones = new ArrayList<>();
            Cursor cursor = getInstance(context).openConnection().query(TABLE_CONTACTS, new String[] {"internationalPhone"},
                    null, null, null, null, null);
            if (cursor == null || cursor.getCount() < 1) {
                if (cursor != null)
                    cursor.close();
                getInstance(context).closeConnection();
                return phones;
            }
            int indexOfPhone = cursor.getColumnIndex("internationalPhone");
            while (cursor.moveToNext()) {
                phones.add(cursor.getString(indexOfPhone));
            }
            cursor.close();
            getInstance(context).closeConnection();
            return phones;
        }

        public synchronized static void setRemoteAvatar(Context context, String number, @Nullable String avatar) {
            ContentValues values = new ContentValues();
            values.put(RAVATAR, TextUtils.isEmpty(avatar) ? null : avatar);
            values.put(USER, 1);
            getInstance(context).openConnection().update(TABLE_CONTACTS, values, NUMBER + " = ?", new String[]{number});
            getInstance(context).closeConnection();
        }

        public static int rangeInt(int min, int max) {
            return new Random().nextInt((max - min) + 1) + min;
        }

        public synchronized static List<Contact> getContacts(Context c, String selection, String[] arguments, int max) {
            List<Contact> contacts = new ArrayList<>();
            Cursor cursor = getInstance(c).openConnection().query(ContactBase.TABLE_CONTACTS, null, selection, arguments, null, null, null);
            if (cursor != null && cursor.getCount() > max) {
                for (int i = 0; i <= max; i++) {
                    int p = rangeInt(1, cursor.getCount() -1);
                    cursor.moveToPosition(p);
                    Contact contact = new Contact();
                    contact.name =  cursor.getString(cursor.getColumnIndex(ContactBase.NAME));
                    contact.number = cursor.getString(cursor.getColumnIndex(ContactBase.NUMBER));

                    String lAvatar, rAvatar;
                    lAvatar = cursor.getString(cursor.getColumnIndex(ContactBase.LAVATAR));
                    rAvatar = cursor.getString(cursor.getColumnIndex(ContactBase.RAVATAR));
                    contact.profileUri =  detectProfileUri(lAvatar,rAvatar);
                    contact.hasOpcon = cursor.getInt(cursor.getColumnIndex(ContactBase.USER)) == 1;
                    contacts.add(contact);
                    // add to cache
                    mContactCache.put(cursor.getColumnIndex(ContactBase.ID), contact);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            getInstance(c).closeConnection();
            return contacts;
        }

        public static void removeSelf(Context c) {
            getInstance(c).openConnection().delete(TABLE_CONTACTS, null, null);
            getInstance(c).closeConnection();
        }

        public synchronized static void nonUser(Context context, String phone) {
            if (context != null && phone !=null) {
                ContentValues values = new ContentValues();
                values.put(USER, 0);
                values.put(RAVATAR, "");
                getInstance(context).openConnection().update(TABLE_CONTACTS, values, NUMBER + "=?", new String[]{phone});
                getInstance(context).closeConnection();
            }
        }

        public static int getUserCount(Context c) {
            Cursor query = getInstance(c).openConnection().query(TABLE_CONTACTS, new String[]{USER}, USER + "=1", null, null, null, null);
            int count = 0;
            if (query != null) {
                count = query.getCount();
                query.close();
            }
            getInstance(c).closeConnection();
            return count;
        }
    }
}
