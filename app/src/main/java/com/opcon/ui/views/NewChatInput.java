package com.opcon.ui.views;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opcon.R;
import com.opcon.libs.utils.AnimationUtils;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.ui.utils.NotifierConstantUtils;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTitleView;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 09/03/2017.
 */

public class NewChatInput extends RelativeLayout {

  @BindView(R.id.emojititle_top_divider)
  View mTopDivider;
  @BindView(R.id.emojititle)
  EmojiTitleView mEmojiTitle;
  @BindView(R.id.emojititle_bottom_divider)
  View mBottomDivider;
  @BindView(R.id.smile)
  CircleRelativeLayout mSmile;
  @BindView(R.id.smile_icon)
  ImageView mSmileIcon;
  @BindView(R.id.edit)
  EmojiEditText mEditText;
  @BindView(R.id.sendIcon)
  ImageView mSendIcon;
  @BindView(R.id.send)
  CircleRelativeLayout mSendButton;
  @BindView(R.id.input)
  RelativeLayout input;
  @BindView(R.id.recyclerView)
  RecyclerView mRecyclerView;
  View mRoot;
  @BindView(R.id.listDivider)
  View mListDivider;

  public String getText() {
    return mEditText.getText().toString();
  }

  public interface ChatInputListener {
    void onCamera();
    void onGallery();
    void onPacket(int packetType);
    void onText(String text);
    void onLocation();
  }

  private Activity mActivity;
  private ChatInputListener mListener;
  private EmojiPopup mEmojiPopup;
  private int mSendButtonLastIcon = R.drawable.ic_attachment_black_24dp;

  public NewChatInput(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    mRoot = inflate(getContext(), R.layout.chat_input, this);
  }

  public void enterToSend(boolean enterToSend) {
    if (enterToSend) {
      mEditText.setImeOptions(EditorInfo.IME_ACTION_SEND);
      mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
          boolean handled = false;
          if (actionId == EditorInfo.IME_ACTION_SEND){
            mListener.onText(mEditText.getText().toString());
            mEditText.setText("");
            handled = true;
          }
          return handled;
        }
      });
    } else {
      mEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
          InputType.TYPE_TEXT_FLAG_MULTI_LINE |
          InputType.TYPE_CLASS_TEXT);
    }
  }

  public void disable(@Nullable String withMessage) {
    hideRecyclerView(false);
    hideEmojiTitleView(false);
    hideKeyboard();
    mSendButton.setVisibility(GONE);
    mSmile.setVisibility(GONE);
    mEditText.setFocusable(false);
    mEditText.setEnabled(false);
    mEditText.setText(withMessage);
  }

  public void enable() {
    mSendButton.setVisibility(VISIBLE);
    mSmile.setVisibility(VISIBLE);
    mEditText.setFocusable(true);
    mEditText.setEnabled(true);
    mEditText.setText("");
    mEditText.setFocusableInTouchMode(true);
  }

  public void setVisibilityOfSendButton(boolean b) {
    mSendButton.setVisibility(b ? View.VISIBLE: View.GONE);
  }

  private int getColor(int cr) {
    return getContext().getResources().getColor(cr);
  }

  private class EmojiEventListener implements OnEmojiPopupShownListener, OnSoftKeyboardOpenListener, OnSoftKeyboardCloseListener, OnEmojiPopupDismissListener, EmojiPopup.EmojiPagerScrollListener, EmojiTitleView.EmojiTitleListener {
    @Override public void onEmojiPopupShown() {
      //smileIconState(true);
      hideRecyclerView(false);
    }

    @Override public void onKeyboardOpen(int keyBoardHeight) {
      //smileIconState(mEmojiPopup.isShowing());
      Timber.d("::: onKeyboardOpen");
      hideRecyclerView(mEmojiPopup.isShowing());
    }

    @Override public void onEmojiPopupDismiss() {
      smileIconState(mEmojiPopup.isShowing());
    }

    @Override public void onKeyboardClose() {
      mEmojiPopup.dismiss();
      hideEmojiTitleView(false);
    }

    @Override public void onScroll(int pageIndex) {
      mEmojiTitle.select(pageIndex);
    }

    @Override
    public void onPositionSelected(int p) {
      mEmojiPopup.setPage(p);
      //smileIconState(mEmojiPopup.isShowing());
    }

    @Override
    public void onBackspace() {
      mEditText.backspace();
    }
  }

  private TextWatcher mTextWatcher = new TextWatcher() {
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override public void afterTextChanged(Editable s) {
      final int icon = mEditText.getText().toString().isEmpty() ?
          R.drawable.ic_attachment_black_24dp : R.drawable.ic_send_grey_600_24dp;
      if (icon != mSendButtonLastIcon)
        AnimationUtils.scaleDownScaleUp(mSendIcon, 0.1f, 1f, 100, 100, new Runnable() {
          @Override
          public void run() {
            setSendIcon();
            mSendButtonLastIcon = icon;
          }
        },null);
      if (mRecyclerView.getVisibility() == VISIBLE) {
        hideRecyclerView(false);
      }
    }
  };

  public void showEmojiTitleView(boolean withAnimation) {
    mEmojiTitle.setVisibility(VISIBLE);
    mBottomDivider.setVisibility(VISIBLE);
  }

  public void hideEmojiTitleView(boolean withAnimation) {
    mBottomDivider.setVisibility(GONE);
    mEmojiTitle.setVisibility(GONE);
  }

  private void setSendIcon() {
    if (mRecyclerView.getVisibility() == VISIBLE) {
      mSendIcon.setImageResource(R.drawable.ic_close_blue_24dp);
    } else {
      if (mEditText.getText().toString().isEmpty()) {
        mSendIcon.setImageResource(R.drawable.ic_attachment_black_24dp);
      } else {
        mSendIcon.setImageResource(R.drawable.ic_send_grey_600_24dp);
      }
    }
  }

  public void setText(String text) {
    mEditText.setText(text);
  }

  public void setHint(String text) {
    mEditText.setHint(text);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
    EmojiEventListener mEmojiPopupEventListener = new EmojiEventListener();
    mEmojiPopup = EmojiPopup.Builder.fromRootView(mRoot)
        .setOnEmojiPopupShownListener(mEmojiPopupEventListener)
        .setOnSoftKeyboardOpenListener(mEmojiPopupEventListener)
        .setOnSoftKeyboardCloseListener(mEmojiPopupEventListener)
        .setOnEmojiPopupDismissListener(mEmojiPopupEventListener)
        .build(mEditText);
    mEmojiPopup.setEmojiPagerScroolListener(mEmojiPopupEventListener);
    mEmojiTitle.setSelectedItemColor(getColor(R.color.colorSecondary));
    mEmojiTitle.setUnselectedItemColor(getColor(R.color.materialGrey));
    mEmojiTitle.setEmojiTitleListener(mEmojiPopupEventListener);
    mEmojiTitle.select(mEmojiPopup.getStartIndex());
    hideEmojiTitleView(false);
    hideRecyclerView(false);
    mEditText.addTextChangedListener(mTextWatcher);
    mEditText.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        hideRecyclerView(false);
        if (mEmojiPopup.isShowing()) {
          mSmile.callOnClick();
        }
      }
    });
    mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          hideRecyclerView(false);
        }
      }
    });
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    mRecyclerView.setAdapter(new AttachmentAdapter(this));
  }

  @OnClick({R.id.send, R.id.smile})
  public void onClick(View v) {
    if  (v.getId() == R.id.send) {
      AnimationUtils.scaleDownScaleUp(v, 0.5f, 1f, 50, 50);
      if (mEditText.getText().toString().isEmpty()) {
        if (mRecyclerView.getVisibility() == GONE)
          showRecyclerView(true);
        else
          hideRecyclerView(true);
      } else {
        if (mListener !=null )mListener.onText(mEditText.getText().toString());
        mEditText.setText("");
      }
    } else {

      if (mEmojiPopup.isShowing()) {
        hideEmojiTitleView(true);
      } else {
        showEmojiTitleView(true);
      }
      smileIconState(!mEmojiPopup.isShowing());

      showKeyboard();

      AnimationUtils.scaleDownScaleUp(mSmileIcon, 0.1f, 1f, 50, 100);
      mEmojiPopup.toggle();
    }
  }

  private void showKeyboard() {
    if (!mEmojiPopup.isShowing()) {
      mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
  }

  private void smileIconState(boolean isShowing) {
    if (isShowing) {
      mSmileIcon.setImageResource(R.drawable.ic_keyboard_grey_600_24dp);
    } else {
      mSmileIcon.setImageResource(R.drawable.ic_insert_emoticon_grey_600_24dp);
    }
  }

  public void hideRecyclerView(boolean withAnimation) {
    Timber.d(":::will hide!");
    if (mRecyclerView.getVisibility() != View.GONE) {
      mRecyclerView.setVisibility(GONE);
    }
    setSendIcon();
    if (mListDivider.getVisibility() != GONE) {
      mListDivider.setVisibility(GONE);
    }
  }

  public void showRecyclerView(boolean withAnimation) {
    mRecyclerView.setVisibility(VISIBLE);
    setSendIcon();

    mListDivider.setVisibility(VISIBLE);

    if (mEmojiPopup.isShowing()) {
      mEmojiPopup.toggle();
    }
    hideEmojiTitleView(false);
    hideKeyboard();
  }

  public void hideKeyboard() {
    View view = mActivity.getCurrentFocus();
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  public void setChatInputListener(ChatInputListener l) {
    mListener = l;
  }

  public void setActivityReference(Activity a) {
    mActivity = a;
  }

  private static List<NotifierConstantUtils.Component> getAdapterItems(Context c) {
    List<NotifierConstantUtils.Component> packets = NotifierConstantUtils.getCutePackets();
    NotifierConstantUtils.Component camera = new NotifierConstantUtils.Component(-100, null, c.getString(R.string.camera), R.drawable.ic_camera_grey_600_24dp);
    NotifierConstantUtils.Component gallery = new NotifierConstantUtils.Component(-200, null, c.getString(R.string.gallery), R.drawable.ic_insert_photo_black_24dp);
    List<NotifierConstantUtils.Component> components = new ArrayList<>();
    components.add(camera);
    components.add(gallery);
    components.add(null); // packets
    components.addAll(packets);
    return components;
  }



  private static class AttachmentAdapter extends RecyclerView.Adapter {
    final int PACKETS_TITLE = 3;
    final int ITEM = 2;
    List<NotifierConstantUtils.Component> mComponents;
    NewChatInput mView;
    AttachmentAdapter(NewChatInput c) {
      mComponents = getAdapterItems(c.getContext());
      mView = c;
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater i = LayoutInflater.from(parent.getContext());
      if (viewType == PACKETS_TITLE) {
        return new TitleHolder(i.inflate(R.layout.title_view_row, parent, false), this);
      } else {
        return new ItemHolder(i.inflate(R.layout.camera_gallery_special_packet_holder, parent, false), this);
      }
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
      if (holder instanceof TitleHolder){
        ((TitleHolder) holder).withPosition(position);
      } else {
        ((ItemHolder) holder).withComponent(mComponents.get(position));
      }
    }

    @Override public int getItemCount() {
      return mComponents.size();
    }

    @Override
    public int getItemViewType(int position) {
     if (position == 2) {
        return PACKETS_TITLE;
      } else {
        return ITEM;
      }
    }

    public static class TitleHolder extends RecyclerView.ViewHolder {
      TitleView mTitleView;
      AttachmentAdapter mRef;
      public TitleHolder(View itemView, AttachmentAdapter a) {
        super(itemView);
        mTitleView = (TitleView) itemView.findViewById(R.id.title_view);
        mRef = a;
      }
      void withPosition(int p) {
        Context c = mTitleView.getContext();
        int li = p == 0 ? R.drawable.ic_camera_white_18dp: R.drawable.ic_select_all_red_600_18dp;
        String title = p == 0 ? c.getString(R.string.send_a_picture) : c.getString(R.string.send_an_packet);
        int mg = c.getResources().getColor(R.color.materialGrey);
        mTitleView.setContentColor(mg);
        mTitleView.setLeftIconColor(mg);
        mTitleView.setContent(title);
        mTitleView.setLeftIcon(li);
        mTitleView.hideRightSide();
        mTitleView.setRightIcon(R.drawable.ic_help_black_18dp);
        mTitleView.setRightIconColor(mg);
        mTitleView.hideLeftSide();
      }
    }

    public static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      TextView mText;
      ImageView mIcon;
      AttachmentAdapter mRef;
      public ItemHolder(View itemView, AttachmentAdapter c) {
        super(itemView);
        mRef = c;
        mIcon = (ImageView) itemView.findViewById(R.id.icon);
        mText = (TextView) itemView.findViewById(R.id.title);
      }
      public void withComponent(NotifierConstantUtils.Component component) {
        mIcon.setImageResource(component.icon);
        mText.setText(component.title!=null?component.title: NotifierConstantUtils.getPacketTitleForSent(mText.getContext(), component.uid));
        itemView.setOnClickListener(this);
      }

      @Override
      public void onClick(View v) {
        int p = getAdapterPosition();
        if (p == -1) return;
        if (mRef.mView.mListener != null) {
          if (p == 0) {
            mRef.mView.mListener.onCamera();
          } else if (p == 1) {
            mRef.mView.mListener.onGallery();
          } else {
            NotifierConstantUtils.Component component = mRef.mComponents.get(p);
            if (component.uid == Packets._LOCATION) {
              mRef.mView.mListener.onLocation();
            } else {
              mRef.mView.mListener.onPacket(component.uid);
            }

          }
        }
        mRef.mView.hideRecyclerView(false);
      }
    }
  }

}
