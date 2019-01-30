package com.opcon.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.opcon.R;
import com.opcon.ui.utils.NotifierConstantUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahmut Taşkiran on 06/03/2017.
 */

public class VerticalPacketPickerView extends RecyclerView {
  private PacketAdapter mAdapter;
  private LinearLayoutManager mLayoutManager;

  @Nullable private List<Integer> mPacketFilter;

  public void setPacketFilter(List<Integer> filter) {
    mPacketFilter = filter;
    if (mAdapter != null) {
      mAdapter.filterPackets(mPacketFilter);
    }
  }

  public void showTitles(boolean showTitles) {
    if (mAdapter != null) {
      mAdapter.showTitles = showTitles;
      mAdapter.notifyDataSetChanged();
    }
  }

  public void setPacket(int packet) {
    mAdapter.setComponent(packet);
    mLayoutManager.scrollToPosition(mAdapter.findPosition(packet));
  }

  public interface SpecialPacketSelectListener {
    void onSpecialPacketSelected(int id, String title);
  }

  public void setListener(SpecialPacketSelectListener l) {
    mAdapter.setListener(l);
  }

  public void setForSender(boolean forSender) {
    this.mAdapter.setForSender(forSender);
  }


  public VerticalPacketPickerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    setLayoutManager(mLayoutManager);
    mAdapter = new PacketAdapter(NotifierConstantUtils.getPackets());
    setAdapter(mAdapter);
  }

  private static class PacketAdapter extends RecyclerView.Adapter<PacketAdapter.ComponentHolder> {

    private int mSelectedPacket = -1;
    private SpecialPacketSelectListener mListener;
    List<NotifierConstantUtils.Component> mPackets;
    List<NotifierConstantUtils.Component> mOPackets;
    boolean mForSender;
    boolean showTitles = true;

    public void filterPackets(@Nullable List<Integer> f){

      if (f == null ){
        NotifierConstantUtils.Component component = mPackets.get(0);
        mPackets = mOPackets;
        mOPackets.remove(component);
        mOPackets.add(0, component);
      } else {
        List<NotifierConstantUtils.Component> newPackets = new ArrayList<>();

        if (mOPackets != null) {
          for (NotifierConstantUtils.Component mPacket : mOPackets) {
            if (f.contains(mPacket.uid)) {
              newPackets.add(mPacket);
            }
          }
        }

        mPackets = newPackets;
      }



      notifyDataSetChanged();

    }

    public PacketAdapter(List<NotifierConstantUtils.Component> packets) {
      mPackets = packets;
      mOPackets = mPackets;
    }
    public void setForSender(boolean forSender) {
      this.mForSender = forSender;
    }

    public void setComponent(int id) {
      mSelectedPacket = id;
      notifyDataSetChanged();
    }

    private int gc(Context c, @ColorRes int ccc) {
      return c.getResources().getColor(ccc);
    }

    @Override public int getItemCount() {
      return mPackets.size();
    }

    @Override public ComponentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      return new ComponentHolder(inflater.inflate(R.layout.row_occ, parent, false), this);
    }

    int findPosition(int id) {
      for (int i = 0; i < mPackets.size(); i++) {
        if (mPackets.get(i).uid == id) {
          return i;
        }
      }
      return 0;
    }

    @Override public void onBindViewHolder(ComponentHolder holder, int position) {
      holder.withComponent(mPackets.get(position));
    }

    public void setListener(SpecialPacketSelectListener listener) {
      this.mListener = listener;
    }

    public static class ComponentHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      private CircleRelativeLayout mCrl;
      private ImageView mIcon;
      private TextView mDesc;
      private PacketAdapter ref;
      public ComponentHolder(View itemView, PacketAdapter ref) {
        super(itemView);
        this.ref = ref;
        mCrl = (CircleRelativeLayout) itemView.findViewById(R.id.crl);
        mIcon = (ImageView) itemView.findViewById(R.id.icon);
        mDesc = (TextView) itemView.findViewById(R.id.title);
        itemView.setOnClickListener(this);
      }
      public void withComponent(NotifierConstantUtils.Component component) {
        mIcon.setImageResource(component.icon);
        Context c = mDesc.getContext();
        if (ref.showTitles) {
          mDesc.setVisibility(VISIBLE);
          mDesc.setText(NotifierConstantUtils.getPacketTitle(c, component.uid, ref.mForSender));
        } else {
          mDesc.setVisibility(GONE);
        }
        int color = NotifierConstantUtils.getPacketColor(component.uid);
        if (component.uid == ref.mSelectedPacket) {
          mCrl.setColor(ref.gc(c, R.color.white));
          mCrl.setStrokeWidth(2);
          mCrl.setStrokeColor(color);
          mIcon.setColorFilter(color);
        } else {
          mCrl.setColor(color);
          mCrl.setStrokeWidth(0);
          mIcon.setColorFilter(Color.WHITE);
        }
      }

      @Override
      public void onClick(View v) {
        int p = getAdapterPosition();
        if (p == -1) return;
        ref.mSelectedPacket = ref.mPackets.get(p).uid;
        if (ref.mListener != null)
          ref.mListener.onSpecialPacketSelected(ref.mSelectedPacket,  NotifierConstantUtils.getPacketTitle(v.getContext(), ref.mSelectedPacket, ref.mForSender));
        ref.notifyDataSetChanged();
      }
    }
  }
}
