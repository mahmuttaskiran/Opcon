package com.opcon.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.components.Contact;
import com.opcon.ui.fragments.ContactFragment;
import com.opcon.ui.views.ContactView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 *
 * Created by Mahmut Taşkiran on 03/12/2016.
 */

public class NewContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
implements SectionTitleProvider {

    private static final int CONTACT = 1;
    private static final int INVITE = 0;

    private static final String FORMAT = "<font color='blue'><b>%s</b></font>";

    private List<Integer> mContactIds;
    private ContactAdapterClickHandler mClickHandler;
    private String mHighlightText;
    private ContactFragment.ContactCache mContactCache;
    private Context mContext;

    private Fragment fragment;

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public NewContactAdapter(Context context,
                             ContactAdapterClickHandler contactAdapterClickHandler,
                             List<Integer> contactsIds) {
        this.mContext = context;
        this.mClickHandler = contactAdapterClickHandler;
        this.mContactIds = contactsIds;
        this.mContactCache = ContactFragment.ContactCache.getInstance();
    }

    static String highlight(String input, String filter) {
        Pattern pattern = Pattern.compile("(?i)(" + filter + ")");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String originalText = matcher.group(1);
            return input.replaceAll("(?i)" + filter, String.format(FORMAT, originalText));
        }
        return input;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ContactHolder) {
            final Contact contact =  getContact(position, holder.itemView.getContext());
            ContactView contactView = ((ContactHolder) holder).contactView;

            AvatarLoader.load(contactView.getReferenceOfImageView(), contact.profileUri, contact.name);
            char beforeChar, currentChar = Character.toLowerCase(contact.name.charAt(0));
            beforeChar = Character.toLowerCase(getBeforeChar(position, holder.itemView.getContext()));
            contactView.setContactName(contact.name);
            contactView.setPhoneNumber(contact.number);
            if (isInFilterState()) {
                contactView.setContactName(Html.fromHtml(highlight(contact.name, mHighlightText)));
            }

            if (beforeChar != currentChar) {
                contactView.setLetterVisible(true, Character.toUpperCase(currentChar));
            } else {
                contactView.setLetterVisible(false, ' ');
            }

            if (contact.hasOpcon) {
                contactView.getGavelButtonRef().setVisibility(View.VISIBLE);
            } else {
                contactView.getGavelButtonRef().setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mContactIds.get(position) == -1) {
            return INVITE;
        } else {
            return CONTACT;
        }
    }

    private char getBeforeChar(int position, Context context) {
        if (position == 0 || mContactIds.get(position -1) == -1) {
            return '#';
        } else {
            Contact contact = getContact(position -1, context);
            return contact.name.charAt(0);
        }
    }

    @Override
    public synchronized int getItemCount() {
        return mContactIds.size();
    }

    public synchronized void setContactIds(List<Integer> mContactIds) {
        this.mContactIds = mContactIds;
    }

    public void setHighlightText(String highlightText) {
        this.mHighlightText = highlightText;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == CONTACT) {
            return new ContactHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_of_contactview, parent, false), this);
        } else {
            return new InviteUserHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.there_is_no_friend, parent, false), this);
        }
    }

    @Override
    public String getSectionTitle(int position) {
        if (mContactIds.get(position) == -1) {
            return "#";
        }
        return String.valueOf(getContact(position, mContext).name.charAt(0)).toUpperCase();
    }

    public String getHighlightText() {
        return mHighlightText;
    }


    static class InviteUserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        AppCompatButton mInvite;
        AppCompatButton mRefresh;
        NewContactAdapter mContactAdapter;
        InviteUserHolder(View v, NewContactAdapter adapter) {
            super(v);
            mContactAdapter = adapter;
            mInvite = (AppCompatButton)v.findViewById(R.id.invite);
            mRefresh = (AppCompatButton) v.findViewById(R.id.refresh);
            mInvite.setOnClickListener(this);
            mRefresh.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == mInvite) {
                invite();
            } else {
                if (mContactAdapter.mClickHandler != null) {
                    mContactAdapter.mClickHandler.onRefreshRequest();
                }
            }
        }

        void invite() {
            Intent intent = new AppInviteInvitation.IntentBuilder(mInvite.getContext().getString(R.string.invitation_title))
                .setMessage(mInvite.getContext().getString(R.string.invitation_message))
                .setDeepLink(Uri.parse("https://play.google.com/store/apps/details?id=com.opcon"))
                .build();
            mContactAdapter.getFragment().getActivity().startActivityForResult(intent, 1);
        }
    }



    static class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ContactView contactView;
        NewContactAdapter adapterReference;
        ContactHolder(View itemView, NewContactAdapter adapterReference) {
            super(itemView);
            this.adapterReference = adapterReference;
            this.contactView = (ContactView) itemView.findViewById(R.id.rowofcontactview_contactview);
            this.contactView.getRootReference().setOnClickListener(this);
            this.contactView.getRootReference().setOnLongClickListener(this);
            this.contactView.getReferenceOfImageView().setOnClickListener(this);
            this.contactView.bNotifierIcon.setOnClickListener(this);
        }
        @Override public void onClick(View v) {
            if (getAdapterPosition() == -1)
                return;

            final int contactId = adapterReference.mContactIds.get(getAdapterPosition());
            if (v.getId() == contactView.getRootReference().getId()) {
                adapterReference.mClickHandler.onContactClick(contactId);
            } else if (v.getId() == contactView.getReferenceOfImageView().getId()) {
                adapterReference.mClickHandler.onAvatarClick(contactId);
            } else if (v.getId() == contactView.bNotifierIcon.getId()) {
                ViewCompat.animate(contactView.bNotifierIcon)
                    .scaleX(0.5F).scaleY(0.5F).setDuration(150)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            ViewCompat.animate(contactView.bNotifierIcon)
                                .scaleX(1).scaleY(1).setDuration(150)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapterReference.mClickHandler.onGavelClick(contactId);
                                    }
                                });
                        }
                    });

                Toast.makeText(contactView.getContext(), R.string.this_man_use_Opcon, Toast.LENGTH_SHORT).show();


            }
        }
        @Override public boolean onLongClick(View v) {
            return false;
        }
    }


    public Contact getContact(int adapterPosition, Context context) {
        return mContactCache.getContact(context, mContactIds.get(adapterPosition));
    }

    private boolean isInFilterState() {
        return !TextUtils.isEmpty(mHighlightText);
    }

    public interface ContactAdapterClickHandler {
        void onAvatarClick(int id);
        void onContactClick(int id);
        void onGavelClick(int id);
        void onRefreshRequest();
    }

}
