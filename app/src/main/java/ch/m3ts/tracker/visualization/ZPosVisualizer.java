package ch.m3ts.tracker.visualization;

import android.graphics.Canvas;
import android.graphics.Paint;

import ch.m3ts.display.stats.DetectionData;
import ch.m3ts.tracker.ZPositionCalc;
import cz.fmo.Lib;

public class ZPosVisualizer {
    public static float WIDTH_PX = 400;
    private static final double TABLE_WIDTH_MM = ZPositionCalc.TABLE_TENNIS_TABLE_WIDTH_MM;
    private static final double FULL_WIDTH_MM = ZPositionCalc.MAX_OFFSET_MM * 2 + TABLE_WIDTH_MM;
    private static final double RATIO = FULL_WIDTH_MM / ZPositionCalc.TABLE_TENNIS_TABLE_LENGTH_MM;
    private static int HEIGHT_FULL_PX;
    private static int HEIGHT_TABLE_PX;
    private static int OFFSET_PX;
    private final Paint detectionPaint;
    private final Paint tablePaint;
    private final float originX;
    private final float originY;

    public ZPosVisualizer(Paint detectionPaint, Paint tablePaint, float x, float y) {
        this.detectionPaint = detectionPaint;
        this.tablePaint = tablePaint;
        this.originX = x;
        this.originY = y;
        calculateSizes();
    }

    public ZPosVisualizer(Paint detectionPaint, Paint tablePaint, float x, float y, float width) {
        this(detectionPaint, tablePaint, x, y);
        WIDTH_PX = width;
        calculateSizes();
    }

    private void calculateSizes() {
        HEIGHT_FULL_PX = (int) Math.round(WIDTH_PX * RATIO);
        HEIGHT_TABLE_PX = (int) Math.round((HEIGHT_FULL_PX * TABLE_WIDTH_MM) / FULL_WIDTH_MM);
        OFFSET_PX = (int) Math.round((HEIGHT_FULL_PX - HEIGHT_TABLE_PX) / 2.0);
    }

    public void drawTableBirdView(Canvas canvas) {
        float tableStartY = originY + (float) OFFSET_PX;
        // outer lines of table
        canvas.drawLine(originX, tableStartY, originX + WIDTH_PX, tableStartY, tablePaint);
        canvas.drawLine(originX + WIDTH_PX, tableStartY, originX + WIDTH_PX, tableStartY + HEIGHT_TABLE_PX, tablePaint);
        canvas.drawLine(originX + WIDTH_PX, tableStartY + HEIGHT_TABLE_PX, originX, tableStartY + HEIGHT_TABLE_PX, tablePaint);
        canvas.drawLine(originX, tableStartY + HEIGHT_TABLE_PX, originX, tableStartY, tablePaint);

        // net line
        float middleOfTableX = originX + WIDTH_PX / 2;
        canvas.drawLine(middleOfTableX, tableStartY, middleOfTableX, tableStartY + HEIGHT_TABLE_PX, tablePaint);

        // side separator line
        float middleOfTableY = (tableStartY + tableStartY + HEIGHT_TABLE_PX) * (float) 0.5;
        canvas.drawLine(originX, middleOfTableY, originX + WIDTH_PX, middleOfTableY, tablePaint);
    }

    public void drawZPos(Canvas canvas, Lib.Detection detection, int leftCornerX, int rightCornerX) {
        if (detection.centerX >= leftCornerX && detection.centerX <= rightCornerX && detection.centerZ < 1) {
            float ratio = (float) detection.centerX / (Math.abs(rightCornerX - leftCornerX));
            float x = originX + WIDTH_PX * ratio;
            float y = Math.round(originY + HEIGHT_FULL_PX - (float) detection.centerZ * HEIGHT_FULL_PX);
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
