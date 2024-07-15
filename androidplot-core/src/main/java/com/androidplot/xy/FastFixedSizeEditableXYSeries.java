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
    private final int overflow;
    private Number min = 0;
    private Number max = 100;

    @NonNull
    private List<FastNumber> xVals = new ArrayList<>();

    @NonNull
    private List<FastNumber> yVals = new ArrayList<>();
    private String title;

    private Number normalize(Number value, Number min, Number max){
        if(value == null) return null;
        double oRange = max.doubleValue() - min.doubleValue();
        double nRange = 100;
        return ((value.doubleValue() - min.doubleValue()) * 100.0) / oRange;
    }

    private final Object obj = new Object();
    public void setMinMaxRangeForNormalize(Number min, Number max)
    {
        if(min == null || max == null) return;
        // Set min max
        this.min = min;
        this.max = max;
        synchronized (obj){
            for(int i = 0; i < this.size; i++)
            {
                yVals.set(i, FastNumber.orNull(normalize(yVals.get(i), min,max)));
            }
        }
    }

    public FastFixedSizeEditableXYSeries(String title, int size) {
        setTitle(title);
        resize(size);
        this.overflow = size / 2;
    }

    public FastFixedSizeEditableXYSeries(String title, int size, int overflow) {
        setTitle(title);
        resize(size);
        this.overflow = overflow;
    }

    public void setXY(@Nullable Number x, @Nullable Number y, int index) {
        rectRegion.union(x, y);
        xVals.set(index, FastNumber.orNull(x));
        yVals.set(index, FastNumber.orNull(normalize(y, min, max)));
    }

    public int setXYMovable(@Nullable Number x, @Nullable Number y, int index) {
        rectRegion.union(x, y);
        xVals.set(index, FastNumber.orNull(x));
        yVals.set(index, FastNumber.orNull(normalize(y, min, max)));
        if(index >= this.size - 1){
            xVals = move(xVals, overflow);
            rectRegion.setMinX(xVals.get(0));
            yVals = move(yVals, overflow);
            return index - overflow;
        }
        return index;
    }

    private List<FastNumber> move(List<FastNumber> list, int moveBy)
    {
        List<FastNumber> nList = new ArrayList<>();
        for(int i = moveBy; i < list.size(); i++)
        {
            nList.add(list.get(i));
        }
        resize(nList, this.size);
        return nList;
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
        if (index >= this.size || index < 0) return null;
        return xVals.get(index);
    }

    @Override
    public Number getY(int index) {
        if (index >= this.size || index < 0) return null;
        synchronized (obj) {
            return yVals.get(index);
        }
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
