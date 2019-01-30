package com.opcon.ui.adapters;

import android.util.SparseIntArray;

import com.opcon.components.Message;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Mahmut Taşkiran on 08/12/2016.
 */

public class MessageSelectionManagement {

    private List<Integer> selectedKeys;

    private SparseIntArray types;

    private MessageSelectionManagementListener listener;


  public interface MessageSelectionManagementListener {
        void onSelectionState();
        void onDeselectionState();
        void onSelected(int key, int forAdapterPosition);
        void onDeselected(int key, int forAdapterPosition);
    }

    MessageSelectionManagement() {
        this.selectedKeys = new ArrayList<>();
        this.types = new SparseIntArray();
    }

    public void setSelectionManagerListener(MessageSelectionManagementListener listener) {
        this.listener = listener;
    }

    void longClickToggle(int key, int type, int forAdapterPosition) {
        toggle(key, type, true, forAdapterPosition);
    }

    void clickToggle(int key, int type, int forAdapterPosition) {
        toggle(key, type, false, forAdapterPosition);
    }

    private void toggle(int key, int type, boolean permissionForStart, int forAdapterPosition) {

        if (inSelectionState()) {
            if (selectedKeys.size() == 1) {
                if (isSelected(key)) {
                    selectedKeys.clear();
                    if (listener != null) {
                        listener.onDeselectionState();
                    }
                } else {
                    add(key, type, forAdapterPosition);
                }
            } else {
                if (isSelected(key))
                    remove(key, type, forAdapterPosition);
                else
                    add(key, type,forAdapterPosition);
            }
        } else {
            if (permissionForStart) {
                if (listener != null){
                    listener.onSelectionState();
                }
                add(key,type, forAdapterPosition);
            }
        }
    }

    public void remove(int key, int type, int forAdapterPosition) {
        selectedKeys.remove(Integer.valueOf(key));
        types.delete(type);
        if (listener != null) {
            listener.onDeselected(key, forAdapterPosition);
        }
    }

    public void add(int key, int type, int forAdapterPosition) {
        selectedKeys.add((key));
        types.put(key, type);
        if (listener != null) {
            listener.onSelected(key, forAdapterPosition);
        }
    }

    public boolean inSelectionState() {
        return !selectedKeys.isEmpty();
    }

    boolean isSelected(int key) {
        return (selectedKeys.contains(key));
    }

    public List<Integer> getSelectedKeys() {
        return selectedKeys;
    }

    public boolean isOnlyText() {
        for (int i = 0; i < selectedKeys.size(); i++) {
            int key = selectedKeys.get(i);
            if (types.get(key) != Message.TEXT) {
                return false;
            }
        }
        return true;
    }

    public boolean isSinglePacket() {
      return selectedKeys.size() == 1 && Message.isSpecialPacket(types.get(selectedKeys.get(0), 0));
    }

}
