package com.androidplot.xy;

public class ZoomState {
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
