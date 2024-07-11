package com.androidplot.xy;

public class ZoomState {
    public interface ZoomDelegate {
        PanZoom.ZoomFactor getZoomFactor(ZoomState state);
        void setZoomFactor(PanZoom.ZoomFactor zoomFactor);
        PanZoom.Pan getPan(ZoomState state);
        void setPan(PanZoom.Pan pan);
        PanZoom.Zoom getZoom(ZoomState state);
        void setZoom(PanZoom.Zoom zoom);
    }

    public ZoomDelegate delegate;
    private boolean block;

    public ZoomState(boolean block, ZoomDelegate delegate)
    {
        this.block = block;
        this.delegate = delegate;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }
}
