package com.opcon.ui.adapters;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.opcon.R;
import com.opcon.components.Dialog;
import com.opcon.ui.management.DialogStoreManagement;
import com.opcon.ui.views.DialogView;

/**
 *
 * Created by Mahmut Taşkiran on 28/11/2016.
 *
 */

public class DialogAdapter extends RecyclerView.Adapter {
    private DialogStoreManagement dialogStoreManagement = DialogStoreManagement.getInstance(null);
    private DialogEventListener mClickListener;

    public interface DialogEventListener {
        void onDialogClick(Dialog dialog);
        void onDialogAvatarClick(Dialog dialog);
        void onDialogLongClick(Dialog d);
    }

    public DialogAdapter(DialogEventListener listener) {
        this.mClickListener = listener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Dialog dialog = dialogStoreManagement.get(position);
        DialogHolder d_holder = (DialogHolder) holder;
        d_holder.withDialog(dialog);
    }

    private Dialog getDialog(int position) {
        return dialogStoreManagement.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder holder;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialog_row, parent, false);
        holder = new DialogHolder(view, this);
        return holder;
    }



    @Override
    public int getItemCount() {
        return dialogStoreManagement.size();
    }

    private static class DialogHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private DialogView dialogView;
        private DialogAdapter referenceOfDialogAdapter;
        private DialogHolder(View itemView, DialogAdapter referenceOfDialogAdapter) {
            super(itemView);
            this.referenceOfDialogAdapter = referenceOfDialogAdapter;
            this.dialogView = (DialogView) itemView.findViewById(R.id.dialog_row_dialog);
            dialogView.setOnClickListener(this);
            dialogView.setOnLongClickListener(this);
            dialogView.mAvatar.setOnClickListener(this);
        }

        private void withDialog(Dialog dialog) {
            this.dialogView.forDialog(dialog);
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                if (v == dialogView.mAvatar) {

                    ViewCompat.animate(dialogView.mAvatar).scaleX(0.8f).scaleY(0.8f).setDuration(150).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            referenceOfDialogAdapter.mClickListener.onDialogAvatarClick(referenceOfDialogAdapter.getDialog(getAdapterPosition()));
                            dialogView.mAvatar.setRotation(0f);
                            dialogView.mAvatar.setScaleX(1f);
                            dialogView.mAvatar.setScaleY(1f);
                        }
                    }).start();

                } else {
                    Dialog dialog;
                    dialog = referenceOfDialogAdapter.getDialog(getAdapterPosition());
                    referenceOfDialogAdapter.mClickListener.onDialogClick(dialog);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                Dialog d = referenceOfDialogAdapter.getDialog(getAdapterPosition());
                referenceOfDialogAdapter.mClickListener.onDialogLongClick(d);
            }
            return false;
        }
    }

}
