package com.opcon.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opcon.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahmut Taşkiran on 18/10/2016.
 */

public class SoundListDialog extends Dialog {
    private int lastSelectedFieldVal= -1;
    private int lastSelectedFieldPosition = -1;


    private ArrayList<Field> mFields;

    private MediaPlayer mediaPlayer;
    private Adapter adapter;
    private OnClickListener listener;

    public interface OnClickListener {
        void onClick(int fieldID);
        void onSelected(int fieldID, String fieldName);
    }

    public SoundListDialog(Context context, final OnClickListener listener) {
        super(context);
        this.listener = listener;
        initAdapter();
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ListView listView = new ListView(context);

        listView.setLayoutParams(layoutParams);
        listView.setAdapter(adapter);

        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, listView.getId());
        Button button = new Button(getContext());
        button.setBackgroundColor(getContext().getResources().getColor(R.color.colorSecondary));
        button.setTextColor(Color.WHITE);
        button.setLayoutParams(buttonParams);
        button.setText(R.string.ok);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastSelectedFieldPosition != -1) {
                    listener.onSelected(lastSelectedFieldVal,
                        upperChiefs(mFields.get(lastSelectedFieldPosition)
                            .getName().substring(2)));
                    if (mediaPlayer != null) {
                        if (mediaPlayer.isPlaying())
                            mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
                dismiss();
            }
        });

        listView.setPadding(0, 0, 0, 200);

        relativeLayout.addView(listView);
        relativeLayout.addView(button);
        setContentView(relativeLayout);

        this.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });

    }

    private void initAdapter() {
        Field[] fields = R.raw.class.getFields();
        mFields = new ArrayList<>();
        List<String> faceArr = new ArrayList<>();

        for (Field f: fields) {
            if (f.getName().startsWith("__")) {
                mFields.add(f);
                faceArr.add(upperChiefs(f.getName().substring(2)));
            }
        }
        this.adapter = new Adapter(getContext(), faceArr, R.layout.row_sound);
    }

    private static String upperChiefs(String str) {
        char[] chars = str.toCharArray();
        if (Character.isLowerCase(chars[0]))
            chars[0] = Character.toUpperCase(chars[0]);

        String s = new String(chars);
        return s.replace("_", " ");
    }

    private class Adapter extends ArrayAdapter<String>{

        public Adapter(Context context, List<String> fakearr, int resource) {
            super(context, resource, fakearr);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

            TextView textView;
            LinearLayout root;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_sound, parent, false);
                textView = (TextView) convertView.findViewById(R.id.rowsound_soundname);
                root = (LinearLayout) convertView.findViewById(R.id.rowsound_root);
                convertView.setTag(R.id.rowsound_root, root);
                convertView.setTag(R.id.rowsound_soundname, textView);
            } else {
                root  = (LinearLayout) convertView.getTag(R.id.rowsound_root);
                textView = (TextView) convertView.getTag(R.id.rowsound_soundname);
            }

            if (lastSelectedFieldPosition == position) {
                root.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.softGrey));
            } else {
                root.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.white));
            }

            textView.setText(getItem(position));
            root.setOnClickListener(new View.OnClickListener() {
                private int ppp = position;
                @Override
                public void onClick(View v) {
                    try {
                        lastSelectedFieldVal = mFields.get(ppp).getInt(null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    lastSelectedFieldPosition = ppp;
                    listener.onClick(ppp);

                    try {
                        if (mediaPlayer != null) {
                            if (mediaPlayer.isPlaying())
                                mediaPlayer.stop();
                            mediaPlayer.release();
                        }
                        mediaPlayer = MediaPlayer.create(getContext(), mFields.get(ppp).getInt(null));
                        mediaPlayer.start();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }
}
