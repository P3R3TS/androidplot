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
public class FastFixedSizeEditableXYSeries implements FastXYSeries, EditableXYSeries {
    private RectRegion rectRegion = new RectRegion();
    private int size;

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

    public void clear() {
        for(int i = 0; i < size; i++)
        {
            xVals.set(i, null);
            yVals.set(i, null);
        }
        rectRegion = new RectRegion();
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

}
