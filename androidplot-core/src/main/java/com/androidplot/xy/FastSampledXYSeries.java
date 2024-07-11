package com.androidplot.xy;

import com.androidplot.Region;

public class FastSampledXYSeries implements FastXYSeries, OrderedXYSeries {
    private int threshold;
    private FastXYSeries rawData;
    private float ratio;
    private XOrder xOrder;
    private double scaleFactor;
    private int leftOffset;
    private double ratioFactor;
    private int sizePoints;
    private XYPlot plot;
    private double lastScaleFactor = 1;

    public interface RedrawCallback{
        void redraw();
    }

    public interface AutoRedrawable{
        void setBound(Region region);
        Region getBound();
        void setPlot(XYPlot plot);
        void setScaleFactor(double rlog);
    }

    @Override
    public RectRegion minMax() {
        return rawData.minMax();
    }

    public FastSampledXYSeries(FastXYSeries rawData, XOrder xOrder, int threshold)
    {
        this.rawData = rawData;
        this.ratio = 2;
        this.threshold = threshold;
        this.xOrder = xOrder;
        this.ratioFactor = Math.log(this.ratio);
    }

    protected void setZoomBounds(RectRegion bounds)
    {
        double leftOffset = bounds.getxRegion().getMin().doubleValue();
        double rightOffset = bounds.getxRegion().getMax().doubleValue();
        double deltaBound = rightOffset - leftOffset;
        int newThreshold = (threshold - 1);

        if(deltaBound <= newThreshold){
            this.scaleFactor = 1;
        } else {
            this.scaleFactor = Math.pow(this.ratio,(int)((Math.log(deltaBound / newThreshold) / ratioFactor + 1)));
        }

        int startOffset;
        try {
            startOffset = minMax().getxRegion().getMin().intValue();
        } catch (NullPointerException e)
        {
            startOffset = 0;
        }

        this.leftOffset = (int)(leftOffset - (leftOffset % this.scaleFactor)) - startOffset;
        int right = (int)(rightOffset + this.scaleFactor - (rightOffset % this.scaleFactor)) - startOffset;
        if(this.leftOffset < 0) this.leftOffset = 0;
        this.sizePoints = (int) ((right - this.leftOffset) / this.scaleFactor);
        if(rawData instanceof AutoRedrawable) {
            ((AutoRedrawable) rawData).setBound(bounds.getxRegion());
            if(this.lastScaleFactor != this.scaleFactor){
                this.lastScaleFactor = this.scaleFactor;
                ((AutoRedrawable) rawData).setScaleFactor(this.scaleFactor);
            }
        }
    }

    public void setPlot(XYPlot plot)
    {
        if(this.plot != plot){
            if(rawData instanceof AutoRedrawable) ((AutoRedrawable)rawData).setPlot(plot);
            this.plot = plot;
        }
    }

    public double getScaleFactor(){
        return this.scaleFactor;
    }

    @Override
    public int size() {
        return sizePoints + 1;//threshold + 1;
    }

    @Override
    public Number getX(int index) {
        return rawData.getX((int) (index * this.scaleFactor + this.leftOffset));
    }


    @Override
    public Number getY(int index) {
        return rawData.getY((int) (index * this.scaleFactor + this.leftOffset));
    }

    @Override
    public String getTitle() {
        return rawData.getTitle();
    }

    @Override
    public XOrder getXOrder() {
        return this.xOrder;
    }
}
