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
    }

    public double calculateEndOffset(int endX, double scaleFactor)
    {
        return (endX - (endX % scaleFactor));
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
