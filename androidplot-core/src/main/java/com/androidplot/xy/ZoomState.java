package com.androidplot.xy;

public class ZoomState {
    public interface ZoomDelegate {
        PanZoom.ZoomFactor getZoomFactor(ZoomState state);
        PanZoom.Pan getPan(ZoomState state);
        PanZoom.Zoom getZoom(ZoomState state);
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
