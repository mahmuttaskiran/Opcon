package com.opcon.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.opcon.R;
import com.opcon.components.Contact;
import com.opcon.database.ContactBase;
import com.opcon.ui.drawables.RandomIconsDrawable;

/**
 *
 * Created by Mahmut Taşkiran on 28/12/2016.
 */

public class NonUserInviteDialog extends AppCompatDialog implements View.OnClickListener, DialogInterface.OnCancelListener{

    private AppCompatButton mInvite;
    private ImageView mImageView;
    private RandomIconsDrawable mRandomIconsDrawable;
    private TextView mUserName;

    private String mDestination;
    private String mAvatar;

    public NonUserInviteDialog(Context context, String destination) {
        super(context);
        this.mDestination = destination;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.non_user_invite_fragment);
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams attributes =
                window.getAttributes();
            attributes.width = ViewGroup.LayoutParams.MATCH_PARENT;
            window.setAttributes(attributes);
        }

        mImageView = (ImageView) findViewById(R.id.imageView);
        mInvite = (AppCompatButton) findViewById(R.id.button);
        mUserName = (TextView) findViewById(R.id.nameOfUser);

        mUserName.setText(ContactBase.Utils.getName(getContext(), mDestination));

        mInvite.setOnClickListener(this);

        mAvatar = ContactBase.Utils.getValidAvatar(getContext(), mDestination);

        RandomIconsDrawable.Builder mDrawableBuilder =
                new RandomIconsDrawable.Builder();
        mDrawableBuilder.setResources(getContext()
                .getResources())
                .setBackgroundColor(getContext()
                        .getResources()
                        .getColor(R.color.colorPrimary))
                .setIconColor(getContext()
                        .getResources()
                        .getColor(R.color.colorPrimaryDark))
                .setMaxScale(1)
                .setMinScale(1)
                .setMax(10);

        mRandomIconsDrawable = mDrawableBuilder.built();
        mImageView.setBackgroundDrawable(mRandomIconsDrawable);

        Glide.with(getContext())
            .load(mAvatar)
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .centerCrop()
            .into(mImageView);

        Contact contact = ContactBase.Utils.getContact(getContext(), mDestination);
        if (contact.hasOpcon) {
            mInvite.setVisibility(View.GONE);
        }

        setOnCancelListener(this);
    }


  @Override
    public void onClick(View v) {
        goToSmsApplication();
        dismiss();
    }

    private void goToSmsApplication() {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:"));
        sendIntent.putExtra("sms_body", getContext().getResources().getString(R.string.invitation_message));
        getContext().startActivity(sendIntent);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mRandomIconsDrawable != null)
            mRandomIconsDrawable.recycleBitmaps();
    }
}
