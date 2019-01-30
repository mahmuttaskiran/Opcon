package com.opcon.ui.views;

import android.content.Context;
import android.graphics.Color;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.components.Component;
import com.opcon.libs.utils.TimeUtils;
import com.opcon.notifier.components.Notifier;
import com.opcon.ui.utils.NotifierConstantUtils;
import com.opcon.ui.utils.Restrict;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import agency.tango.android.avatarview.views.AvatarView;

/**
 *
 * Created by Mahmut Taşkiran on 14/01/2017.
 */

public class NotifierView extends RelativeLayout {

    private static final String TITLE_FORMAT = "<b>%s</b><br><b><i>%s, </i></b>%s";

    public CardView mCardView;
    public AvatarView mAvatar;
    public AvatarView mConditionAvatar, mOperationAvatar;
    public TextView mTitle, mConditionDesc, mOperationDesc, mNotification;
    public ImageView mConditionIcon, mOperationIcon, mOptions;
    public RelativeLayout mRootOfInfo;
    public LinearLayout mRestrictRoot;
    public CardView mRootDateRestrict, mRootTimeRangeRestrict;
    public TextView mDateRestrictText, mTimeRangeRestrictText;

    public CardView mNoteCard;
    public TextView mNote;

    public boolean editable;

    public CircleRelativeLayout mConditionCircle, mOperationCircle;

    public final int dipToPx4;

    Notifier notifier;

    public NotifierView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate();
        setClickable(true);
        dipToPx4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getContext().getResources().getDisplayMetrics());
    }

    public NotifierView(Context context) {
        super(context);
        dipToPx4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getContext().getResources().getDisplayMetrics());
        inflate();
    }

    private void inflate() {
        LayoutInflater li = LayoutInflater.from(getContext());
        bindViews(li.inflate(R.layout.notifierview, this, true));
    }

    private void bindViews(View inflatedView) {
        mAvatar = (AvatarView) inflatedView.findViewById(R.id.avatar);
        mTitle = (TextView) inflatedView.findViewById(R.id.desc);
        mConditionDesc = (TextView) inflatedView.findViewById(R.id.conditionTitle);
        mOperationDesc = (TextView) inflatedView.findViewById(R.id.operationTitle);

        mConditionIcon = (ImageView) inflatedView.findViewById(R.id.conditionIcon);
        mOperationIcon = (ImageView) inflatedView.findViewById(R.id.operationIcon);
        mNotification = (TextView) inflatedView.findViewById(R.id.notificationIcon);
        mCardView = (CardView) inflatedView.findViewById(R.id.rootCard);
        mOptions = (ImageView) inflatedView.findViewById(R.id.options);
        mOperationAvatar = (AvatarView) inflatedView.findViewById(R.id.operationAvatar);
        mConditionAvatar = (AvatarView) inflatedView.findViewById(R.id.conditionAvatar);
        mRootOfInfo = (RelativeLayout) inflatedView.findViewById(R.id.layout);
        mRestrictRoot = (LinearLayout) inflatedView.findViewById(R.id.restrict);
        mDateRestrictText = (TextView) inflatedView.findViewById(R.id.dateRestrictText);
        mTimeRangeRestrictText = (TextView) inflatedView.findViewById(R.id.timeRestrictText);
        mRootDateRestrict = (CardView) inflatedView.findViewById(R.id.dateRestrict);
        mRootTimeRangeRestrict = (CardView) inflatedView.findViewById(R.id.timeRestrict);

        mOperationCircle = (CircleRelativeLayout) inflatedView.findViewById(R.id.operationIconCircle);
        mConditionCircle = (CircleRelativeLayout) inflatedView.findViewById(R.id.conditionIconCircle);

        mNote = (TextView) inflatedView.findViewById(R.id.notifier_note);
        mNoteCard = (CardView) inflatedView.findViewById(R.id.notifier_note_card);

    }

    public static int getStateColor(Context c, int r_state) {
        switch (r_state) {
            case Notifier.DELETED:
                return gc(c, R.color.red_ff1744);
            case Notifier.STOPPED:
            case Notifier.NOT_DETERMINED:
                return gc(c, R.color.colorSecondary);
            case Notifier.RUNNING:
                return gc(c, R.color.colorPrimary);
        }
        return gc(c, R.color.colorSecondary);
    }

    public static int gc(Context c, @ColorRes int ci) {
        return c.getResources().getColor(ci);
    }

    public void with(Notifier rc) {

        this.notifier = rc;

        AvatarLoader.load(mAvatar, rc.getOwnerAvatar(getContext()), rc.getOwnerName(getContext()));
        AvatarLoader.load(mConditionAvatar, rc.getConditionProcessorAvatar(getContext()), rc.getConditionProcessorName(getContext()));
        AvatarLoader.load(mOperationAvatar, rc.getOperationProcessorAvatar(getContext()), rc.getOperationProcessorName(getContext()));

        mConditionCircle.setColor(getStateColor(getContext(), rc.getConditionProcessorState()));
        mOperationCircle.setColor(getStateColor(getContext(), rc.getOperationProcessorState()));

        String time = TimeUtils.justTimeIsSomeDay(rc.getTimestamp(), TimeUnit.DAYS.toMillis(1));

        String desc = rc.getDescription();
        if (desc == null)
            desc = "";

        this.mTitle.setText(Html.fromHtml(!desc.isEmpty() ? String.format(TITLE_FORMAT, rc.getOwnerName(mTitle.getContext()), time, desc): String.format("<b>%s</b><br><b>%s</b>", rc.getOwnerName(getContext()), time)));
        this.mConditionDesc.setText(Html.fromHtml(rc.getConditionDescription(getContext())));
        this.mOperationDesc.setText(Html.fromHtml(rc.getOperationDescription(getContext())));

        this.mConditionIcon.setImageResource((NotifierConstantUtils.getConditionIcon18dp(rc.getCondition().getId())));
        this.mOperationIcon.setImageResource((NotifierConstantUtils.getOperationIcon18dp(rc.getOperation().getId())));

        if (rc.getNonSeenNotificationLength(getContext()) > 0) {
            mNotification.setVisibility(VISIBLE);
            mNotification.setText(String.valueOf(rc.getNonSeenNotificationLength(getContext())));
        } else {
            mNotification.setVisibility(GONE);
        }

        hideNote();

        if (!rc.isProfileUpdater()) {
            if (rc.getRelationshipState() == Notifier.DELETED) {
                noteThat(R.string.this_notifier_is_deleted_on_target, gc(getContext(), R.color.red_ff1744));
            } else if (rc.getState() == Notifier.NOT_DETERMINED  && rc.anyProgressable()) {
                noteThat(R.string.this_notifier_is_not_active, gc(getContext(), R.color.colorSecondary));
            } else if (rc.getRelationshipState() == Notifier.NOT_DETERMINED && !rc.bothProgressable()) {
                noteThat(R.string.this_notifier_is_not_active_on_target, gc(getContext(), R.color.colorSecondary));
            }
        }

        setRestrictDetails(rc);
        editionStyle();

        if (editable ){
            mTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
        } else {
            if (rc.isNotificationOn(getContext())) {
                mTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_notifications_active_grey),null,null,null);
            } else {
                mTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_notifications_off_grey),null,null,null);
            }
        }


        if (!editable) {
            mTitle.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    if (getContext() != null) {
                        if (notifier.isNotificationOn(getContext())) {
                            Notifier.setNotificationState(getContext(), notifier.getId(), false);
                            Toast.makeText(getContext(), R.string.notification_closed_for_notifier, Toast.LENGTH_SHORT).show();
                            mTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_notifications_off_grey),null,null,null);
                        } else {
                            Notifier.setNotificationState(getContext(), notifier.getId(), true);
                            Toast.makeText(getContext(), R.string.notification_opened_for_notifier, Toast.LENGTH_SHORT).show();
                            mTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_notifications_active_grey),null,null,null);
                        }

                    }


                }
            });
        }

    }

    void noteThat(@StringRes int resourceId, int color) {
        mNoteCard.setVisibility(VISIBLE);
        //mNoteCard.setCardBackgroundColor(color);
        mNote.setText(Html.fromHtml(getContext().getString(resourceId)));
    }

    public void hideNote() {
        mNoteCard.setVisibility(GONE);
    }

    void setRestrictDetails(Notifier r) {
        if (r.getCondition().isRestricted()) {
            mRestrictRoot.setVisibility(VISIBLE);
            Component dr = r.getCondition().getDateRestrictParams();
            Component trr = r.getCondition().getTimeRangeRestrictParams();



            if (dr != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, dr.getInt(Restrict.YEAR));
                calendar.set(Calendar.MONTH, dr.getInt(Restrict.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH, dr.getInt(Restrict.DAY));
                String date = SimpleDateFormat.getDateInstance().format(new Date(calendar.getTimeInMillis()));
                mRootDateRestrict.setVisibility(VISIBLE);
                mDateRestrictText.setText(date);
            } else {
                mRootDateRestrict.setVisibility(GONE);
            }

            if (trr != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, trr.getInt(Restrict.FROM_HOURS));
                calendar.set(Calendar.MINUTE, trr.getInt(Restrict.FROM_MINUTES));
                String from = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT ).format(new Date(calendar.getTimeInMillis()));

                Calendar calendarTo = Calendar.getInstance();
                calendarTo.set(Calendar.HOUR_OF_DAY, trr.getInt(Restrict.TO_HOURS));
                calendarTo.set(Calendar.MINUTE, trr.getInt(Restrict.TO_MINUTES));
                String to = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(new Date(calendarTo.getTimeInMillis()));
                mTimeRangeRestrictText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_timelapse, 0,0,0);
                mRootTimeRangeRestrict.setVisibility(VISIBLE);
                mTimeRangeRestrictText.setText(from + "/" + to);
            } else {
                mRootTimeRangeRestrict.setVisibility(GONE);
            }
        } else {
            if (!r.isProfileUpdater() && r.anyProgressable()) {
                mTimeRangeRestrictText.setText(R.string.there_is_no_any_time_restrict);
                mTimeRangeRestrictText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_android_hand, 0,0,0);
                mRootTimeRangeRestrict.setVisibility(VISIBLE);
                mRestrictRoot.setVisibility(VISIBLE);
            } else {
                mRootTimeRangeRestrict.setVisibility(GONE);
                mRestrictRoot.setVisibility(GONE);
            }

            if (r.getCondition().getDateRestrictParams() == null) {
                mRootDateRestrict.setVisibility(GONE);
            }
        }
    }

    private void setState(@ColorRes int color, @ColorRes int textColor, @DrawableRes int moreIcon) {
        mRootOfInfo.setBackgroundColor(getContext().getResources().getColor(color));
        mTitle.setTextColor(getContext().getResources().getColor(textColor));
        mOptions.setImageResource(moreIcon);
    }


    public void makeEditable() {
        editable = true;
        mTitle.setText("");
        mTitle.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mTitle.setHint(R.string.please_enter_a_comment_for_notifier);
        mTitle.setEnabled(true);
        mTitle.setTextSize(18);
        mTitle.setFocusable(true);
        mTitle.setFocusableInTouchMode(true);
        mTitle.setClickable(true);
        hideNote();
        mTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
    }

    public String getDesc() {
        return mTitle.getText().toString();
    }

    public void hideNotification() {
        this.mNotification.setVisibility(GONE);
    }

    public void hideRightSide() {
        this.mNotification.setVisibility(GONE);
        this.mOptions.setVisibility(GONE);
    }

    public void editionStyle() {
        setState(R.color.white, R.color.strongGrey, R.drawable.ic_more_vert_black_18dp);
        mTitle.setHintTextColor(getContext().getResources().getColor(R.color.strongGrey));
    }

    public void fullView() {
        //mCardView.setUseCompatPadding(false);
        mCardView.setCardElevation(0);
        mCardView.setRadius(0);
        mCardView.setCardBackgroundColor(Color.WHITE);
    }

    public void hideOptions() {
        mOptions.setVisibility(INVISIBLE);
    }
}
