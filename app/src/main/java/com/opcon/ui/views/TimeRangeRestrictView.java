package com.opcon.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.opcon.R;
import com.opcon.ui.dialogs.TimeRangeSelector;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by Mahmut Taşkiran on 15/10/2016.
 */

public class TimeRangeRestrictView extends RelativeLayout implements TimeRangeSelector.OnTimeRangeSelectListener{

    private static final String FORMAT_TEXT = "<font color='red'><b>%02d:%02d</b></font> -> <font color='blue'><b>%02d:%02d</b></font>";

    @BindView(R.id.timeRestrict)
    TitleView mTitleView;

    private Date from, to;

    public TimeRangeRestrictView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View rootView = LayoutInflater.from(getContext())
                .inflate(R.layout.restrict_time_range, this, true);
        ButterKnife.bind(this, rootView);
        mTitleView.setRightIconClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime(null,null);
            }
        });
        mTitleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeRangeSelector timeRangeSelector;
                if (from != null && to != null) {
                    timeRangeSelector = new TimeRangeSelector(getContext(), TimeRangeRestrictView.this, from, to);
                } else {
                    timeRangeSelector = new TimeRangeSelector(getContext(), TimeRangeRestrictView.this);
                }
                timeRangeSelector.show();
            }
        });
    }

    public Date getFrom() {
        return this.from;
    }

    public Date getTo() {
        return this.to;
    }


    @Override
    public void onSelectTimeRange(Date from, Date to) {
        setTime(from, to);
    }

    public void setTime(Date from, Date to) {
        if (from == null || to == null) {

            mTitleView.setRightIcon(R.drawable.ic_help_black_18dp);
            mTitleView.setRightIconColor(getContext().getResources().getColor(R.color.materialGrey));
            mTitleView.setRightIconStrokeColor(getContext().getResources().getColor(R.color.materialGrey));
            mTitleView.setContent(getContext().getString(R.string.time_restrict_date));
            mTitleView.hideRightSide();
            this.from = null;
            this.to = null;
        } else {
            mTitleView.setRightIcon(R.drawable.ic_close_black_18dp);
            mTitleView.setRightIconColor(Color.RED);
            mTitleView.showRightSide();
            mTitleView.setRightIconStrokeColor(Color.RED);
            mTitleView.setContent(Html.fromHtml(String.format(FORMAT_TEXT, from.getHours(), from.getMinutes(), to.getHours(), to.getMinutes())));

            this.from = from;
            this.to = to;
        }
    }

}
