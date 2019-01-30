package com.opcon.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.opcon.R;
import com.opcon.components.Ack;
import com.opcon.components.Message;
import com.opcon.components.NotifierLog;
import com.opcon.database.MessageProvider;
import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.ComponentSender;
import com.opcon.firebaseclient.NotifierEventSentUtils;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.PicassoCompressor;
import com.opcon.libs.utils.AndroidEnvironmentsUtils;
import com.opcon.libs.utils.DateUtils;
import com.opcon.notifier.components.Notifier;
import com.opcon.services.ImageMessageUploader;
import com.opcon.ui.activities.ChatActivity;
import com.opcon.ui.activities.FullScreenImageViewerActivity;
import com.opcon.ui.fragments.occs.ConditionLocation;
import com.opcon.ui.management.DialogStoreManagement;
import com.opcon.ui.views.DialogView;
import com.opcon.ui.views.SpecialPacketView;
import com.vanniktech.emoji.EmojiTextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 *
 * Created by Mahmut Taşkiran on 12/11/2016.
 *
 */

public class MessageAdapter extends RecyclerView.Adapter {

    public static final int DATE_DIVIDER = -100;
    public static final int TIME_DIVIDER = -200;

    private int SELECTION_COLOR;
    private int RECEIVED_TEXT_MSG_BG;
    private int SENT_TEXT_MSG_BG;

    private List<Integer> mMessageIds;
    private MessageCacheMng mMessageCacheManager;
    private MessageSelectionManagement mSelectionManagement;

    public List<Integer> getMessageIds() {
        return mMessageIds;
    }

    public MessageAdapter(Context context, List<Integer> messagesIds) {
        RECEIVED_TEXT_MSG_BG = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        SENT_TEXT_MSG_BG = ContextCompat.getColor(context,R.color.lightGrey);
        SELECTION_COLOR = ContextCompat.getColor(context,R.color.selectedItemColor);

        this.mMessageIds = messagesIds;
        this.mMessageCacheManager = new MessageCacheMng(context, mMessageIds, 1024 * 1024);
        this.mSelectionManagement = new MessageSelectionManagement();
    }

    public MessageSelectionManagement getSelectionManagement() {
        return this.mSelectionManagement;
    }

    @Override
    public int getItemCount() {
        return mMessageIds.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BaseMessageHolder) {
            Message msg = getMessage(position);
            BaseMessageHolder baseMessageHolder = (BaseMessageHolder) holder;
            baseMessageHolder.forMessage(msg);
        } else if (holder instanceof TimeHolder) {
            Message msg = getNextNonnullMessage(position);
            if (msg != null)
                ((TimeHolder) holder).timestamp(msg.getSentTimestamp());
        } else if (holder instanceof DateHolder) {
            Message msg = getNextNonnullMessage(position);
                if (msg != null)
            ((DateHolder) holder).timestamp(msg.getSentTimestamp());
        }
    }

    private Message getNextNonnullMessage( int position) {
        for (int i = position; i < mMessageIds.size(); i++) {
            Message msg = getMessage(i);
            if (msg != null) {
                return msg;
            }
        }
        return null;
    }

    private Message getBackNonnullMessage(int position) {
        for (int i = position; i >= 0; i--) {
            Message msg = getMessage(i);
            if (msg != null)
                return msg;
        }
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        View v;
        LayoutInflater li = LayoutInflater.from(parent.getContext());
        if (viewType == TIME_DIVIDER) {
            v = li.inflate(R.layout.message_time, parent, false);
            vh = new TimeHolder(this, v);
        } else if (viewType == DATE_DIVIDER) {
            v = li.inflate(R.layout.message_date, parent, false);
            vh = new DateHolder(this, v);
        } else if (viewType == Message.TEXT) {
            v = li.inflate(R.layout.message_text, parent, false);
            vh = new TextMessageHolder(this, v);
        } else if (viewType == Message.PICTURE) {
            v = li.inflate(R.layout.message_image, parent, false);
            vh = new ImageMessageHolder(this, v);
        } else if (viewType == Message.LAST_CAPTURED_IMAGE) {
            v = li.inflate(R.layout.message_image, parent, false);
            vh = new ImageMessageHolder(this, v);
        } else if (Message.isSpecialPacket(viewType)) {
            v = li.inflate(R.layout.message_special_packet, parent, false);
            vh = new SpecialPacketHolder(this, v);
        }
        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        int msgId = mMessageIds.get(position);
        if (msgId == TIME_DIVIDER) {
            return TIME_DIVIDER;
        } else if (msgId == DATE_DIVIDER) {
            return DATE_DIVIDER;
        } else {
            return getMessage(position).getType();
        }
    }

    public Message getMessage(int position) {
        int uid = mMessageIds.get(position);
        if (uid == TIME_DIVIDER || uid == DATE_DIVIDER) {
            return null;
        }
        return mMessageCacheManager._get(uid);
    }

    public Message getMessageFromCache(int key) {
        return mMessageCacheManager._get(key);
    }

    public void addMessages(List<Message> messages) {
        Message lm = getBackNonnullMessage((mMessageIds.size() -1));
        long lastTimestamp = 0;
        if (lm != null) {
            lastTimestamp = lm.getSentTimestamp();
        }
        for (Message msg: messages) {
            long sent = msg.getSentTimestamp();
            boolean isDiffDay = DateUtils.isDifferentDays(lastTimestamp, sent);
            boolean isDiffTime = DateUtils.isTimeRangeLargerThan(sent, lastTimestamp, 3 * (1000 * 60));

            if (isDiffDay && isDiffTime) {
                mMessageIds.add(MessageAdapter.DATE_DIVIDER);
                mMessageIds.add(MessageAdapter.TIME_DIVIDER);
                lastTimestamp = sent;
            } else if (isDiffTime) {
                mMessageIds.add(MessageAdapter.TIME_DIVIDER);
                lastTimestamp = sent;
            } else if (isDiffDay) {
                mMessageIds.add(MessageAdapter.DATE_DIVIDER);
                lastTimestamp = sent;
            }

            mMessageIds.add(msg.getId());
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemInserted(mMessageIds.size());
                }
            });
            mMessageCacheManager.put(msg.getId(), msg);
        }
    }

    public void updateMessage(int id) {
        int position = 0;
        for (int i = 0; i < mMessageIds.size(); i++) {
            int mMessageId = mMessageIds.get(i);
            if (mMessageId == id) {
                position = i;
                break;
            }
        }
        final int f_position = position;
        mMessageCacheManager.remove(id);
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemChanged(f_position);
            }
        });
    }

    public void setMessageIds(List<Integer> messageIds) {
        this.mMessageIds = messageIds;
    }

    private static class BaseHolder extends RecyclerView.ViewHolder {
        MessageAdapter adapter;
        private BaseHolder(MessageAdapter adapterRef, View itemView) {
            super(itemView);
            this.adapter = adapterRef;
        }
        public MessageAdapter getAdapter() {
            return adapter;
        }
        public Context getContext() {
            return itemView.getContext();
        }
    }

    private static abstract class BaseMessageHolder extends BaseHolder implements View.OnClickListener, View.OnLongClickListener {

        private RelativeLayout mRoot;
        private TextView mDetails;
        private ImageView mAck;
        private TextView mWaiting;

        private BaseMessageHolder(MessageAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        void defineRoot() {
            if (this instanceof  SpecialPacketHolder) {
                SpecialPacketHolder holder = (SpecialPacketHolder) this;
                this.mRoot = holder.specialPacket.root;
                this.mDetails = holder.specialPacket.details;
                this.mAck = holder.specialPacket.ack_details;
            } else {
                this.mRoot = (RelativeLayout) itemView.findViewById(R.id.message_root_of_message);
                this.mDetails = (TextView) itemView.findViewById(R.id.message_details);
                this.mAck = (ImageView) itemView.findViewById(R.id.message_ack_detail);
            }
            if (!(this instanceof ImageMessageHolder)) {
                this.mWaiting = (TextView) itemView.findViewById(R.id.message_waiting_message);
            }
            this.mRoot.setOnClickListener(this);
            this.mRoot.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != -1) {
                Message msg = getAdapter().getMessage(getAdapterPosition());
                if (msg.isWaiting()) {
                    if (!msg.isImageMessage()) {
                        // sent message.
                        progressWaitingMessage(msg);
                        getAdapter().notifyItemChanged(getAdapterPosition());
                    }
                } else if (msg.getType() == Message.LOCATION && !getAdapter().mSelectionManagement.inSelectionState()) {
                    double latitude = msg.getDouble(Message.Location.LATITUDE);
                    double longitude = msg.getDouble(Message.Location.LONGITUDE);
                    ConditionLocation.showLocation(getContext(), latitude, longitude);
                } else {
                    getAdapter().mSelectionManagement.clickToggle(
                        msg.getId(),
                        msg.getType(),
                        getAdapterPosition()
                    );
                }
            }
        }

        void progressWaitingMessage(Message msg) {
            msg.setWaiting(false);
            if (msg.isImageMessage()) {
                ChatActivity.sendWaitingImageMessage(mRoot.getContext().getApplicationContext(), msg);
            } else {
                msg.send(getContext().getApplicationContext());
                MessageProvider.Utils.update(getContext(), msg);
            }
        }

        void removeWaitingMessage(Message msg) {
            MessageProvider.Utils.delete(getContext(), msg.getId());
            getAdapter().mMessageIds.remove(getAdapterPosition());
            getAdapter().notifyItemRemoved(getAdapterPosition());
            Notifier notifier = NotifierProvider.Utils.get(getContext(), msg.getRelationNotifier());
            if (notifier != null) {
                NotifierEventSentUtils.send(getContext(), notifier.getId(), NotifierLog.PACKET_DO_NOT_SENT);
            }
        }

        @Override public boolean onLongClick(View v) {
            if (getAdapterPosition() != -1) {
                Message msg = getAdapter().getMessage(getAdapterPosition());
                if (msg == null) return false;
                if (msg.isWaiting()) {
                    if (!msg.isImageMessage()) {
                        removeWaitingMessage(msg);
                    }
                } else {
                    getAdapter().mSelectionManagement.longClickToggle(
                        msg.getId(),
                        msg.getType(),
                        getAdapterPosition()
                    );
                }
            }
            return false;
        }


        private void toLeftSide() {
            mRoot.setGravity(Gravity.START);
        }

        private void toRightSide() {
            mRoot.setGravity(Gravity.END);
        }

        private void setSelectionState(boolean selectionState) {
            if (selectionState) {
                mRoot.setBackgroundColor(getAdapter().SELECTION_COLOR);
            } else {
                mRoot.setBackgroundColor(Color.WHITE);
            }
        }

        public void forMessage(Message message) {

            boolean amISender = message.isSenderAmI();
            // catch non-seen messages and set it as seen.
            if (!amISender && !message.isSeen() && PresenceManager.getInstance(getContext()).isActive()) {
                long seen = System.currentTimeMillis();
                message.setSeenTimestamp(seen);
                MessageProvider.Utils.update(getContext(), message);
                Ack ack = new Ack(message.getSid(),
                    System.currentTimeMillis(), Ack.SEEN);
                new ComponentSender("acks/" + message.getSender(), ack)
                    .sent();
                DialogStoreManagement.getInstance(getContext()).onAck(ack);
            }

            if (amISender) {
                toRightSide();
                boolean b = DialogView.setupAckStateFor(message, mAck);
                if (b) {
                    mAck.setVisibility(View.VISIBLE);
                } else {
                    mAck.setVisibility(View.INVISIBLE);
                }
            } else {
                toLeftSide();
                mAck.setVisibility(View.GONE);
            }

            if (message.isWaiting() && !message.isImageMessage()) {
                mRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightGrey));
                hideDetails();
                mWaiting.setVisibility(View.VISIBLE);
            } else {
                if (mWaiting != null) {
                    mWaiting.setVisibility(View.GONE);
                }
                if (getAdapter().mSelectionManagement.inSelectionState()) {
                    if (getAdapter().mSelectionManagement.isSelected(message.getId())) {
                        setSelectionState(true);
                        showDetails(message);
                    } else {
                        setSelectionState(false);
                        hideDetails();
                    }
                } else {
                    setSelectionState(false);
                    hideDetails();
                }
            }


        }

        private void showDetails(Message msg) {
            String details = getDetailsForMessage(msg);
            this.mDetails.setScaleY(0);
            this.mDetails.setVisibility(View.VISIBLE);
            ViewCompat.animate(mDetails).scaleY(1).setDuration(200).start();
            this.mDetails.setText(details);
        }

        private void hideDetails() {
            ViewCompat.animate(mDetails).scaleY(0).setDuration(100).withEndAction(new Runnable() {
                @Override
                public void run() {
                    mDetails.setVisibility(View.GONE);
                }
            }).start();
        }
    }

    public void shutdown() {
        mMessageCacheManager.evictAll();
        mMessageCacheManager.cancel();
        mMessageIds.clear();
    }

    private static String getDetailsForMessage(Message msg) {
        DateFormat dateFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        return dateFormat.format(new Date(msg.getSentTimestamp()));
    }

    private static class TextMessageHolder extends BaseMessageHolder {
        private CardView cardView;
        private TextView textView;
        private TextMessageHolder(MessageAdapter adapter, View itemView) {
            super(adapter, itemView);
            this.textView = (TextView) itemView.findViewById(R.id.message_text_tv);
            this.cardView = (CardView) itemView.findViewById(R.id.message_text_card);
            defineRoot();
        }
        @Override
        public void forMessage(Message message) {
            super.forMessage(message);
            textView.setText(message.getString(Message.Text.BODY));
            if (message.isSenderAmI()) {
                cardView.setCardBackgroundColor(getAdapter().SENT_TEXT_MSG_BG);
                textView.setTextColor(Color.BLACK);
            } else {
                cardView.setCardBackgroundColor(getAdapter().RECEIVED_TEXT_MSG_BG);
                textView.setTextColor(Color.WHITE);
            }
        }
    }

    private static class ImageMessageHolder extends BaseMessageHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private ImageView mImageView;
        private RelativeLayout mRootOfProcess;
        private ImageView mIconOfProces;

        private ImageButton mShare, mDelete;
        private LinearLayout mChoices;

        private EmojiTextView mAdditionText;

        private ImageMessageHolder(MessageAdapter adapter, final View itemView) {
            super(adapter, itemView);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.message_image_progress);
            mImageView = (ImageView) itemView.findViewById(R.id.message_image_iv);
            mRootOfProcess = (RelativeLayout) itemView.findViewById(R.id.message_image_root_process);
            mIconOfProces = (ImageView) itemView.findViewById(R.id.message_image_process_image);
            mChoices = (LinearLayout) itemView.findViewById(R.id.choices);
            mShare = (ImageButton) itemView.findViewById(R.id.share);
            mDelete = (ImageButton) itemView.findViewById(R.id.delete);
            mAdditionText = (EmojiTextView) itemView.findViewById(R.id.addition_text);

            mImageView.setOnClickListener(this);
            mImageView.setOnLongClickListener(this);
            mRootOfProcess.setOnClickListener(this);
            mShare.setOnClickListener(this);
            mDelete.setOnClickListener(this);
            defineRoot();

            if (mProgressBar.getIndeterminateDrawable() != null) {
                mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }

        }

        @Override
        public void forMessage(final Message message) {
            super.forMessage(message);

            gone(mRootOfProcess);

            if (message.isSenderAmI()) {
                // for upload
                picasso(Uri.fromFile(new File(message.getString(Message.Picture.FILE))));
                if (message.isWaiting()) {
                    visible(mChoices);
                    gone(mRootOfProcess);
                } else {
                    gone(mChoices);
                    if (message.getBoolean(Message.Picture.DONE)) {
                        gone(mRootOfProcess);
                    } else if (ImageMessageUploader.getInstance(getContext()).isUploading(message.getId())){
                        // in progress
                        visible(mRootOfProcess, mIconOfProces,mProgressBar);
                        mProgressBar.setIndeterminate(true);
                        setProgressIcon(R.drawable.ic_close_white_18dp);
                    } else {
                        // is failed. can be re-upload. with onclick.
                        visible(mRootOfProcess, mIconOfProces);
                        gone(mProgressBar);
                        setProgressIcon(R.drawable.ic_file_upload_white_18dp);
                    }
                }
            } else {
                visible(mRootOfProcess);
                setProgressIcon(R.drawable.ic_access_alarms_18_white);
                loadFromNetwork(message);
            }

            if (message.getType() == Message.LAST_CAPTURED_IMAGE) {
                if (!TextUtils.isEmpty(message.getString(Message.SpecialPacket.ADDITION_TEXT))) {
                    mAdditionText.setVisibility(View.VISIBLE);
                    mAdditionText.setText(message.getString(Message.SpecialPacket.ADDITION_TEXT));
                } else {
                    mAdditionText.setVisibility(View.GONE);
                }
            }
        }



        private void visible(View ... v) {
            for (View aV : v)
                aV.setVisibility(View.VISIBLE);
        }

        private void gone(View ... v) {
            for (View aV : v)
                aV.setVisibility(View.GONE);
        }

        private void setProgressIcon(@DrawableRes int resourceId) {
            mIconOfProces.setImageResource(resourceId);
        }

        private void picasso(Uri uri) {
            Glide.with(mProgressBar.getContext())
                .load(uri)
                .asBitmap()
                .fitCenter()
                .sizeMultiplier(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(R.drawable.no_item_background)
                .into(mImageView);
        }

        private void loadFromNetwork(final Message msg) {



            Glide.with(getContext())
                .load(msg.getString(Message.Picture.URL))
                .dontAnimate()
                .fitCenter()
                .sizeMultiplier(0.5f)
                //.bitmapTransform(new RoundedCornersTransformation(getContext(), 15, 15))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override public boolean onException(Exception e, String model,
                                                         Target<GlideDrawable> target, boolean isFirstResource)
                    {
                      String thumb = msg.getString(Message.Picture.THUMBNAIL);
                      if (thumb != null) {
                        picasso(Uri.fromFile(new File(thumb)));
                        visible(mRootOfProcess, mIconOfProces);
                        gone(mProgressBar);
                        setProgressIcon(R.drawable.ic_file_download_white_18dp);
                      }
                      return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource)
                    {
                        gone(mRootOfProcess);
                        msg.put(Message.Picture.DONE, true);
                        return false;
                    }
                })
                .into(mImageView);

        }

        @Override public void onClick(View v) {


            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                return;
            }

            Message msg = getAdapter().getMessage(getAdapterPosition());
            if (getAdapter().mSelectionManagement.inSelectionState()) {
                getAdapter().mSelectionManagement.clickToggle(msg.getId(), Message.PICTURE, getAdapterPosition());
                return;
            }

            if (msg == null)
                return;

            if (msg.isWaiting()) {

                if (v == mDelete) {
                    removeWaitingMessage(msg);
                } else if (v == mShare) {
                    progressWaitingMessage(msg);
                    getAdapter().notifyItemChanged(getAdapterPosition());
                } else if (v == mImageView) {
                    FullScreenImageViewerActivity.show(getContext(), getImageUriFor(msg));
                }
            } else {
                if (v == mRootOfProcess) {


                    if (msg.isSenderAmI()) {
                        if (!ImageMessageUploader.getInstance(getContext()).isUploading(msg.getId())) {
                            if (AndroidEnvironmentsUtils.hasActiveInternetConnection(getContext())) {
                                msg.put(Message.Picture.FAILED, false);
                                msg.put(Message.Picture.DONE, false);
                                ImageMessageUploader.getInstance(getContext()).start(msg);
                            } else {
                                Toast.makeText(getContext(), R.string.not_internet_connection, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            ImageMessageUploader.getInstance(getContext()).stop(msg.getId());
                            msg.put(Message.Picture.FAILED, true);
                            getAdapter().notifyItemChanged(getAdapterPosition());
                        }

                    } else {
                        loadFromNetwork(msg);
                    }
                } else if (v == mImageView) {

                    if (!getAdapter().mSelectionManagement.inSelectionState()) {
                        Uri uri = getImageUriFor(msg);
                        if (uri != null) {
                            FullScreenImageViewerActivity.show(getContext(), uri);
                        }
                    } else {
                        getAdapter().mSelectionManagement.clickToggle(
                            msg.getId(),
                            msg.getType(),
                            getAdapterPosition()
                        );
                    }

                }
            }


        }
    }

    static Uri getImageUriFor(Message msg) {
        Uri uri;
        if (msg.isSenderAmI()) {
            uri = Uri.fromFile(new File(msg.getString(Message.Picture.FILE)));
        } else {
            if (msg.getBoolean(Message.Picture.DONE)) {
                uri = Uri.parse(msg.getString(Message.Picture.URL));
            } else {
                uri = Uri.fromFile(new File(msg.getString(Message.Picture.THUMBNAIL)));
            }
        }
        return uri;
    }

    private static class SpecialPacketHolder extends BaseMessageHolder implements View.OnClickListener{
        private SpecialPacketView specialPacket;
        private SpecialPacketHolder(MessageAdapter adapter, View itemView) {
            super(adapter, itemView);
            this.specialPacket = (SpecialPacketView) itemView.findViewById(R.id.message_sp_sp);
            defineRoot();
        }
        @Override public void forMessage(Message message) {
            super.forMessage(message);
            this.specialPacket.setMessage(message);
            this.specialPacket.forAdapter();
        }

    }

    private static class TimeHolder extends BaseHolder {
        TextView timeView;
        private TimeHolder(MessageAdapter adapterRef, View itemView) {
            super(adapterRef, itemView);
            timeView = (TextView) itemView.findViewById(R.id.message_time);
        }
        @SuppressLint("SimpleDateFormat")
        public void timestamp(long timestamp) {
            long halfHourAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(35);
            if (halfHourAgo < timestamp) {
                timeView.setText( new PrettyTime().format(new Date(timestamp)));
            } else {
                timeView.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(timestamp)));
            }
        }
    }

    private static class DateHolder extends BaseHolder {
        TextView messageDate;
        private DateHolder(MessageAdapter adapterRef, View itemView) {
            super(adapterRef, itemView);
            messageDate = (TextView) itemView.findViewById(R.id.message_date_date);
        }

        public void timestamp(long timestamp) {
            DateFormat simpleDateFormat = SimpleDateFormat.getDateInstance();
            String formattedTime = simpleDateFormat.format(new Date(timestamp));
            messageDate.setText(formattedTime);
        }
    }

}
