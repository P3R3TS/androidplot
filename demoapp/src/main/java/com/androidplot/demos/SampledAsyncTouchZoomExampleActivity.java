/*
 * Copyright 2015 AndroidPlot.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.androidplot.demos;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.androidplot.Plot;
import com.androidplot.Region;
import com.androidplot.xy.PlotState;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.FastFixedSizeEditableXYSeries;
import com.androidplot.xy.FastSampledXYSeries;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.OrderedXYSeries;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.StepModelFit;
import com.androidplot.xy.WindowZoomEstimator;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYPointDetection;

import java.text.DecimalFormat;
import java.util.Random;

public class SampledAsyncTouchZoomExampleActivity extends Activity {
    private static final int SERIES_SIZE = 43200;
    private static final int SERIES_ALPHA = 20;
    private static final int NUM_GRIDLINES = 5;
    private XYPlot plot;
    private PanZoom panZoom;
    private Button resetButton;
    private Spinner panSpinner;
    private Spinner zoomSpinner;

    private FastFixedSizeEditableXYSeries s1;
    private FastFixedSizeEditableXYSeries s2;
    private FastFixedSizeEditableXYSeries s3;
    private FastFixedSizeEditableXYSeries s4;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touch_zoom_async_example);
        resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plot.moveBoundariesTo(XYPlot.MoveType.Right, BoundaryMode.FIXED);
                plot.redraw();
            }
        });
        plot = findViewById(R.id.plot);

        plot.getLayoutManager().remove(plot.getLegend());
        plot.getLayoutManager().remove(plot.getTitle());

        XYGraphWidget graph = plot.getGraph();

        graph.setPaddingRight(0f);
        graph.setPaddingTop(0f);
        graph.setPaddingBottom(0f);
        graph.setPaddingLeft(0f);
        graph.setMarginRight(0f);
        graph.setMarginBottom(50f);

        int color = Color.parseColor("#e1edfe");

        graph.getBackgroundPaint().setColor(color);
        graph.getDomainGridLinePaint().setColor(Color.GRAY);
        graph.getDomainGridLinePaint().setPathEffect(new DashPathEffect(new float[] {10, 10}, 1));
        graph.getGridBackgroundPaint().setColor(color);
        graph.getRangeCursorPaint().setColor(Color.GRAY);
        graph.getRangeCursorPaint().setPathEffect(new DashPathEffect(new float[] {10, 10}, 1));

        graph.getRangeOriginLinePaint().setColor(Color.BLACK);
        graph.getDomainOriginLinePaint().setColor(Color.BLACK);

        graph.getRangeGridLinePaint().setColor(Color.GRAY);
        graph.getRangeGridLinePaint().setPathEffect(new DashPathEffect(new float[] {10, 10}, 1));

        graph.getDomainSubGridLinePaint().setColor(Color.GRAY);
        graph.getDomainSubGridLinePaint().setPathEffect(new DashPathEffect(new float[] {10, 10}, 1));
        graph.getRangeSubGridLinePaint().setColor(Color.GRAY);
        graph.getRangeSubGridLinePaint().setPathEffect(new DashPathEffect(new float[] {10, 10}, 1));


//        graph.getBackgroundPaint().setColor(Color.parseColor("#B3D3FC"));
//        graph.getGridBackgroundPaint().setColor(graph.getDomainCursorPaint().getColor());

        // set a fixed origin and a "by-value" step mode so that grid lines will
        // move dynamically with the data when the users pans or zooms:
        plot.setUserDomainOrigin(0);
        plot.setUserRangeOrigin(0);

        // predefine the stepping of both axis
        // increment will be chosen from list to best fit NUM_GRIDLINES grid lines
        double[] inc_domain = new double[]{10, 50, 100, 500};
        double[] inc_range = new double[]{10};
        plot.setDomainStepModel(new StepModelFit(plot.getBounds().getxRegion(), inc_domain, NUM_GRIDLINES));
        plot.setRangeStepModel(new StepModelFit(plot.getBounds().getyRegion(), inc_range, NUM_GRIDLINES));


        panSpinner = findViewById(R.id.pan_spinner);
        zoomSpinner = findViewById(R.id.zoom_spinner);
//        plot.getGraph().setLinesPerRangeLabel(2);
        plot.getGraph().setLinesPerDomainLabel(2);
        //plot.getGraph().getBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).
                setFormat(new DecimalFormat("####"));
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("######"));

        plot.setRangeLabel("");
        plot.setDomainLabel("");

        plot.setBorderStyle(Plot.BorderStyle.NONE, null, null);

        panZoom = PanZoom.attach(plot, PanZoom.Pan.BOTH, PanZoom.Zoom.STRETCH_HORIZONTAL, PanZoom.ZoomLimit.LIMITS, new Region(20, 3600));

        panZoom.setDelegate(new XYPointDetection(new XYPointDetection.XYPointDetect() {

            @Override
            public void onPointXClick(XYPointDetection.State state, double x, double xVal) {
                Log.d("test", "xScreen: " + x + " xValue: " + xVal);
            }
        }));

        plot.getOuterLimits().set(100000, 100100, 0, 100);
        initSpinners();

        // enable autoselect of sampling level based on visible boundaries:
        plot.getRegistry().setEstimator(new WindowZoomEstimator());

        plot.getGraph().setGridRect(null);
        plot.getGraph().setLabelRect(null);

        generateSeriesData();
        reset();
    }

    private void reset() {
        plot.setDomainBoundaries(100000, 100100, BoundaryMode.FIXED);
        plot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);
        plot.redraw();
    }


    private void generateSeriesData() {
        new AsyncTask() {

            PlotState autoPan = new PlotState(false);

            @Override
            protected Object doInBackground(Object[] objects) {
                generateAndAddSeries(800, new LineAndPointFormatter(Color.parseColor("#7763f6"), null, null, null), autoPan);
                generateAndAddSeries(400, new LineAndPointFormatter(Color.parseColor("#c56fa2"), null, null, null), autoPan);
                generateAndAddSeries(200, new LineAndPointFormatter(Color.parseColor("#1479ff"), null, null, null), autoPan);
                generateAndAddSeries(100, new LineAndPointFormatter(Color.parseColor("#ec3f3f"), null, null, null), autoPan);

                Random r = new Random();

                for (int i = 0; i < SERIES_SIZE; i++) {

                    s1.setXYAndRedraw(i + 100000, r.nextInt(800) / 10f, i);
                    s2.setXYAndRedraw(i + 100000, r.nextInt(400) / 10f, i);
                    s3.setXYAndRedraw(i + 100000, r.nextInt(200) / 10f, i);
                    s4.setXYAndRedraw(i + 100000, r.nextInt(100) / 10f, i);
//                        series.addXY(i, r.nextInt(max));
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
//            series.setY(, i);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {

            }
        }.execute();
    }

    private void generateAndAddSeries(int max, LineAndPointFormatter formatter, PlotState autoPan) {
        int s = SERIES_SIZE;
        final FastFixedSizeEditableXYSeries series = new FastFixedSizeEditableXYSeries("s" + max, s, autoPan);


        final FastSampledXYSeries sampledSeries =
                new FastSampledXYSeries(series, OrderedXYSeries.XOrder.ASCENDING, 100);

        plot.addSeries(sampledSeries, formatter);

        if (s1 == null) s1 = series;
        else if (s2 == null) s2 = series;
        else if (s3 == null) s3 = series;
        else if (s4 == null) s4 = series;
    }

    private void initSpinners() {
        panSpinner.setAdapter(
                new ArrayAdapter<>(this, R.layout.spinner_item, PanZoom.Pan.values()));
        panSpinner.setSelection(panZoom.getPan().ordinal());
        panSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                panZoom.setPan(PanZoom.Pan.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing to do
            }
        });


        zoomSpinner.setAdapter(
                new ArrayAdapter<>(this, R.layout.spinner_item, PanZoom.Zoom.values()));
        zoomSpinner.setSelection(panZoom.getZoom().ordinal());
        zoomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                panZoom.setZoom(PanZoom.Zoom.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing to do
            }
        });
    }

    // (optional) save the current pan/zoom state
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putSerializable("pan-zoom-state", panZoom.getState());
    }

    // (optional) restore the previously saved pan/zoom state
    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        PanZoom.State state = (PanZoom.State) bundle.getSerializable("pan-zoom-state");
        panZoom.setState(state);
        plot.redraw();
    }
}

