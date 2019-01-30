package com.opcon.ui.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.components.Message;
import com.opcon.database.ContactBase;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.utils.SpecialPacketUtils;
import com.opcon.ui.utils.NotifierConstantUtils;
import com.opcon.ui.utils.SpecialMessageBodyInterpreter;
import com.vanniktech.emoji.EmojiTextView;

import agency.tango.android.avatarview.views.AvatarView;

/**
 * Created by Mahmut Taşkiran on 24/11/2016.
 */

public class SpecialPacketView extends RelativeLayout {

    public RelativeLayout root;

    public EmojiTextView description;
    public AvatarView circleImageView;
    public CardView rootCardView;
    public ImageView ack_details;
    public TextView details;
    public ImageView mIcon;

    public SpecialPacketView(Context c) {
        super(c);
        init();
    }

    public SpecialPacketView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.special_packet_view, this, true);
        this.mIcon = (ImageView) itemView.findViewById(R.id.message_sp_icon);
        this.description = (EmojiTextView) itemView.findViewById(R.id.message_sp_body);
        this.circleImageView = (AvatarView) itemView.findViewById(R.id.message_sp_avatar);
        this.root = (RelativeLayout) itemView.findViewById(R.id.message_root_of_message);
        this.rootCardView = (CardView) itemView.findViewById(R.id.message_sp_root_cardview);
        this.details = (TextView) itemView.findViewById(R.id.message_details);
        this.ack_details = (ImageView) itemView.findViewById(R.id.message_ack_detail);

    }

    public void setMessage(Message message) {
        int icon = NotifierConstantUtils.getSpecialPacketGreyIcon(SpecialPacketUtils.getPacketTypeOfMessageType(message.getType()));



        mIcon.setImageResource(icon);

        this.description.setText(Html.fromHtml(SpecialMessageBodyInterpreter.interpret(message, getContext())));


        String avatar;

        if (message.isSenderAmI()) {
            avatar = PresenceManager.getAvatar(getContext());
        } else {
            avatar = ContactBase.Utils.getValidAvatar(getContext(), message.getSender());
        }

        AvatarLoader.load(circleImageView, avatar, message.getSenderName(getContext()));
    }




    public void forAdapter() {
        int dpToPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                240, getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(dpToPx,
                CardView.LayoutParams.WRAP_CONTENT);
        rootCardView.setLayoutParams(newParams);
    }
}
