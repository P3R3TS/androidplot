package com.androidplot.xy;

public abstract class ZoomDelegate {
    public abstract PanZoom.ZoomFactor getZoomFactor(ZoomState state);
    void setZoomFactor(PanZoom.ZoomFactor zoomFactor) {};
    public abstract  PanZoom.Pan getPan(ZoomState state);
    void setPan(PanZoom.Pan pan) {};
    public abstract PanZoom.Zoom getZoom(ZoomState state);
    void setZoom(PanZoom.Zoom zoom) {};
}
