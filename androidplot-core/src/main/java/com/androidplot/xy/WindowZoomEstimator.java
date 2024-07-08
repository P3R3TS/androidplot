package com.androidplot.xy;

/**
 * Estimates optimal zoom level to be applied to a {@link FastSampledXYSeries} based on the current
 * visible bounds of the owning {@link XYPlot}.
 */
public class WindowZoomEstimator extends Estimator {

    @Override
    public void run(XYPlot plot, XYSeriesBundle sf) {
        if(sf.getSeries() instanceof FastSampledXYSeries) {
            FastSampledXYSeries oxy = (FastSampledXYSeries) sf.getSeries();
            oxy.setZoomBounds(plot.getBounds());
            oxy.setPlot(plot);
        }
    }
}
