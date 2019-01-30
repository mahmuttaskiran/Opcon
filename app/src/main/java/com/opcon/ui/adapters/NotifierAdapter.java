package com.opcon.ui.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.opcon.R;
import com.opcon.components.Helper;
import com.opcon.ui.fragments.NotifierFragment;
import com.opcon.ui.views.HelperHolder;
import com.opcon.ui.views.NotifierView;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Mahmut Taşkiran on 05/12/2016.
 */

public class NotifierAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int NOTIFIER = 309;
    private static final int HELPER = 943;

    public interface NotifierViewEventHandler {
        void onNotificationClick(int id);
        void onAvatarClick(int id);
        void onShowOptions(int adapterPosition, int id);
    }

    private NotifierFragment.NotifierCache notifierCacheCache;
    private ArrayList<Object> ids;
    private NotifierViewEventHandler notifierViewEventHandler;
    private List<com.opcon.notifier.components.Notifier> directStore;



    public NotifierAdapter(ArrayList<Object> ids, @Nullable List<com.opcon.notifier.components.Notifier> directStore, @Nullable NotifierViewEventHandler eventHandler) {
        this.ids = ids;
        this.directStore = directStore;
        this.notifierViewEventHandler = eventHandler;
        this.notifierCacheCache = NotifierFragment.NotifierCache.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == NOTIFIER) {
            return new NotifierHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_notifierview, parent, false), this);
        } else {
            return new HelperHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.helper_for_notifier, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NotifierHolder) {
            ((NotifierHolder) holder).forNotifier(getNotifierComponent(position, holder.itemView.getContext()));
        } else if (holder instanceof HelperHolder) {

            final Context c = holder.itemView.getContext();
            HelperHolder helperHolder = (HelperHolder) holder;
            final Helper helper = (Helper) ids.get(position);

            helperHolder.newBuilder().setCardBackground(R.color.white)
                .setElevation(4)
                .setDivider(c.getResources().getDrawable(helper.dividerDrawableResourceId))
                .setTopIconColor(helper.topIconColor)
                .setTopBackground(helper.topColor)
                .setTitle(helper.title)
                .setMessage(helper.text, helper.asHtml)
                .setNegativeButton(R.string.understand, new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        int index = ids.indexOf(helper);
                        if (index != -1) {
                            HelperHolder.gotIt(c, helper.idenify);
                            ids.remove(index);
                            notifyItemRemoved(index);
                        }

                    }
                });

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (ids.get(position) instanceof Helper) {
            return HELPER;
        } else {
            return NOTIFIER;
        }
    }

    private com.opcon.notifier.components.Notifier getNotifierComponent(int position, Context context) {
        if (directStore != null)  {
            return directStore.get(position);
        } else {
             return notifierCacheCache.getNotificator(context, (Integer) ids.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (directStore != null) {
            return directStore.size();
        } else {
            return ids.size();
        }
    }

    static class NotifierHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private NotifierView mNotifierView;
        private NotifierAdapter mAdapterRef;

        private NotifierHolder(View itemView, NotifierAdapter adapterReference) {
            super(itemView);
            this.mAdapterRef = adapterReference;
            this.mNotifierView = (NotifierView) itemView.findViewById(R.id.notifierView);
            mNotifierView.mAvatar.setOnClickListener(this);
            mNotifierView.mOptions.setOnClickListener(this);
            mNotifierView.setOnLongClickListener(this);
            mNotifierView.mNotification.setOnClickListener(this);

        }

        private void forNotifier(com.opcon.notifier.components.Notifier component) {
            this.mNotifierView.with(component);
        }

        @Override
        public void onClick(View v) {
            // protect RecyclerView.NO_POSITION.
            if (getAdapterPosition() == -1) {
                return;
            }
            if (v.getId() == mNotifierView.mAvatar.getId()) {
                mAdapterRef.notifierViewEventHandler.onAvatarClick(mAdapterRef.getNotifierComponent(getAdapterPosition(), mNotifierView.getContext()).getId());
            } else if (v.getId() == mNotifierView.mNotification.getId()) {
                mAdapterRef.notifierViewEventHandler.onNotificationClick(mAdapterRef.getNotifierComponent(getAdapterPosition(), mNotifierView.getContext()).getId());
            }  else if (v.getId() == mNotifierView.mOptions.getId()) {
                mAdapterRef.notifierViewEventHandler.onShowOptions(getAdapterPosition(), mAdapterRef.getNotifierComponent(getAdapterPosition(), mNotifierView.getContext()).getId());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (getAdapterPosition() == -1)
                return false;
                mAdapterRef.notifierViewEventHandler.onShowOptions(getAdapterPosition(), mAdapterRef.getNotifierComponent(getAdapterPosition(), mNotifierView.getContext()).getId());
            return false;
        }
    }

}
