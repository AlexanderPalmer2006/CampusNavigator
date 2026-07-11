package za.ac.wits.campusnavigator.ui.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.TypedValue;
import android.view.View;
import java.util.Collections;
import java.util.List;

/**
 * Draws the Walking Route as an accent stroke with a surface-raised outline (DESIGN.md
 * walking-route component), via native {@link Canvas} drawing rather than a MapLibre
 * {@code LineLayer}/{@code GeoJsonSource} -- on-device verification (Story 2.2 Task 6)
 * showed a real MapLibre line layer does not render against this app's minimal offline
 * style.json (empty {@code "sources"}, no vector tiles), the same class of silent-failure
 * this codebase already hit once with {@code SymbolLayer} text (Story 1.1's glyphs
 * gotcha) -- confirmed via a deliberately oversized/high-contrast on-device test before
 * falling back here, not assumed. Same "native View overlay repositioned on every camera
 * move" approach as the building labels and {@link LocationMarkerView}.
 */
final class RouteLineView extends View {

    private static final float LINE_WIDTH_DP = 4f;
    private static final float OUTLINE_WIDTH_DP = 7f;

    private final Paint outlinePaint;
    private final Paint linePaint;
    private final Path path = new Path();

    private List<PointF> points = Collections.emptyList();

    RouteLineView(Context context, int accentColor, int outlineColor) {
        super(context);

        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(outlineColor);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(dpToPx(OUTLINE_WIDTH_DP));
        outlinePaint.setStrokeCap(Paint.Cap.ROUND);
        outlinePaint.setStrokeJoin(Paint.Join.ROUND);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(accentColor);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dpToPx(LINE_WIDTH_DP));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        // The route line has no independent tap interaction in V1 (EXPERIENCE.md
        // Interaction Primitives), but EXPERIENCE.md's Accessibility Floor separately
        // names the Walking Route among elements needing a TalkBack role+state label --
        // non-interactivity and no-labeling-needed are not the same rule. Included in the
        // accessibility tree as a non-clickable, announced element (contentDescription set
        // by the caller via setPoints()/MapFragment), same pattern as LocationMarkerView.
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setClickable(false);
    }

    /** Screen-space points, already projected via the map's current camera. */
    void setPoints(List<PointF> newPoints) {
        this.points = newPoints;
        path.reset();
        if (!newPoints.isEmpty()) {
            PointF first = newPoints.get(0);
            path.moveTo(first.x, first.y);
            for (int i = 1; i < newPoints.size(); i++) {
                PointF point = newPoints.get(i);
                path.lineTo(point.x, point.y);
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points.size() < 2) {
            return;
        }
        canvas.drawPath(path, outlinePaint);
        canvas.drawPath(path, linePaint);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
