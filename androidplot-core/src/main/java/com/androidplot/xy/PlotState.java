package com.androidplot.xy;

public class PlotState {
    private boolean block;
    private XYPlot plot = null;

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

    public void setPlot(XYPlot plot){
        this.plot = plot;
    }

    public XYPlot getPlot() {
        return this.plot;
    }
}
