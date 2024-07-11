package com.androidplot.xy;


import android.graphics.RectF;
import android.graphics.PointF;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.*;

import com.androidplot.*;
import com.androidplot.util.*;

import java.awt.font.NumericShaper;
import java.io.Serializable;
import java.util.*;


/**
 * Enables basic pan/zoom touch behavior for an {@link XYPlot}.
 * By default boundaries there are no boundaries imposed on scrolling and zooming.  You can provide these boundaries
 * on your {@link XYPlot} using {@link XYPlot#getOuterLimits()}.
 * TODO: zoom using dynamic center point
 * TODO: stretch both mode
 */
public class PanZoom implements View.OnTouchListener {

    protected static final float MIN_DIST_2_FING = 5f;
    protected static final int FIRST_FINGER = 0;
    protected static final int SECOND_FINGER = 1;

    private final XYPlot plot;
//    private Pan pan;
    //    private Zoom zoom;
    private ZoomState zoomState;
//    private ZoomFactor zoomFactor;

    private Region axisRegion;
    private ZoomLimit zoomLimit;
    private boolean isEnabled = true;

    private DragState dragState = DragState.NONE;
    private PointF firstFingerPos;

    // rectangle created by the space between two fingers
    protected RectF fingersRect;
    private View.OnTouchListener delegate;
    private OnPanZoomDelegate onPanDelegate;
    private OnPanZoomDelegate onZoomDelegate;
    private State state = new State();

    public interface OnPanZoomDelegate{
        void execute();
    }

    public enum ZoomFactor {
        LEFT,
        CENTER,
        RIGHT
    }

    // Definition of the touch states
    protected enum DragState {
        NONE,
        ONE_FINGER,
        TWO_FINGERS
    }

    public enum Pan {
        NONE,
        HORIZONTAL,
        VERTICAL,
        BOTH
    }

    public enum Zoom {

        /**
         * Comletely disable panning
         */
        NONE,

        /**
         * Zoom on the horizontal axis only
         */
        STRETCH_HORIZONTAL,

        /**
         * Zoom on the vertical axis only
         */
        STRETCH_VERTICAL,

        /**
         * Zoom on the vertical axis by the vertical distance between each finger, while zooming
         * on the horizontal axis by the horizantal distance between each finger.
         */
        STRETCH_BOTH,

        /**
         * Zoom each axis by the same amount, specifically the total distance between each finger.
         */
        SCALE
    }

    /**
     * Limits imposed on the zoom.
     */
    public enum ZoomLimit {
        /**
         * Do not zoom outside the plots outer bounds, if they are defined.
         */
        OUTER,

        /**
         * Additionally to the outer bounds if plot.StepModel defines a value based increment
         * make sure at least one tick is visible by not zooming in further.
         */
        MIN_TICKS,

        /**
         * Additionally to the outer bounds if limits is defines
         */
        LIMITS,
    }

    // TODO: consider making this immutable / threadsafe
    public static class State implements Serializable {
        private Number domainLowerBoundary;
        private Number domainUpperBoundary;
        private Number rangeLowerBoundary;
        private Number rangeUpperBoundary;
        private BoundaryMode domainBoundaryMode;
        private BoundaryMode rangeBoundaryMode;

        public void setDomainBoundaries(Number lowerBoundary, Number upperBoundary, BoundaryMode mode) {
            this.domainLowerBoundary = lowerBoundary;
            this.domainUpperBoundary = upperBoundary;
            this.domainBoundaryMode = mode;
        }

        public void setRangeBoundaries(Number lowerBoundary, Number upperBoundary, BoundaryMode mode) {
            this.rangeLowerBoundary = lowerBoundary;
            this.rangeUpperBoundary = upperBoundary;
            this.rangeBoundaryMode = mode;
        }

        public void applyDomainBoundaries(@NonNull XYPlot plot) {
            plot.setDomainBoundaries(domainLowerBoundary, domainUpperBoundary, domainBoundaryMode);
        }

        public void applyRangeBoundaries(@NonNull XYPlot plot) {
            plot.setRangeBoundaries(rangeLowerBoundary, rangeUpperBoundary, rangeBoundaryMode);
        }

        public void apply(@NonNull XYPlot plot) {
            applyDomainBoundaries(plot);
            applyRangeBoundaries(plot);
        }
    }

    private class PanZoomState implements ZoomState.ZoomDelegate
    {
        private Pan pan;
        private Zoom zoom;
        private ZoomFactor zoomFactor;

        @Override
        public void setZoomFactor(ZoomFactor zoomFactor) {
            this.zoomFactor = zoomFactor;
        }
        @Override
        public void setZoom(Zoom zoom) {
            this.zoom = zoom;
        }
        @Override
        public void setPan(Pan pan) {
            this.pan = pan;
        }

        public PanZoomState(Pan pan, Zoom zoom, ZoomFactor zoomFactor)
        {
            this.pan = pan;
            this.zoom = zoom;
            this.zoomFactor = zoomFactor;
        }

        @Override
        public ZoomFactor getZoomFactor(ZoomState state) {
            return this.zoomFactor;
        }

        @Override
        public Pan getPan(ZoomState state) {
            return this.pan;
        }

        @Override
        public Zoom getZoom(ZoomState state) {
            return this.zoom;
        }
    }

    protected PanZoom(@NonNull XYPlot plot, Pan pan, Zoom zoom) {
        this.plot = plot;
        this.zoomState = new ZoomState(false, new PanZoomState(pan, zoom, ZoomFactor.CENTER));
        this.zoomLimit = ZoomLimit.OUTER;
        this.axisRegion = new Region(0, 1000);
    }


    // additional constructor not to break api
    protected PanZoom(@NonNull XYPlot plot, Pan pan, Zoom zoom, ZoomLimit limit) {
        this.plot = plot;
        this.zoomState = new ZoomState(false, new PanZoomState(pan, zoom, ZoomFactor.CENTER));
        this.zoomLimit = limit;
        this.axisRegion = new Region(0, 1000);
    }

    // additional constructor not to break api
    protected PanZoom(@NonNull XYPlot plot, Pan pan, Zoom zoom, ZoomLimit limit, Region zoomRegion) {
        this.plot = plot;
        this.zoomState = new ZoomState(false, new PanZoomState(pan, zoom, ZoomFactor.CENTER));
        this.zoomLimit = limit;
        this.axisRegion = zoomRegion;
    }

    // additional constructor not to break api
    protected PanZoom(@NonNull XYPlot plot, ZoomState zoomState,  ZoomLimit limit, Region zoomRegion, ZoomFactor zoomFactor) {
        this.zoomState = zoomState;
        this.plot = plot;
        this.zoomLimit = limit;
        this.axisRegion = zoomRegion;
    }

    // additional constructor not to break api
    protected PanZoom(@NonNull XYPlot plot, Pan pan, Zoom zoom, ZoomLimit limit, Region zoomRegion, ZoomFactor zoomFactor) {
        this.plot = plot;
        this.zoomState = new ZoomState(false, new PanZoomState(pan, zoom, zoomFactor));
        this.zoomLimit = limit;
        this.axisRegion = zoomRegion;
    }


    public State getState() {
        return this.state;
    }

    public void setState(@NonNull State state) {
        this.state = state;
        state.apply(plot);
    }

    protected void adjustRangeBoundary(Number lower, Number upper,  BoundaryMode mode) {
        state.setRangeBoundaries(lower, upper, mode);
        state.applyRangeBoundaries(plot);
    }

    protected void adjustDomainBoundary(Number lower, Number upper, BoundaryMode mode) {
        state.setDomainBoundaries(lower, upper, mode);
        state.applyDomainBoundaries(plot);
    }

    /**
     * Convenience method for enabling pan/zoom behavior on an instance of {@link XYPlot}, using
     * a default behavior of {@link Pan#BOTH} and {@link Zoom#SCALE}.
     * Use {@link PanZoom#attach(XYPlot, Pan, Zoom, ZoomLimit)} for finer grain control of this behavior.
     * @param plot
     * @return
     */
    public static PanZoom attach(@NonNull XYPlot plot) {
        return attach(plot, Pan.BOTH, Zoom.SCALE);
    }

    /**
     * Old method for enabling pan/zoom behavior on an instance of {@link XYPlot}, using
     * the default behavior of {@link ZoomLimit#OUTER}.
     * Use {@link PanZoom#attach(XYPlot, Pan, Zoom, ZoomLimit)} for finer grain control of this behavior.
     * @param plot
     * @param pan
     * @param zoom
     * @return
     */
    public static PanZoom attach(@NonNull XYPlot plot, @NonNull Pan pan, @NonNull Zoom zoom) {
        return attach(plot,pan,zoom, ZoomLimit.OUTER);
    }

    /**
     * New method for enabling pan/zoom behavior on an instance of {@link XYPlot}.
     * @param plot
     * @param pan
     * @param zoom
     * @param limit
     * @return
     */
    public static PanZoom attach(@NonNull XYPlot plot, @NonNull Pan pan, @NonNull Zoom zoom, @NonNull ZoomLimit limit) {
        PanZoom pz = new PanZoom(plot, pan, zoom, limit);
        plot.setOnTouchListener(pz);
        plot.setPanZoom(pz);
        return pz;
    }

    /**
     * New method for enabling pan/zoom behavior on an instance of limits.
     * @param plot
     * @param pan
     * @param zoom
     * @param limit
     * @param zoomRegion
     * @return
     */
    public static PanZoom attach(@NonNull XYPlot plot, @NonNull Pan pan, @NonNull Zoom zoom, @NonNull ZoomLimit limit, @NonNull Region zoomRegion) {
        PanZoom pz = new PanZoom(plot, pan, zoom, limit, zoomRegion);
        plot.setOnTouchListener(pz);
        plot.setPanZoom(pz);
        return pz;
    }

    /**
     * New method for enabling pan/zoom behavior on an instance of limits.
     * @param plot
     * @param pan
     * @param zoom
     * @param limit
     * @param zoomRegion
     * @param zoomFactor
     * @return
     */
    public static PanZoom attach(@NonNull XYPlot plot, @NonNull Pan pan, @NonNull Zoom zoom, @NonNull ZoomLimit limit, @NonNull Region zoomRegion, @NonNull ZoomFactor zoomFactor) {
        PanZoom pz = new PanZoom(plot, pan, zoom, limit, zoomRegion, zoomFactor);
        plot.setOnTouchListener(pz);
        plot.setPanZoom(pz);
        return pz;
    }

    /**
     * New method for enabling pan/zoom behavior on an instance of limits.
     * @param plot
     * @param zoomState
     * @param limit
     * @param zoomRegion
     * @param zoomFactor
     * @return
     */
    public static PanZoom attach(@NonNull XYPlot plot, @NonNull ZoomState zoomState, @NonNull ZoomLimit limit, @NonNull Region zoomRegion, @NonNull ZoomFactor zoomFactor) {
        PanZoom pz = new PanZoom(plot, zoomState, limit, zoomRegion, zoomFactor);
        plot.setOnTouchListener(pz);
        plot.setPanZoom(pz);
        return pz;
    }


    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent event) {
        boolean isConsumed = false;
        if (delegate != null) {
            isConsumed = delegate.onTouch(view, event);
        }
        if (isEnabled() && !isConsumed) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: // start gesture
                    firstFingerPos = new PointF(event.getX(), event.getY());
                    dragState = DragState.ONE_FINGER;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN: // second finger
                {
                    setFingersRect(fingerDistance(event));
                    // the distance run is done to avoid false alarms
                    if (getFingersRect().width() > MIN_DIST_2_FING || getFingersRect().width() < -MIN_DIST_2_FING) {
                        dragState = DragState.TWO_FINGERS;
                    }
                    break;
                }
                case MotionEvent.ACTION_POINTER_UP: // end zoom
                    dragState = DragState.NONE;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (dragState == DragState.ONE_FINGER) {
                        pan(event);
                    } else if (dragState == DragState.TWO_FINGERS) {
                        zoom(event);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    reset();
                    break;
            }
        }
        // we're forced to consume the event here as not consuming it will prevent future calls:
        return true;
    }

    /**
     * Calculates the distance between two finger motion events.
     * @param firstFingerX
     * @param firstFingerY
     * @param secondFingerX
     * @param secondFingerY
     * @return
     */
    protected RectF fingerDistance(float firstFingerX, float firstFingerY, float secondFingerX, float secondFingerY) {
        final float left = firstFingerX > secondFingerX ? secondFingerX : firstFingerX;
        final float right = firstFingerX > secondFingerX ? firstFingerX : secondFingerX;
        final float top = firstFingerY > secondFingerY ? secondFingerY : firstFingerY;
        final float bottom = firstFingerY > secondFingerY ? firstFingerY : secondFingerY;
        return new RectF(left, top, right, bottom);
    }

    /**
     * Calculates the distance between two finger motion events.
     * @param evt
     * @return
     */
    protected RectF fingerDistance(final MotionEvent evt) {
        return fingerDistance(
                evt.getX(FIRST_FINGER),
                evt.getY(FIRST_FINGER),
                evt.getX(SECOND_FINGER),
                evt.getY(SECOND_FINGER));
    }

    protected void pan(final MotionEvent motionEvent) {
        if (zoomState.delegate.getPan(zoomState) == Pan.NONE) {
            return;
        }

        final PointF oldFirstFinger = firstFingerPos; //save old position of finger
        firstFingerPos = new PointF(motionEvent.getX(), motionEvent.getY()); //update finger position
        if (EnumSet.of(Pan.HORIZONTAL, Pan.BOTH).contains(zoomState.delegate.getPan(zoomState))) {
            Region newBounds = new Region();
            calculatePan(oldFirstFinger, newBounds, true);
            adjustDomainBoundary(newBounds.getMin(), newBounds.getMax(), BoundaryMode.FIXED);
        }
        if (EnumSet.of(Pan.VERTICAL, Pan.BOTH).contains(zoomState.delegate.getPan(zoomState))) {
            Region newBounds = new Region();
            calculatePan(oldFirstFinger, newBounds, false);
            adjustRangeBoundary(newBounds.getMin(), newBounds.getMax(), BoundaryMode.FIXED);
        }

        if(onPanDelegate != null) onPanDelegate.execute();

        plot.redraw();
    }

    protected void calculatePan(final PointF oldFirstFinger, Region bounds, final boolean horizontal) {
        final float offset;
        // multiply the absolute finger movement for a factor.
        // the factor is dependent on the calculated min and max
        if (horizontal) {
            bounds.setMinMax(plot.getBounds().getxRegion());
            offset = (oldFirstFinger.x - firstFingerPos.x) *
                    ((bounds.getMax().floatValue() - bounds.getMin().floatValue()) / plot.getWidth());
        } else {
            bounds.setMinMax(plot.getBounds().getyRegion());
            offset = -(oldFirstFinger.y - firstFingerPos.y) *
                    ((bounds.getMax().floatValue() - bounds.getMin().floatValue()) / plot.getHeight());
        }
        // move the calculated offset
        bounds.setMin(bounds.getMin().floatValue() + offset);
        bounds.setMax(bounds.getMax().floatValue() + offset);

        //get the distance between max and min
        final float diff = bounds.length().floatValue();

        //run if we reached the limit of panning
        if (horizontal && plot.getOuterLimits().getxRegion().isDefined()) {
            if (bounds.getMin().floatValue() < plot.getOuterLimits().getMinX().floatValue()) {
                bounds.setMin(plot.getOuterLimits().getMinX());
                bounds.setMax(bounds.getMin().floatValue() + diff);
            }
            if (bounds.getMax().floatValue() > plot.getOuterLimits().getMaxX().floatValue()) {
                bounds.setMax(plot.getOuterLimits().getMaxX());
                bounds.setMin(bounds.getMax().floatValue() - diff);
            }
        } else if(plot.getOuterLimits().getyRegion().isDefined()) {
            if (bounds.getMin().floatValue() < plot.getOuterLimits().getMinY().floatValue()) {
                bounds.setMin(plot.getOuterLimits().getMinY());
                bounds.setMax(bounds.getMin().floatValue() + diff);
            }
            if (bounds.getMax().floatValue() > plot.getOuterLimits().getMaxY().floatValue()) {
                bounds.setMax(plot.getOuterLimits().getMaxY());
                bounds.setMin(bounds.getMax().floatValue() - diff);
            }
        }
    }

    protected boolean isValidScale(float scale) {
        return !Float.isInfinite(scale)
                && !Float.isNaN(scale)
                && (!(scale > -0.001) || !(scale < 0.001));
    }

    protected void zoom(final MotionEvent motionEvent) {
        if (zoomState.delegate.getZoom(zoomState) == Zoom.NONE) {
            return;
        }
        final RectF oldFingersRect = getFingersRect();
        final RectF newFingersRect = fingerDistance(motionEvent);
        setFingersRect(newFingersRect);
        if(oldFingersRect == null || RectFUtils.areIdentical(oldFingersRect, newFingersRect)) {
            // zooming gesture has not happened yet so skip:
            return;
        }
        RectF newRect = new RectF();


        float scaleX = 1;
        float scaleY = 1;

        switch (zoomState.delegate.getZoom(zoomState)) {
            case STRETCH_HORIZONTAL:
                scaleX = oldFingersRect.width() / newFingersRect.width();
                if (!isValidScale(scaleX)) {
                    return;
                }
                break;
            case STRETCH_VERTICAL:
                scaleY = oldFingersRect.height() / newFingersRect.height();
                if (!isValidScale(scaleY)) {
                    return;
                }
                break;
            case STRETCH_BOTH:
                scaleX = oldFingersRect.width() / newFingersRect.width();
                scaleY = oldFingersRect.height() / newFingersRect.height();
                if (!isValidScale(scaleX) || !isValidScale(scaleY)) {
                    return;
                }
                break;
            case SCALE:
                float sc1 = (float) Math.hypot(oldFingersRect.height(), oldFingersRect.width());
                float sc2 = (float) Math.hypot(newFingersRect.height(), newFingersRect.width());
                float sc = sc1 / sc2;
                scaleX = sc;
                scaleY = sc;
                if (!isValidScale(scaleX) || !isValidScale(scaleY)) {
                    return;
                }
                break;
        }

        if (EnumSet.of(
                Zoom.STRETCH_HORIZONTAL,
                Zoom.STRETCH_BOTH,
                Zoom.SCALE).contains(zoomState.delegate.getZoom(zoomState))) {
            calculateZoom(newRect, scaleX, true);
            adjustDomainBoundary(newRect.left, newRect.right, BoundaryMode.FIXED);
        }
        if (EnumSet.of(
                Zoom.STRETCH_VERTICAL,
                Zoom.STRETCH_BOTH,
                Zoom.SCALE).contains(zoomState.delegate.getZoom(zoomState))) {
            calculateZoom(newRect, scaleY, false);
            adjustRangeBoundary(newRect.top, newRect.bottom, BoundaryMode.FIXED);
        }

        if(onZoomDelegate != null) onZoomDelegate.execute();

        plot.redraw();
    }

    /**
     *
     * @param newRect RectF into which zoom calculation results should be placed.
     * @param scale
     * @param isHorizontal
     */
    protected void calculateZoom(RectF newRect, float scale, boolean isHorizontal) {
        final float calcMax;
        final float span;
        final RectRegion bounds = plot.getBounds();
        if (isHorizontal) {
            calcMax = bounds.getMaxX().floatValue();
            span = calcMax - bounds.getMinX().floatValue();
        } else {
            calcMax = bounds.getMaxY().floatValue();
            span = calcMax - bounds.getMinY().floatValue();
        }

        final float midPoint = calcMax - (span / 2.0f);
        float offset = span * scale / 2.0f;
        final RectRegion limits = plot.getOuterLimits();

        if (isHorizontal ) {
            // zoom limited and increment by value StepMode?
            if (zoomLimit == ZoomLimit.MIN_TICKS) {
                // make sure we do not zoom in too far (there should be at least one grid line visible)
                if (plot.getDomainStepValue() > (scale*span)) {
                    offset = (float)(plot.getDomainStepValue() / 2.0f);
                }
            }

            // zoom limited
            if(zoomLimit == ZoomLimit.LIMITS) {
                if(axisRegion.getMin().floatValue() > (scale*span)) {
                    offset = (float)(axisRegion.getMin().doubleValue() / 2.0f);
                }
                if(axisRegion.getMax().floatValue() < (scale*span)) {
                    offset = (float)(axisRegion.getMax().doubleValue() / 2.0f);
                }
            }

            switch (this.zoomState.delegate.getZoomFactor(zoomState))
            {
                case CENTER:
                    newRect.left = midPoint - offset;
                    newRect.right = midPoint + offset;
                    break;
                case LEFT:
                    newRect.left = bounds.getMinX().floatValue();
                    newRect.right = midPoint + offset;
                    break;
                case RIGHT:
                    newRect.left = midPoint - offset;
                    newRect.right = bounds.getMaxX().floatValue();
                    break;
            }

            if(limits.isFullyDefined()) {
                if (newRect.left < limits.getMinX().floatValue()) {
                    newRect.left =  limits.getMinX().floatValue();
                }
                if (newRect.right >  limits.getMaxX().floatValue()) {
                    newRect.right =  limits.getMaxX().floatValue();
                }
            }
        } else {
            // zoom limited and increment by value StepMode?
            if (zoomLimit == ZoomLimit.MIN_TICKS) {
                // make sure we do not zoom in too far (there should be at least one grid line visible)
                if (plot.getRangeStepValue() > (scale*span)) {
                    offset = (float)(plot.getRangeStepValue() / 2.0f);
                }
            }

            newRect.top = midPoint - offset;
            newRect.bottom = midPoint + offset;
            if(limits.isFullyDefined()) {
                if (newRect.top < limits.getMinY().floatValue()) {
                    newRect.top = limits.getMinY().floatValue();
                }
                if (newRect.bottom > limits.getMaxY().floatValue()) {
                    newRect.bottom = limits.getMaxY().floatValue();
                }
            }
        }
    }

    public void setZoomLimits(Region axisRegion) {
        this.axisRegion = axisRegion;
    }

    public Region getZoomLimits() {
        return this.axisRegion;
    }

    public Pan getPan() {
        return zoomState.delegate.getPan(zoomState);
    }

    public void setPan(Pan pan) {
        zoomState.delegate.setPan(pan);
    }

    public void setZoomFactor(ZoomFactor zoomFactor) {
        zoomState.delegate.setZoomFactor(zoomFactor);
    }

    public ZoomFactor getZoomFactor() {
        return zoomState.delegate.getZoomFactor(zoomState);
    }

    public Zoom getZoom() {
        return zoomState.delegate.getZoom(zoomState);
    }

    public void setZoom(Zoom zoom) {
        zoomState.delegate.setZoom(zoom);
    }

    public ZoomLimit getZoomLimit() {
        return zoomLimit;
    }

    public void setZoomLimit(ZoomLimit zoomLimit) {
        this.zoomLimit = zoomLimit;
    }

    public View.OnTouchListener getDelegate() {
        return delegate;
    }

    /**
     * Set a delegate to receive onTouch calls before this class does.  If the delegate wishes
     * to consume the event, it should return true, otherwise it should return false.  Returning
     * false will not prevent future onTouch events from filtering through the delegate as it normally
     * would when attaching directly to an instance of {@link View}.
     * @param delegate
     */
    public void setDelegate(View.OnTouchListener delegate) {
        this.delegate = delegate;
    }

    public void setOnPanDelegate(OnPanZoomDelegate onPanDelegate) {
        this.onPanDelegate = onPanDelegate;
    }

    public OnPanZoomDelegate getOnPanDelegate() {
        return onPanDelegate;
    }

    public void setOnZoomDelegate(OnPanZoomDelegate onZoomDelegate) {
        this.onZoomDelegate = onZoomDelegate;
    }

    public OnPanZoomDelegate getOnZoomDelegate() {
        return onZoomDelegate;
    }

    public void reset() {
        this.firstFingerPos = null;
        setFingersRect(null);
        this.setFingersRect(null);
    }

    protected RectF getFingersRect() {
        return fingersRect;
    }

    protected void setFingersRect(RectF fingersRect) {
        this.fingersRect = fingersRect;
    }
}
