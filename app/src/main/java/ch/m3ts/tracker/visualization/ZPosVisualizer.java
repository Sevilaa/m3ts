package ch.m3ts.tracker.visualization;

import android.graphics.Canvas;
import android.graphics.Paint;

import ch.m3ts.display.stats.DetectionData;
import ch.m3ts.tracker.ZPositionCalc;
import cz.fmo.Lib;

public class ZPosVisualizer {
    public static final float DEFAULT_WIDTH_PX = 400;
    private static final double TABLE_WIDTH_MM = ZPositionCalc.TABLE_TENNIS_TABLE_WIDTH_MM;
    private static final double FULL_WIDTH_MM = ZPositionCalc.MAX_OFFSET_MM * 2 + TABLE_WIDTH_MM;
    private static final double RATIO = FULL_WIDTH_MM / ZPositionCalc.TABLE_TENNIS_TABLE_LENGTH_MM;
    private final Paint detectionPaint;
    private final Paint tablePaint;
    private final float originX;
    private final float originY;
    private float widthPx;
    private int heightFullPx;
    private int heightTablePx;
    private int offsetPx;

    public ZPosVisualizer(Paint detectionPaint, Paint tablePaint, float x, float y) {
        this.detectionPaint = detectionPaint;
        this.tablePaint = tablePaint;
        this.originX = x;
        this.originY = y;
        this.widthPx = DEFAULT_WIDTH_PX;
        calculateSizes();
    }

    public ZPosVisualizer(Paint detectionPaint, Paint tablePaint, float x, float y, float width) {
        this(detectionPaint, tablePaint, x, y);
        widthPx = width;
        calculateSizes();
    }

    private void calculateSizes() {
        heightFullPx = (int) Math.round(widthPx * RATIO);
        heightTablePx = (int) Math.round((heightFullPx * TABLE_WIDTH_MM) / FULL_WIDTH_MM);
        offsetPx = (int) Math.round((heightFullPx - heightTablePx) / 2.0);
    }

    public void drawTableBirdView(Canvas canvas) {
        float tableStartY = originY + (float) offsetPx;
        // outer lines of table
        canvas.drawLine(originX, tableStartY, originX + widthPx, tableStartY, tablePaint);
        canvas.drawLine(originX + widthPx, tableStartY, originX + widthPx, tableStartY + heightTablePx, tablePaint);
        canvas.drawLine(originX, tableStartY + heightTablePx, originX + widthPx, tableStartY + heightTablePx, tablePaint);
        canvas.drawLine(originX, tableStartY, originX, tableStartY + heightTablePx, tablePaint);

        // net line
        float middleOfTableX = originX + widthPx / 2;
        canvas.drawLine(middleOfTableX, tableStartY, middleOfTableX, tableStartY + heightTablePx, tablePaint);

        // side separator line
        float middleOfTableY = (tableStartY + tableStartY + heightTablePx) * (float) 0.5;
        canvas.drawLine(originX, middleOfTableY, originX + widthPx, middleOfTableY, tablePaint);
    }

    public void drawZPos(Canvas canvas, Lib.Detection detection, int leftCornerX, int rightCornerX) {
        if (detection.centerX >= leftCornerX && detection.centerX <= rightCornerX && detection.centerZ < 1.0 && detection.centerZ > 0.0) {
            float ratio = (float) detection.centerX / (Math.abs(rightCornerX - leftCornerX));
            float x = originX + widthPx * ratio;
            float y = Math.round(originY + heightFullPx - (float) detection.centerZ * heightFullPx);
            canvas.drawCircle(x, y, 20, detectionPaint);
        }
    }

    public void drawZPos(Canvas canvas, DetectionData detectionData, int leftCornerX, int rightCornerX) {
        Lib.Detection detection = new Lib.Detection();
        detection.centerX = detectionData.getX();
        detection.centerZ = detectionData.getZ();
        drawZPos(canvas, detection, leftCornerX, rightCornerX);
    }
}
