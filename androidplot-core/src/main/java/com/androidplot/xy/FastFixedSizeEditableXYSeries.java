package com.androidplot.xy;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidplot.Region;
import com.androidplot.util.FastNumber;

import java.util.ArrayList;
import java.util.List;

/**
 * An efficient implementation of {@link EditableXYSeries} intended for use cases where
 * the total number of points visible is known ahead of time and is fairly static.
 */
public class FastFixedSizeEditableXYSeries implements FastXYSeries, EditableXYSeries, FastSampledXYSeries.AutoRedrawable {
    private final RectRegion rectRegion = new RectRegion();
    private int size;
    private Region bound = null;
    private XYPlot plot = null;
    private double scaleFactor = 1;

    @NonNull
    private List<FastNumber> xVals = new ArrayList<>();

    @NonNull
    private List<FastNumber> yVals = new ArrayList<>();
    private String title;
    //private int lastVisibleIndex = 0;

    public FastFixedSizeEditableXYSeries(String title, int size) {
        setTitle(title);
        resize(size);
    }

    public void setXY(@Nullable Number x, @Nullable Number y, int index) {
        rectRegion.union(x, y);
        xVals.set(index, FastNumber.orNull(x));
        yVals.set(index, FastNumber.orNull(y));
    }

    public void setXYAndRedraw(@Nullable Number x, @Nullable Number y, int index) {
        rectRegion.union(x, y);
        xVals.set(index, FastNumber.orNull(x));
        yVals.set(index, FastNumber.orNull(y));

        if (this.plot != null) {
            if(bound != null && bound.contains(x.intValue() - 2) && plot.getOuterLimits().getMaxX().doubleValue() >= x.longValue()) this.plot.redraw();
            resamplePan(x.doubleValue());
        }
    }

    private void resamplePan(double x)
    {
        try {
            int lastVisibleIndex = (int) (x - (x % scaleFactor));
            if (plot.getOuterLimits().getMaxX().doubleValue() < getX(lastVisibleIndex).doubleValue()) {
                boolean isBoundaries = plot.isBoundariesFrom(XYPlot.MoveType.Right);
                plot.getOuterLimits().setMaxX(getX(lastVisibleIndex).doubleValue());
                if (isBoundaries) {
                    plot.moveBoundariesTo(XYPlot.MoveType.Right, BoundaryMode.FIXED);
                    plot.redraw();
                }
            }
        } catch (NullPointerException e)
        {
            // lastIndex is null
        }
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int size() {
        return xVals.size();
    }

    @Override
    public Number getX(int index) {
        if (index >= this.size) return null;
        return xVals.get(index);
    }

    @Override
    public Number getY(int index) {
        if (index >= this.size) return null;
        return yVals.get(index);
    }

    @Override
    public RectRegion minMax() {
        return rectRegion;
    }

    @Override
    public void setX(Number x, int index) {
        rectRegion.union(x, null);
        xVals.set(index, FastNumber.orNull(x));
    }

    @Override
    public void setY(Number y, int index) {
        rectRegion.union(null, y);
        yVals.add(index, FastNumber.orNull(y));
    }

    @Override
    public void resize(int size) {
        this.size = size;
        resize(xVals, size);
        resize(yVals, size);
    }

    protected void resize(@NonNull List list, int size) {
        if (size > list.size()) {
            while (list.size() < size) {
                list.add(null);
            }
        } else if (size < list.size()) {
            while (list.size() > size) {
                list.remove(list.size() - 1);
            }
        }
    }

    @Override
    public void setBound(Region region) {
        this.bound = region;
    }

    @Override
    public Region getBound() {
        return this.bound;
    }

    @Override
    public void setPlot(XYPlot plot) {
        this.plot = plot;
    }

    @Override
    public void setScaleFactor(double rlog) {
        scaleFactor = rlog;
    }

}
