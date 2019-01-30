package com.vanniktech.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vanniktech.emoji.emoji.Cars;
import com.vanniktech.emoji.emoji.Electronics;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.emoji.Food;
import com.vanniktech.emoji.emoji.Nature;
import com.vanniktech.emoji.emoji.People;
import com.vanniktech.emoji.emoji.Sport;
import com.vanniktech.emoji.emoji.Symbols;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;
import com.vanniktech.emoji.listeners.RepeatListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressLint("ViewConstructor")
final class EmojiView extends LinearLayout implements ViewPager.OnPageChangeListener {
    private static final int BACKGROUND_COLOR = 0xffeceff1;

    private static final int RECENT_INDEX = 0;
    private static final int PEOPLE_INDEX = 1;
    private static final int NATURE_INDEX = 2;
    private static final int FOOD_INDEX = 3;
    private static final int SPORT_INDEX = 4;
    private static final int CARS_INDEX = 5;
    private static final int ELECTRONICS_INDEX = 6;
    private static final int SYMBOLS_INDEX = 7;

    private static final long INITIAL_INTERVAL = TimeUnit.SECONDS.toMillis(1) / 2;
    private static final int NORMAL_INTERVAL = 50;

    @ColorInt private final int themeAccentColor;
    @Nullable OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;

    private int emojiTabLastSelectedIndex = -1;

    private final ImageView[] emojiTabs;
    private int mStartIndex;
    private RecentEmojiGridView recentGridView;

    private ViewPager emojisPager;

    EmojiView(final Context context, final OnEmojiClickedListener onEmojiClickedListener, @NonNull final RecentEmoji recentEmoji) {
        super(context);

        View.inflate(context, R.layout.emoji_view, this);

        setOrientation(VERTICAL);
        setBackgroundColor(BACKGROUND_COLOR);

        emojisPager = (ViewPager) findViewById(R.id.emojis_pager);
        emojisPager.addOnPageChangeListener(this);

        final List<? extends View> views = getViews(context, onEmojiClickedListener, recentEmoji);
        final EmojiPagerAdapter emojisAdapter = new EmojiPagerAdapter(views);
        emojisPager.setAdapter(emojisAdapter);

        emojiTabs = new ImageView[SYMBOLS_INDEX + 1];
        emojiTabs[RECENT_INDEX] = (ImageView) findViewById(R.id.emojis_tab_0_recent);
        emojiTabs[PEOPLE_INDEX] = (ImageView) findViewById(R.id.emojis_tab_1_people);
        emojiTabs[NATURE_INDEX] = (ImageView) findViewById(R.id.emojis_tab_2_nature);
        emojiTabs[FOOD_INDEX] = (ImageView) findViewById(R.id.emojis_tab_3_food);
        emojiTabs[SPORT_INDEX] = (ImageView) findViewById(R.id.emojis_tab_4_sport);
        emojiTabs[CARS_INDEX] = (ImageView) findViewById(R.id.emojis_tab_5_cars);
        emojiTabs[ELECTRONICS_INDEX] = (ImageView) findViewById(R.id.emojis_tab_6_electronics);
        emojiTabs[SYMBOLS_INDEX] = (ImageView) findViewById(R.id.emojis_tab_7_symbols);

        handleOnClicks(emojisPager);

        findViewById(R.id.emojis_backspace).setOnTouchListener(new RepeatListener(INITIAL_INTERVAL, NORMAL_INTERVAL, new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (onEmojiBackspaceClickListener != null) {
                    onEmojiBackspaceClickListener.onEmojiBackspaceClicked(view);
                }
            }
        }));

        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        themeAccentColor = value.data;

        final int startIndex = recentGridView.numberOfRecentEmojis() > 0 ? RECENT_INDEX : PEOPLE_INDEX;
        mStartIndex = startIndex;

        emojisPager.setCurrentItem(startIndex);
        onPageSelected(startIndex);
    }


    private void handleOnClicks(final ViewPager emojisPager) {
        for (int i = 0; i < emojiTabs.length; i++) {
            final int position = i;
            emojiTabs[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    emojisPager.setCurrentItem(position);
                }
            });
        }
    }

    public void setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener onEmojiBackspaceClickListener) {
        this.onEmojiBackspaceClickListener = onEmojiBackspaceClickListener;
    }

    @NonNull
    private List<? extends View> getViews(final Context context, @Nullable final OnEmojiClickedListener onEmojiClickedListener, @NonNull final RecentEmoji recentEmoji) {
        recentGridView = new RecentEmojiGridView(context).init(onEmojiClickedListener, recentEmoji);
        final EmojiGridView peopleGridView = createGridView(context, People.DATA, onEmojiClickedListener);
        final EmojiGridView natureGridView = createGridView(context, Nature.DATA, onEmojiClickedListener);
        final EmojiGridView foodGridView = createGridView(context, Food.DATA, onEmojiClickedListener);
        final EmojiGridView sportGridView = createGridView(context, Sport.DATA, onEmojiClickedListener);
        final EmojiGridView carsGridView = createGridView(context, Cars.DATA, onEmojiClickedListener);
        final EmojiGridView electronicsGridView = createGridView(context, Electronics.DATA, onEmojiClickedListener);
        final EmojiGridView symbolsGridView = createGridView(context, Symbols.DATA, onEmojiClickedListener);
        return Arrays.asList(recentGridView, peopleGridView, natureGridView, foodGridView, sportGridView, carsGridView, electronicsGridView, symbolsGridView);
    }

    private EmojiGridView createGridView(@NonNull final Context context, final Emoji[] emojis, @Nullable final OnEmojiClickedListener onEmojiClickedListener) {
        final EmojiGridView emojiGridView = new EmojiGridView(context);
        final EmojiArrayAdapter emojiArrayAdapter = new EmojiArrayAdapter(getContext(), emojis);
        emojiArrayAdapter.setOnEmojiClickedListener(onEmojiClickedListener);
        emojiGridView.setAdapter(emojiArrayAdapter);
        return emojiGridView;
    }

    @Override
    public void onPageSelected(final int i) {

        if (listener != null)
            listener.onScroll(i);

        if (emojiTabLastSelectedIndex != i) {
            if (i == RECENT_INDEX) {
                recentGridView.invalidateEmojis();
            }

            switch (i) {
                case RECENT_INDEX:
                case PEOPLE_INDEX:
                case NATURE_INDEX:
                case FOOD_INDEX:
                case SPORT_INDEX:
                case CARS_INDEX:
                case ELECTRONICS_INDEX:
                case SYMBOLS_INDEX:
                    if (emojiTabLastSelectedIndex >= 0 && emojiTabLastSelectedIndex < emojiTabs.length) {
                        emojiTabs[emojiTabLastSelectedIndex].setSelected(false);
                        emojiTabs[emojiTabLastSelectedIndex].clearColorFilter();
                    }

                    emojiTabs[i].setSelected(true);
                    emojiTabs[i].setColorFilter(themeAccentColor, PorterDuff.Mode.SRC_IN);

                    emojiTabLastSelectedIndex = i;
                    break;
                default:
                    break;
            }
        }
    }

    EmojiPopup.EmojiPagerScrollListener listener;

    public void setEmojiPagerListener(EmojiPopup.EmojiPagerScrollListener listener) {
        this.listener  = listener;
    }

    public void setPageIndex(int index ) {
        emojisPager.setCurrentItem(index);
    }

    public int getStartIndex() {
        return mStartIndex;
    }

    @Override
    public void onPageScrolled(final int i, final float v, final int i2) {
        // Don't care
    }

    @Override
    public void onPageScrollStateChanged(final int i) {
        // Don't care
    }
}
