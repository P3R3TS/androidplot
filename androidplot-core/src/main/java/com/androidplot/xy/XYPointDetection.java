package com.androidplot.xy;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class XYPointDetection implements View.OnTouchListener{
    private boolean enable;
    private State state = State.NONE;
    private XYPointDetect onPointDetect;

    private Number lastX;

    public enum State{
        NONE,
        READY,
        SHOW,
    }

    public interface XYPointDetect{
        /**
         * @param x xScreen
         * @param xVal xValue on graph
         */
        void onPointXClick(State state, double x, double xVal);
    }

    public XYPointDetection(XYPointDetect onPointDetect)
    {
        enable = true;
        this.onPointDetect = onPointDetect;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if(isEnable())
        {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getX();
                    state = State.READY;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    state = State.NONE;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(lastX != null && Math.abs(lastX.floatValue() - event.getX()) > 10)
                        state = State.NONE;
                    break;
                case MotionEvent.ACTION_UP:
                    if(state == State.READY) state = State.SHOW;
                    break;
            }
            if(state == State.SHOW || state == State.NONE)
            {
                if(view instanceof XYPlot){
                    lastX = null;
                    XYPlot plot = (XYPlot) view;
                    float xPoint = event.getX();

                    Number x = plot.getGraph().screenToSeriesX(xPoint);

                    if(x != null)
                    {
                        onPointDetect.onPointXClick(state, xPoint, x.doubleValue());
                    }
                }
            }
        }
        return false;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }
}
