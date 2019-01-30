package com.opcon.ui.views;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;

import com.opcon.R;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Mahmut Taşkiran on 15/10/2016.
 *
 */

public class DateRestrictView extends RelativeLayout {

    @BindView(R.id.dateRestrict)
    TitleView mTitleView;

    private int year = -1, month = -1, day = -1;

    public DateRestrictView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ButterKnife.bind(this, LayoutInflater.from(getContext()).inflate(R.layout.restrict_date, this, true));

        mTitleView.setRightIconClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(-1, -1, -1);
            }
        });
    }

    @OnClick(R.id.dateRestrict)
    public void onClick(View v) {
        final int year, month, day;

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                setDate(year, month, dayOfMonth);
            }
        };

        DatePickerDialog datePickerDialog;

        if (DateRestrictView.this.year == -1) {
            datePickerDialog = new DatePickerDialog(getContext(), listener, year, month, day);
        } else {
            datePickerDialog = new DatePickerDialog(getContext(), listener, DateRestrictView.this.year, DateRestrictView.this.month, DateRestrictView.this.day);
        }

        datePickerDialog.show();
    }

    public void setDate(int year, int month, int day) {
        if (year == -1 && month == -1 && day == -1)  {
            this.day = -1;
            this.year = -1;
            this.month = -1;
            mTitleView.setContent(getContext().getString(R.string.time_restrict_date));
            mTitleView.setRightIcon(R.drawable.ic_help_black_18dp);
            mTitleView.setRightIconColor(getContext().getResources().getColor(R.color.materialGrey));
            mTitleView.setRightIconStrokeColor(getContext().getResources().getColor(R.color.materialGrey));
            mTitleView.hideRightSide();
        } else {
            DateRestrictView.this.year = year;
            DateRestrictView.this.month = month;
            DateRestrictView.this.day = day;

            // fix [OP-00001]

            mTitleView.setContent(Html.fromHtml(String.format("<font color='red'><b>%02d/%02d/%04d</b></font>", day, month + 1, year)));
            mTitleView.setRightIcon(R.drawable.ic_close_black_18dp);
            mTitleView.setRightIconColor(Color.RED);
            mTitleView.setRightIconStrokeColor(Color.RED);
            mTitleView.showRightSide();
        }
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }
}
