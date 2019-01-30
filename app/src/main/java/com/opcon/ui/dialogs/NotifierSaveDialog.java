package com.opcon.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.opcon.R;
import com.opcon.notifier.components.Notifier;
import com.opcon.ui.views.NotifierView;


/**
 *
 * Created by Mahmut Taşkiran on 19/10/2016.
 */

public class NotifierSaveDialog extends Dialog implements View.OnClickListener{

    public void hideNote() {
        noteView.setVisibility(View.GONE);
    }

    public interface OnCompleteListener {
        void onDescriptionEntered(boolean global, String desc);
    }

    private TextView noteView;

    private Button mCheckButton;

    private OnCompleteListener listener;
    private NotifierView notifierView;

    public NotifierSaveDialog(Activity context, boolean creationMode, OnCompleteListener listener, Notifier rv) {
        super(context, R.style.PauseDialog);
        this.listener = listener;



        if (getWindow() != null)
        {
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        ScrollView scrollView = new ScrollView(context);


        LinearLayout rootLayout = new LinearLayout(getContext());
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rootLayout.setGravity(Gravity.CENTER);
        rootLayout.setBackgroundColor(getContext().getResources().getColor(R.color.white));

        scrollView.setBackgroundColor(getContext().getResources().getColor(R.color.transparentDark));

        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));



        Button button = new AppCompatButton(getContext());
        if (creationMode) {
            button.setText(getContext().getString(R.string.notifier_creation_save_it));
        } else {
            button.setText(getContext().getString(R.string.notifier_creation_update_it));
        }
        button.setBackgroundColor(getContext().getResources().getColor(R.color.colorSecondary));
        button.setLeft(10);
        button.setRight(10);

        button.setGravity(Gravity.CENTER);
        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(this);
        button.setTextColor(Color.WHITE);

        mCheckButton = button;




        notifierView = new NotifierView(context);

        notifierView.setPadding(0,0,0,0);


        notifierView.makeEditable();
        notifierView.fullView();
        notifierView.hideRightSide();
        notifierView.editionStyle();
        notifierView.with(rv);
        notifierView.makeEditable();

        rootLayout.addView(notifierView);
        rootLayout.addView(button);

        CardView cardView = new CardView(context);
        cardView.setRadius(0);
        cardView.setCardBackgroundColor(context.getResources().getColor(R.color.transparentDark));
        cardView.setPadding(100, 100, 100, 100);
        cardView.setContentPadding(40, 40, 40, 40);
        cardView.setCardElevation(2);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        noteView = new TextView(context);
        noteView.setText(Html.fromHtml(context.getString(R.string.notifier_save_dialog_note1)));
        noteView.setTextColor(getContext().getResources().getColor(R.color.white));
        Drawable drawable = context.getResources().getDrawable(R.drawable.ic_info_outline_white_18dp);
        drawable.setColorFilter(getContext().getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);

        noteView.setCompoundDrawablesWithIntrinsicBounds(drawable, null,null,null);
        noteView.setCompoundDrawablePadding(35);
        noteView.setLayoutParams(new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        cardView.addView(noteView);

        rootLayout.addView(cardView);

        scrollView.addView(rootLayout);


        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContentView(scrollView);

    }

    public void setCheckButtonText(String text) {
        mCheckButton.setText(text);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onDescriptionEntered(false, notifierView.getDesc());
            dismiss();
        }
    }
}
