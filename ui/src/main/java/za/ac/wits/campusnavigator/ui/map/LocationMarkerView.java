package za.ac.wits.campusnavigator.ui.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;

/**
 * Draws the "you are here" marker: a full-contrast accent dot with a surface-raised
 * outline, plus a translucent accuracy ring behind it while GPS accuracy is degraded
 * (Story 1.2 AC 2, AC 3). The dot's own paint never changes -- only the ring's visibility.
 */
final class LocationMarkerView extends View {

    private static final float DOT_RADIUS_DP = 7f;
    private static final float DOT_OUTLINE_WIDTH_DP = 1.5f;
    private static final float RING_RADIUS_DP = 20f;
    private static final int RING_ALPHA = 70; // translucent, out of 255

    private final Paint dotPaint;
    private final Paint dotOutlinePaint;
    private final Paint ringPaint;
    private final float dotRadiusPx;
    private final float ringRadiusPx;

    private boolean accuracyDegraded;

    LocationMarkerView(Context context, int accentColor, int outlineColor) {
        super(context);
        dotRadiusPx = dpToPx(DOT_RADIUS_DP);
        ringRadiusPx = dpToPx(RING_RADIUS_DP);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(accentColor);
        dotPaint.setStyle(Paint.Style.FILL);

        dotOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotOutlinePaint.setColor(outlineColor);
        dotOutlinePaint.setStyle(Paint.Style.STROKE);
        dotOutlinePaint.setStrokeWidth(dpToPx(DOT_OUTLINE_WIDTH_DP));

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setColor(accentColor);
        ringPaint.setStyle(Paint.Style.FILL);
        ringPaint.setAlpha(RING_ALPHA);

        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    void setAccuracyDegraded(boolean degraded) {
        if (this.accuracyDegraded != degraded) {
            this.accuracyDegraded = degraded;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        if (accuracyDegraded) {
            canvas.drawCircle(cx, cy, ringRadiusPx, ringPaint);
        }
        canvas.drawCircle(cx, cy, dotRadiusPx, dotPaint);
        canvas.drawCircle(cx, cy, dotRadiusPx, dotOutlinePaint);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
