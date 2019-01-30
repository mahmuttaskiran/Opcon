package com.opcon.ui.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opcon.libs.utils.TimeUtils;
import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.components.Component;
import com.opcon.components.NotifierLog;
import com.opcon.notifier.components.Notifier;
import com.opcon.ui.activities.NotifierBuilderActivity;
import com.opcon.ui.views.NotifierView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import agency.tango.android.avatarview.views.AvatarView;
import timber.log.Timber;

/**
 *
 * Created by Mahmut Taşkiran on 29/11/2016.
 *
 */

public class NotifierLogAdapter extends RecyclerView.Adapter {

    private static final String ONLY_TITLE = "<font color='black'><b>%s</b></font>";
    private static final String EDITED = "<br><font color='grey'><i>%s</i></font>";

    private static final int NOTIFIER = 928;
    private static final int LOG = 743;

    private List<Component> logs;

    public NotifierLogAdapter(List<Component> logs) {
        super();
        this.logs = logs;
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LogHolder) {
            ((LogHolder) holder).withNotifierLog((NotifierLog) logs.get(position));
        } else {
            ((NotifierHolder) holder).mNotifierView.with((Notifier) logs.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        Component c= logs.get(position);
        if (c instanceof Notifier) {
            return NOTIFIER;
        } else if (c instanceof NotifierLog) {
            return LOG;
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == NOTIFIER) {
            return new NotifierHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_notifierview, parent, false));
        } else {
            return new LogHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notifier_log_view, parent, false), this);
        }
    }

    private static class NotifierHolder extends RecyclerView.ViewHolder {
        NotifierView mNotifierView;
        NotifierHolder(View itemView) {
            super(itemView);
            mNotifierView = (NotifierView) itemView.findViewById(R.id.notifierView);
            mNotifierView.hideOptions();
            mNotifierView.hideNotification();
        }

    }

    private static class LogHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private NotifierLogAdapter adapterRef;
        private TextView date, title;
        private RelativeLayout root;
        private AvatarView avatar;
        private ImageView icon;
        LogHolder(View itemView, NotifierLogAdapter adapterRef) {
            super(itemView);
            this.adapterRef = adapterRef;
            date = (TextView) itemView.findViewById(R.id.log_date);
            title = (TextView) itemView.findViewById(R.id.log_title);
            root = (RelativeLayout) itemView.findViewById(R.id.root);
            this.avatar = (AvatarView) itemView.findViewById(R.id.avatar);
            this.icon = (ImageView) itemView.findViewById(R.id.icon);
            root.setOnClickListener(this);
        }

        void withNotifierLog(NotifierLog log) {
            String title = log.getTitle(date.getContext());
            int icon = log.getIcon();
            String avatar = log.getAvatar();

            AvatarLoader.load(this.avatar, avatar, log.getSenderName(date.getContext()));

            this.icon.setImageResource(icon);

            String format_of_only_title = ONLY_TITLE;
            String format_of_seen_to_details_of_editing = format_of_only_title + EDITED;

            if (log.getType() == NotifierLog.EDITED) {
                this.title.setText(Html.fromHtml(String.format(format_of_seen_to_details_of_editing, title,
                    date.getContext().getString(R.string.click_for_seen_editing_details))));
            } else {
                this.title.setText(Html.fromHtml(String.format(format_of_only_title, title)));
            }

            this.date.setText(TimeUtils.shortDateAndTime(log.getTimestamp(), TimeUnit.HOURS.toMillis(2)));

            if (log.isSeen()) {
                root.setBackgroundColor(root.getContext().getResources().getColor(R.color.nonSeenLogColor));
            } else {
                root.setBackgroundColor(Color.WHITE);
            }

        }

        @Override
        public void onClick(View v) {
            int p = getAdapterPosition();
            if (p == -1) return;
            Component component =adapterRef.logs.get(p);
            if (component instanceof NotifierLog) {
                if (((NotifierLog) component).getType() == NotifierLog.EDITED) {
                    NotifierBuilderActivity.seen(date.getContext(), ((NotifierLog) component).getExternalParams().toJson());
                }
            }

        }
    }
}
