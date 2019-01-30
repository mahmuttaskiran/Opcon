package com.opcon.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.opcon.R;
import com.opcon.components.Contact;
import com.opcon.database.ContactBase;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.ui.adapters.NewContactAdapter;
import com.opcon.ui.dialogs.NonUserInviteDialog;
import com.opcon.ui.fragments.ContactFragment;
import com.opcon.ui.views.CircleRelativeLayout;

import butterknife.ButterKnife;
import butterknife.BindView;
import timber.log.Timber;

public class ContactsActivity extends AppCompatActivity implements NewContactAdapter.ContactAdapterClickHandler, TextWatcher {

    public static final String SELECTED_CONTACT_NAME = "scN";
    public static final String SELECTED_CONTACT_NUMBER = "scNumber";
    public static final String ONLY_USER = "ou";
    public static final String NUMBER_ENTERABLE = "ne";
    public static final String GO_PROFILE = "goProfile";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.editText)
    EditText mEtSearch;

    @BindView(R.id.directEnterParentCard)
    CardView mDirectEnterParentCard;

    @BindView(R.id.directEnterInput)
    EditText mDirectEnterInput;

    @BindView(R.id.directEnterOk)
    CircleRelativeLayout mDirectEnterOk;


    private ContactFragment mContactFragment;

    private boolean mNumberEnterable;
    private boolean mSearchMode;

    private MenuItem miClose;
    NonUserInviteDialog mInviteDialog;

    private boolean goProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_contacts);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(5);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_back_white_18);
            drawable.setColorFilter(getResources().getColor(R.color.searchContentColor), PorterDuff.Mode.MULTIPLY);
            getSupportActionBar().setHomeAsUpIndicator(drawable);
        }

        if (getIntent().getExtras() != null) {
            goProfile = getIntent().getExtras().getBoolean(GO_PROFILE);
        }


        this.mNumberEnterable = getIntent().getBooleanExtra(NUMBER_ENTERABLE, false);

        mEtSearch.setHint(R.string.search);


        if (mNumberEnterable) {

            mDirectEnterParentCard.setVisibility(View.VISIBLE);

            mDirectEnterOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterableOK();
                }
            });

        } else {

            mDirectEnterParentCard.setVisibility(View.GONE);

        }

        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        mContactFragment = new ContactFragment();
        mContactFragment.setHasOptionsMenu(false);
        mContactFragment.setContactClickHandler(this);

        mContactFragment.mOnlyUser = getIntent().getBooleanExtra(ONLY_USER, false);

        fragmentTransaction.add(R.id.container, mContactFragment);
        fragmentTransaction.commit();

        mEtSearch.addTextChangedListener(this);
        checkPermissions();
    }

    private void visibilityOfEnterable() {
        if (mNumberEnterable && !mSearchMode) {
            mDirectEnterParentCard.setVisibility(View.VISIBLE);
        } else {
            mDirectEnterParentCard.setVisibility(View.GONE);
        }
    }

    private void checkPermissions() {
        boolean check = PermissionUtils.check(getApplicationContext(), Manifest.permission.READ_CONTACTS);
        if (!check) {
            mToolbar.setVisibility(View.GONE);
        } else {
            mToolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // ignore
    }

    void setSearchMode(boolean mode) {
        if (miClose == null)
            return;

        if (mode) {
            miClose.setVisible(true);
        } else {
            miClose.setVisible(false);
        }
        mSearchMode =  mode;
        visibilityOfEnterable();
    }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override public void afterTextChanged(Editable s) {

        String willSearch = mEtSearch.getText().toString();

        if (s == null || s.length() == 0) {
            mContactFragment.setFilter(null);
            setSearchMode(false);
            return;
        }

        if (willSearch.length() == 0) {
            mContactFragment.setFilter(null);
            setSearchMode(false);
        } else {
            mContactFragment.hideDoNotFind();
            mContactFragment.setFilter(willSearch);
            setSearchMode(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_contact, menu);
        this.miClose = menu.findItem(R.id.menu_close);

        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item!=null) {
                Drawable icon = item.getIcon();
                if (icon != null) {
                    icon.setColorFilter(getResources().getColor(R.color.searchContentColor), PorterDuff.Mode.MULTIPLY);
                }
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    void enterableOK() {
        String text = mDirectEnterInput.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            Intent result = new Intent();
            result.putExtra(NUMBER_ENTERABLE, text);
            result.putExtra(SELECTED_CONTACT_NUMBER, text);
            result.putExtra(SELECTED_CONTACT_NAME, ContactBase.Utils.getPureName(this, text));
            setResult(RESULT_OK, result);
            finish();
        } else {
            Toast.makeText(this, R.string.please_enter_an_phone_number, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
        } else if (item.getItemId() == R.id.menu_close) {
            mEtSearch.setText(null);
            setSearchMode(false);
        } else if (item.getItemId() == R.id.refresh) {
            mContactFragment.updateContactsAsync(true, true);
        } else if (item.getItemId() == R.id.addANewUser) {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onAvatarClick(int id) {
       goProfile(id);
    }

    @Override
    public void onContactClick(int id) {
        if (goProfile) {
            goProfile(id);
        } else {
            Contact contact = ContactBase.Utils.getContactFull(getApplicationContext(), id);
            if ((contact.hasOpcon && mContactFragment.mOnlyUser) || !mContactFragment.mOnlyUser) {
                Intent result = new Intent();
                result.putExtra(SELECTED_CONTACT_NAME, contact.name);
                result.putExtra(SELECTED_CONTACT_NUMBER, contact.number);
                setResult(RESULT_OK, result);
                finish();
            } else {
                mInviteDialog = new NonUserInviteDialog(ContactsActivity.this, contact.number);
                mInviteDialog.show();
            }

        }
    }

    void goProfile(int id) {
        Contact contact = ContactBase.Utils.getContactFull(getBaseContext(), id);
        if (contact.hasOpcon) {
            ProfileActivity.profile(ContactsActivity.this, contact.number);
            finish();
        } else {
            if (mInviteDialog == null || !mInviteDialog.isShowing()) {
                mInviteDialog = new NonUserInviteDialog(ContactsActivity.this, contact.number);
                mInviteDialog.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mSearchMode) {
            mEtSearch.setText("");
        } else {
            finish();
        }
    }

    @Override
    public void onGavelClick(int id) {}

    @Override
    public void onRefreshRequest() {
        // ignore
    }

    public static void goResult(Activity context, boolean onlyUser, int requestCode) {
        Intent intent = new Intent(context, ContactsActivity.class);
        intent.putExtra(ONLY_USER, onlyUser);
        context.startActivityForResult(intent,requestCode);
    }

    public static void goResultForMainActivity(Activity context, boolean onlyUser, int requestCode) {
        Intent intent = new Intent(context, ContactsActivity.class);
        intent.putExtra(ONLY_USER, onlyUser);

        context.startActivityForResult(intent,requestCode);
    }

    public static void goProfile(Context context) {
        Intent i = new Intent(context, ContactsActivity.class);
        i.putExtra(ContactsActivity.GO_PROFILE, true);

        context.startActivity(i);
    }

}
