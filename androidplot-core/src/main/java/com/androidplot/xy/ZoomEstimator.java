package com.androidplot.xy;

import android.util.Log;

/**
 * Estimates optimal zoom level to be applied to a {@link ZoomEstimatorRequests} based on the current
 * visible bounds of the owning {@link XYPlot}.
 */
public class ZoomEstimator extends Estimator {

    @Override
    public void run(XYPlot plot, XYSeriesBundle sf) {
        if(sf.getSeries() instanceof ZoomEstimatorRequests) {
            ZoomEstimatorRequests oxy = (ZoomEstimatorRequests) sf.getSeries();
            final double factor = calculateZoom(oxy, plot.getBounds());
            oxy.setZoomFactor(factor);
        }
    }

    protected double calculateZoom(ZoomEstimatorRequests series, RectRegion visibleBounds) {
        RectRegion seriesBounds = series.getBounds();
        final double maxFactor = series.getMaxZoomFactor();
        double ratio;
        try {
            ratio = seriesBounds.getxRegion().ratio(visibleBounds.getxRegion()).doubleValue();
        } catch (NullPointerException e)
        {
            return maxFactor;
        }
        final double factor = Math.abs(Math.round(maxFactor / ratio));
        return factor > 0 ? factor : 1;
    }

}

