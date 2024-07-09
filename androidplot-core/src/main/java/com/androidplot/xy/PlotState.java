package com.androidplot.xy;

public class PlotState {
    private boolean block;
    private XYPlot plot = null;
    private boolean scrollNonBlock;

    public PlotState(boolean block)
    {
        this.block = block;
        this.scrollNonBlock = false;
    }

    public PlotState(boolean block, boolean scrollNonBlock)
    {
        this.block = block;
        this.scrollNonBlock = scrollNonBlock;
    }

    public void setScrollNonBlock(boolean scrollNonBlock) {
        this.scrollNonBlock = scrollNonBlock;
    }

    public boolean isScrollNonBlock() {
        return scrollNonBlock;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public void setPlot(XYPlot plot){
        this.plot = plot;
    }

    public XYPlot getPlot() {
        return this.plot;
    }
}
