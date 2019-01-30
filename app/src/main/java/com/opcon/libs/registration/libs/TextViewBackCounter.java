package com.opcon.libs.registration.libs;

import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mahmut Taşkiran on 1/24/17.
 *
 */

public class TextViewBackCounter extends TimerTask {
    private long time;
    private Timer timer;
    private BackCounterListener listener;
    private WeakReference<TextView> textView;
    private boolean state;

    public interface BackCounterListener {
        void endOfBackCount();
    }

    public TextViewBackCounter(long time, WeakReference<TextView> textView, BackCounterListener listener) {
        this.time = time;
        this.timer = new Timer();
        this.textView = textView;
        this.listener = listener;
    }

    public void start() {
        state = true;
        timer.schedule(this, 0, TimeUnit.SECONDS.toMillis(1));
        TextView tv = textView.get();
        if (tv!=null){
            tv.setVisibility(View.VISIBLE);
        }
    }

    public void stop() {
        state = false;
        if (timer != null)
            this.timer.cancel();

        this.textView = null;
        this.timer = null;
        this.cancel();
    }

    @Override
    public synchronized void run() {
        time -= TimeUnit.SECONDS.toMillis(1);

        if (!state || timer == null|| textView == null) {
            return;
        }

        TextView tv = textView.get();
        if (tv !=null)
        {
            tv.post(new Runnable() {
                @Override public void run() {
                    long second = (time / 1000) % 60;
                    long minute = (time / (1000 * 60)) % 60;
                    long hour = (time / (1000 * 60 * 60)) % 24;

                    String time;

                    if (minute == 0) {
                        time = String.format("%02d", second);
                    } else if (hour == 0) {
                        time = String.format("%02d:%02d", minute, second);
                    } else {
                        time = String.format("%02d:%02d:%02d", hour, minute, second);
                    }

                    TextView tv = textView.get();
                    if (textView != null && tv !=null)
                        tv.setText(time);
                }
            });

            if (time <= 1 && listener != null && textView != null) {
                tv.post(new Runnable() {
                    @Override public void run() {
                        TextView tv = textView.get();
                        if (tv!=null){
                            tv.setVisibility(View.GONE);
                        }
                        stop();
                        listener.endOfBackCount();
                    }
                });
            }
        }
    }

    public void setTime(long time) {
        this.time = time;
    }
}
