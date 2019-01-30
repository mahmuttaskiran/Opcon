package com.opcon.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.opcon.R;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.components.constants.Operations;
import com.opcon.ui.utils.NotifierConstantUtils;

import java.util.List;


/**
 * Created by Mahmut Taşkiran on 06/03/2017.
 */

public class VerticalOccView extends RecyclerView {
  private OCCAdapter mOccAdapter;

  public void noFilter() {
    if (mOccAdapter != null) {
      mOccAdapter.mNoFilter = true;
      mOccAdapter.notifyDataSetChanged();
    }
  }

  public void setComponents(List<NotifierConstantUtils.Component> components) {
    if (mOccAdapter != null) {
      mOccAdapter.mComponents = components;
      mOccAdapter.notifyDataSetChanged();
    }
  }

  public enum Component {condition, operation}
  private Component mComponent;
  private ComponentClickListener mListener;
  private LinearLayoutManager mLayoutManager;

  public interface ComponentClickListener {
    void onComponentClick(NotifierConstantUtils.Component c);
    void onTargetChanged();
  }

  public void setListener(ComponentClickListener l) {
    mListener = l;
    mOccAdapter.setListener(mListener);
  }

  public void setComponent(Component c, int ui) {
    mOccAdapter.setComponent(c, ui);
    int p = mOccAdapter.findPosition(ui);
    mLayoutManager.scrollToPosition(p);
  }


  public VerticalOccView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    setLayoutManager(mLayoutManager);
    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VerticalOccView);
    String mHeDivider = ta.getString(R.styleable.VerticalOccView_hiDivider);
    String mYouDivider = ta.getString(R.styleable.VerticalOccView_youDivider);
    String component = ta.getString(R.styleable.VerticalOccView_components);
    if (TextUtils.isEmpty(component)) component = "condition";
    mComponent = Component.valueOf(component);
    ta.recycle();
    initAdapter();
    setAdapter(mOccAdapter);
    mOccAdapter.setHiDivider(mHeDivider);
    mOccAdapter.setYouDivider(mYouDivider);



  }

  private void initAdapter() {
    List<NotifierConstantUtils.Component> onTheTarget = mComponent == Component.condition ?
        NotifierConstantUtils.getConditions(getContext(), NotifierConstantUtils.ON_THE_TARGET) :
        NotifierConstantUtils.getOperations(getContext(), NotifierConstantUtils.ON_THE_TARGET);

    List<NotifierConstantUtils.Component> onTheOwner = mComponent == Component.condition ?
        NotifierConstantUtils.getConditions(getContext(), NotifierConstantUtils.ON_THE_OWNER) :
        NotifierConstantUtils.getOperations(getContext(), NotifierConstantUtils.ON_THE_OWNER);

    mOccAdapter = new OCCAdapter(onTheTarget, onTheOwner, mListener, mComponent);

  }

  private static class OCCAdapter extends RecyclerView.Adapter {
    private static final int FILTER = 2;
    private static final int COMPONENT = 1;

    private static final int ON_THE_TARGET = 0;
    private static final int ON_THE_OWNER = 1;

    private int mOnWho;
    private int mSelectedComponentId = -1;

    private boolean mNoFilter;

    private String mHiDivider, mYouDivider;

    List<NotifierConstantUtils.Component> mComponents;
    List<NotifierConstantUtils.Component> mOnTheTarget;
    List<NotifierConstantUtils.Component> mOnTheOwner;

    ComponentClickListener mListener;

    private Component mComponentType;

    public OCCAdapter(List<NotifierConstantUtils.Component> mOnTheTarget, List<NotifierConstantUtils.Component> mOnTheOwner, ComponentClickListener mListener, Component component) {
      this.mComponentType = component;
      this.mOnTheTarget = mOnTheTarget;
      this.mOnTheOwner = mOnTheOwner;
      this.mListener = mListener;
      mOnWho = ON_THE_TARGET;
      setComponents(mOnTheTarget);
    }

    public void setHiDivider(String d) {
      mHiDivider = d;
    }

    public void setYouDivider(String s) {
      mYouDivider = s;
    }

    public void setComponent(Component component, int id) {
      boolean onTheTarget = component ==  Component.condition ? Conditions.isOnTheTarget(id) : Operations.isOnTheTarget(id);
      List<NotifierConstantUtils.Component> newComponents;
      if (onTheTarget) {
        mOnWho = ON_THE_TARGET;
        newComponents = mOnTheTarget;
      } else {
        mOnWho = ON_THE_OWNER;
        newComponents = mOnTheOwner;
      }

      mSelectedComponentId = id;

      setComponents(newComponents);
      notifyDataSetChanged();

    }

    int findPosition(int id) {
      for (int i = 0; i < mComponents.size(); i++) {
        if (mComponents.get(i) != null)
          if (mComponents.get(i).uid == id) {
            return i;
          }
      }
      return 0;
    }

    private int gc(Context c, @ColorRes int ccc) {
      return c.getResources().getColor(ccc);
    }

    public int setComponents(List<NotifierConstantUtils.Component> components) {
      int oldSize = components.size();
      this.mComponents = components;
      return oldSize;
    }

    @Override public int getItemCount() {
      return mComponents.size();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      return new ComponentHolder(inflater.inflate(R.layout.row_occ, parent, false), this);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      ((ComponentHolder) holder).withComponent(mComponents.get(position));
    }

    @Override public int getItemViewType(int i) {
      if (!mNoFilter) {
        return i < 2 ? FILTER : COMPONENT;
      } else {
        return COMPONENT;
      }
    }

    public void setListener(ComponentClickListener listener) {
      this.mListener = listener;
    }


    private static class ComponentHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      private CircleRelativeLayout mCrl;
      private ImageView mIcon;
      private TextView mDesc;
      private OCCAdapter ref;
      public ComponentHolder(View itemView, OCCAdapter ref) {
        super(itemView);
        this.ref = ref;
        mCrl = (CircleRelativeLayout) itemView.findViewById(R.id.crl);
        mIcon = (ImageView) itemView.findViewById(R.id.icon);
        mDesc = (TextView) itemView.findViewById(R.id.title);
        itemView.setOnClickListener(this);
      }

      public void withComponent(NotifierConstantUtils.Component component) {
        mDesc.setText(component.title);
        mIcon.setImageResource(component.icon);
        Context c = mDesc.getContext();

        int color = ref.mComponentType == Component.condition ? NotifierConstantUtils.getConditionColor(component.uid):
            NotifierConstantUtils.getOperationColor(component.uid);

        if (component.uid == ref.mSelectedComponentId) {
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

        ViewCompat.animate(mCrl)
            .scaleX(0.5F)
            .scaleY(0.5F)
            .setDuration(150)
            .withEndAction(new Runnable() {
              @Override
              public void run() {
                ViewCompat.animate(mCrl)
                    .scaleX(1)
                    .scaleY(1)
                    .setDuration(150)
                    .start();
              }
            }).start();


        int p = getAdapterPosition();
        if (p == -1)return;
        ref.mSelectedComponentId = ref.mComponents.get(p).uid;
        if (ref.mListener != null)
          ref.mListener.onComponentClick(ref.mComponents.get(p));
        ref.notifyDataSetChanged();
      }
    }
  }
}
