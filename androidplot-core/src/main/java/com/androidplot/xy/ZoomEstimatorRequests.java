package com.androidplot.xy;

public interface ZoomEstimatorRequests{
    double getMaxZoomFactor();
    void setZoomFactor(double factor);
    RectRegion getBounds();
}
