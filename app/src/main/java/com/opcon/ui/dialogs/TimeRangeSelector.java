package com.opcon.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.erz.timepicker_library.TimePicker;
import com.opcon.R;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mahmut Taşkiran on 15/10/2016.
 */

public class    TimeRangeSelector extends Dialog {

    private TimePicker tpFrom;
    private TimePicker tpTo;

  private OnTimeRangeSelectListener listener;

    public TimeRangeSelector(Context context, OnTimeRangeSelectListener listener) {
        this(context, listener, null, null);
    }

    public TimeRangeSelector(Context context, OnTimeRangeSelectListener listener, Date from, Date to) {
        super(context);
      this.listener = listener;
        setContentView(R.layout.fragment_timerange_restrict);

      Window window = getWindow();
      if (window != null) {
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(attributes);
      }


        tpFrom = (TimePicker) findViewById(R.id.fragmenttimerestrict_from);
        tpTo = (TimePicker) findViewById(R.id.fragmenttimerestrict_to);
      CardView rlDone = (CardView) findViewById(R.id.fragmenttimerestrict_select);

        tpFrom.enableTwentyFourHour(true);
        tpTo.enableTwentyFourHour(true);

        rlDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TimeRangeSelector.this.listener != null) {
                  TimeRangeSelector.this.listener.onSelectTimeRange(tpFrom.getTime(), tpTo.getTime());
                    dismiss();
                }
            }
        });


      if (from == null) {
        from = new Date(System.currentTimeMillis());
      }

      if (to == null){
        to = new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS));
      }

      tpFrom.setTime(from);
      tpTo.setTime(to);

    }


    public interface OnTimeRangeSelectListener {
        void onSelectTimeRange(Date from, Date to);
    }
}
