package com.opcon.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.components.Dialog;
import com.opcon.components.Message;
import com.opcon.database.MessageProvider;
import com.vanniktech.emoji.EmojiTextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import agency.tango.android.avatarview.views.AvatarView;


/**
 * Created by Mahmut Taşkiran on 28/11/2016.
 */

public class DialogView extends RelativeLayout {

    private EmojiTextView mName;
    private TextView mDate;
    private ImageView mAckState;
    public AvatarView mAvatar;

    private TextView mNonSeenLength;

    private int mNonSeenMessageLength;

    private static int DIALOG_TITLE_COLOR;
    private static int DIALOG_CONTENT_COLOR;

    public DialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.dialog_view, this, true);
        this.mNonSeenLength = (TextView) root.findViewById(R.id.dialog_non_seen_length);
        this.mName = (EmojiTextView) root.findViewById(R.id.dialog_text);
        this.mDate = (TextView) root.findViewById(R.id.dialog_date);
        this.mAckState = (ImageView) root.findViewById(R.id.dialog_ack_state);
        this.mAvatar = (AvatarView) root.findViewById(R.id.dialog_avatar);

        DIALOG_CONTENT_COLOR = root.getResources().getColor(R.color.dialogContentColor);
        DIALOG_TITLE_COLOR = root.getResources().getColor(R.color.dialogTitleColor);

    }

    public void forDialog(Dialog dialog) {

        String dialogLastStr = dialog.content != null ? dialog.content:
            MessageProvider.DialogUtils.getDialogLastStr(getContext(),
                dialog.lastNotifier, dialog.lastMessage);

        if (dialog.lastMessage != null && dialog.lastMessage.isWaiting()) {
            this.mName.setText(Html.fromHtml(String.format("<font color='" + DIALOG_TITLE_COLOR + "'>%s</font><br><font color='black'>%s</font>", dialog.name, getContext().getString(R.string.there_are_waiting_message))));
        } else {
            if (TextUtils.isEmpty(dialogLastStr)) {
                this.mName.setText(Html.fromHtml(String.format("<font color='" + DIALOG_TITLE_COLOR + "'>%s</font>", dialog.name)));
            } else {
                this.mName.setText(Html.fromHtml(String.format("<font color='" + DIALOG_TITLE_COLOR + "'>%s</font><br><font color='" + DIALOG_CONTENT_COLOR + "'>%s</font>", dialog.name, dialogLastStr)));
            }
        }

        AvatarLoader.load(mAvatar, dialog.avatarPath, dialog.name);
        mAckState.setVisibility(GONE);

        if (dialog.lastMessage != null && dialog.lastMessage.isSenderAmI()) {
            boolean b = setupAckStateFor(dialog.lastMessage, mAckState);
            if (b){
                mAckState.setVisibility(VISIBLE);
            }
        }

        if (dialog.getLastTime() > 1 && !dialog.isAssistant()) {
            mDate.setVisibility(VISIBLE);
            mDate.setText(SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(dialog.getLastTime())));
        } else {
            mDate.setVisibility(GONE);
        }
        mNonSeenMessageLength = dialog.nonSeenMessageLength;
        decideNonSeenVisibility();

    }

    private void decideNonSeenVisibility() {
        if (mNonSeenMessageLength > 0) {
            mNonSeenLength.setVisibility(VISIBLE);
            mNonSeenLength.setText(String.valueOf(mNonSeenMessageLength));
        } else {
            mNonSeenLength.setVisibility(GONE);
        }
    }


    public void setName(Spanned name) {
        this.mName.setText(name);
    }

    // imageView'in varsayılan olarak hiçbir görsel
    // öğe taşımadığını varsayarak, server'a iletilmiş iletiler
    // boş mavi işaretlenir; henüz server'a iletilmemiş iletiler
    // bir görsel öğe belirtmez; kullanıcıya iletişler mavi icon ve
    // mavi kenarlıkla belirtilir. Görülen iletilerse
    // mavi dolgu ve beyaz ikonla gösterilir.

    public static void color (Drawable ic, int color) {
        ic.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public static boolean setupAckStateFor(Message msg, ImageView imageView) {

        Resources res = imageView.getContext().getResources();
        Drawable bg = null, ic = null;

        if (msg.isWaiting()) {
            bg = res.getDrawable(R.drawable.white_bg_blue_border);
            ic = res.getDrawable(R.drawable.ic_question);
            color(ic, res.getColor(R.color.colorPrimary));
        } else if (msg.isImageMessage() && !msg.getBoolean(Message.Picture.DONE)) {
            ic = null;
            bg = null;
        } else {

            if (msg.isSeen()) {
                ic = res.getDrawable(R.drawable.ic_check_18_white);
                bg = imageView.getContext().getResources().getDrawable(R.drawable.blue_circle);
            } else if (msg.isReceived()) {
                ic = res.getDrawable(R.drawable.ic_check_primary);
                bg = imageView.getContext().getResources().getDrawable(R.drawable.white_bg_blue_border);
            } else if (msg.isSent()) {
                ic = res.getDrawable(R.drawable.ic_check_primary);
                bg = imageView.getContext().getResources().getDrawable(R.drawable.circle_white);
            }

        }

        if (ic != null) {
            imageView.setImageDrawable(ic);
            imageView.setBackgroundDrawable(bg);
            return true;
        } else {
            imageView.setBackgroundDrawable(null);
            imageView.setImageDrawable(null);
            return false;
        }

    }

}
