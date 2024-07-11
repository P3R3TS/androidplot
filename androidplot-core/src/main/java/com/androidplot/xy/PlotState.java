package com.androidplot.xy;

public class PlotState {
    private boolean block;

    public PlotState(boolean block)
    {
        this.block = block;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

}
