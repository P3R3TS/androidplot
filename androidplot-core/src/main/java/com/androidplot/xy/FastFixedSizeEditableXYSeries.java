package com.androidplot.xy;


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
    private XYPlot _plot = null;
    private double scaleFactor = 1;
    private PlotState autoPan;

    @NonNull
    private List<FastNumber> xVals = new ArrayList<>();

    @NonNull
    private List<FastNumber> yVals = new ArrayList<>();
    private String title;
    //private int lastVisibleIndex = 0;

    public FastFixedSizeEditableXYSeries(String title, int size, PlotState autoPan) {
        this.autoPan = autoPan;
        setTitle(title);
        resize(size);
    }

    public void setXY(@Nullable Number x, @Nullable Number y, int index) {
        rectRegion.union(x, y);
        xVals.set(index, FastNumber.orNull(x));
        yVals.set(index, FastNumber.orNull(y));
    }

    public interface PlotTools{
        void redraw();
        void setMaxX(Number maxX);
        void moveBoundariesTo(XYPlot.MoveType moveType, BoundaryMode mode);
    }

    public void setXYAndRedraw(@Nullable Number x, @Nullable Number y, int index) {
        rectRegion.union(x, y);
        xVals.set(index, FastNumber.orNull(x));
        yVals.set(index, FastNumber.orNull(y));

        if (this._plot != null) {
            if(bound != null && bound.contains(x.intValue() - 2) && _plot.getOuterLimits().getMaxX().doubleValue() >= x.longValue()) this._plot.redraw();
            resamplePan(index, this._plot);
        }
    }

    public void setXYAndRedraw(@Nullable Number x, @Nullable Number y, int index, PlotTools plotTools, XYPlot plot) {
        rectRegion.union(x, y);
        xVals.set(index, FastNumber.orNull(x));
        yVals.set(index, FastNumber.orNull(y));

        if (this._plot != null) {
            if(bound != null && bound.contains(x.intValue() - 2) && plot.getOuterLimits().getMaxX().doubleValue() >= x.longValue()) plotTools.redraw();
            resamplePan(index, plotTools, plot);
        }
    }

    public void resamplePan(int lastIndex, PlotTools plotTools, XYPlot plot)
    {
        try {
            int lastVisibleIndex = (int) (lastIndex - (lastIndex % scaleFactor));
            if (plot.getOuterLimits().getMaxX().doubleValue() < getX(lastVisibleIndex).doubleValue()) {
                boolean isBoundaries = plot.isBoundariesFrom(XYPlot.MoveType.Right);
                plotTools.setMaxX(getX(lastVisibleIndex).doubleValue());
                if(autoPan.isBlock()) return;
                if (isBoundaries || autoPan.isScrollNonBlock()) {
                    plotTools.moveBoundariesTo(XYPlot.MoveType.Right, BoundaryMode.FIXED);
                    plotTools.redraw();
                }
            }
        } catch (NullPointerException e)
        {
            // lastIndex is null
        }
    }

    public void resamplePan(int lastIndex, XYPlot plot)
    {
        try {
            int lastVisibleIndex = (int) (lastIndex - (lastIndex % scaleFactor));
            if (plot.getOuterLimits().getMaxX().doubleValue() < getX(lastVisibleIndex).doubleValue()) {
                boolean isBoundaries = plot.isBoundariesFrom(XYPlot.MoveType.Right);
                plot.getOuterLimits().setMaxX(getX(lastVisibleIndex).doubleValue());
                if(autoPan.isBlock()) return;
                if (isBoundaries || autoPan.isScrollNonBlock()) {
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
        this._plot = plot;
        this.autoPan.setPlot(plot);
    }

    @Override
    public void setScaleFactor(double rlog) {
        scaleFactor = rlog;
    }

}
