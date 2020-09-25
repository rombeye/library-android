package com.carlosmuvi.segmentedprogressbar;

import android.os.Handler;
import android.util.Log;

/**
 * Created by carlosmuvi on 02/09/16.
 */

public class DrawingTimer {

    private final Handler handler;
    private final long tickTimeInMilliseconds = 30;
    private int totalTicks;
    private int currentTick = 0;
    private Listener listener;
    private TimerState timerState = TimerState.IDLE;

    public DrawingTimer() {
        handler = new Handler();
    }

    public void start(long timeInMilliseconds) {
        if (timerState == TimerState.IDLE) {
            this.totalTicks = (int) (timeInMilliseconds / tickTimeInMilliseconds);
        }
        if (timerState != TimerState.RUNNING) {
            timerState = TimerState.RUNNING;
            runDrawingTask();
        }
    }

    private void runDrawingTask() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onTick(currentTick, totalTicks);
                currentTick++;
                if (currentTick <= totalTicks) {
                    handler.postDelayed(this, tickTimeInMilliseconds);
                } else {
                    Log.d("finished", "run: ");
                    reset();
                    listener.onFinish();
                }
            }
        });
    }

    public void pause() {
        if (timerState == TimerState.RUNNING) {
            timerState = TimerState.PAUSED;
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void resume() {
        if (timerState == TimerState.PAUSED) {
            timerState = TimerState.RUNNING;
            runDrawingTask();
        }
    }

    public void reset() {
        pause();
        timerState = TimerState.IDLE;
        currentTick = 0;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isRunning() {
        return timerState == TimerState.RUNNING;
    }

    public boolean isPaused() {
        return timerState == TimerState.PAUSED;
    }

    public boolean isIdle() {
        return timerState == TimerState.IDLE;
    }

    enum TimerState {
        RUNNING, PAUSED, IDLE
    }

    interface Listener {
        void onTick(int currentTicks, int totalTicks);

        void onFinish();
    }
}
