package za.ac.wits.campusnavigator.ui.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;
import java.util.Collections;
import java.util.List;

/**
 * Draws Building footprint fills as filled polygons with a thin outline (DESIGN.md's
 * building-fill component, Story 6.3), via native {@link Canvas} drawing -- generalizes
 * {@link RouteLineView}'s established pattern (screen-space points supplied by the caller,
 * {@code invalidate()} on update) from a single open polyline to N closed, filled polygons.
 * Used only if the runtime {@code FillLayer}/{@code GeoJsonSource} attempt (Task 2) fails
 * on-device, the same "attempt first, fall back to a proven native overlay" discipline
 * {@link RouteLineView}'s own Javadoc documents for the Walking Route line.
 *
 * <p>Sits in a new {@code buildingFillOverlay} {@link android.widget.FrameLayout} tier,
 * added to {@code fragment_map.xml} <em>before</em> (beneath, in paint order)
 * {@code buildingLabelOverlay} -- fills must render behind building labels, the Walking
 * Route, and the location marker (AC 2), the opposite of {@code labelOverlay}'s existing
 * single-tier "everything on top" assumption.</p>
 */
final class BuildingFillView extends View {

    private static final float STROKE_WIDTH_DP = 1.5f;

    private final Paint fillPaint;
    private final Paint strokePaint;
    private List<List<PointF>> polygons = Collections.emptyList();

    BuildingFillView(Context context, int fillColor, int strokeColor) {
        super(context);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(fillColor);
        fillPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(strokeColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dpToPx(STROKE_WIDTH_DP));
        strokePaint.setStrokeJoin(Paint.Join.ROUND);

        // Purely decorative map content, same posture as RouteLineView -- no independent
        // tap interaction, but still a real element worth keeping out of the accessibility
        // tree entirely (unlike the route line, a footprint fill has no discrete
        // destination-specific meaning worth a TalkBack announcement; the Building label
        // sitting on top of it already carries that role).
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setClickable(false);
    }

    /** Screen-space polygons (one list of points per ring), already projected via the map's current camera. */
    void setPolygons(List<List<PointF>> newPolygons) {
        this.polygons = newPolygons;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (List<PointF> ring : polygons) {
            if (ring.size() < 3) {
                // Not a real polygon (a degenerate/collapsed ring) -- skip rather than draw
                // a meaningless sliver.
                continue;
            }
            Path path = new Path();
            PointF first = ring.get(0);
            path.moveTo(first.x, first.y);
            for (int i = 1; i < ring.size(); i++) {
                PointF point = ring.get(i);
                path.lineTo(point.x, point.y);
            }
            path.close();
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);
        }
    }

    /**
     * Cheap pre-projection culling (epic-6-scoping-2026-07-12.md §3's named cardinality
     * concern): true if the ring's real-world lat/lng bounds intersect the map's currently
     * visible bounds, so {@code MapFragment} can skip reprojecting/drawing a ring that's
     * entirely off-screen rather than doing the work and discarding it every frame.
     */
    static boolean intersectsVisibleBounds(RectF ringLatLngBounds, RectF visibleLatLngBounds) {
        return RectF.intersects(ringLatLngBounds, visibleLatLngBounds);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
