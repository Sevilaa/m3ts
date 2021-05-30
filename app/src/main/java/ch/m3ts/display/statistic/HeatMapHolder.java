package ch.m3ts.display.statistic;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.Map;

import ch.m3ts.display.statistic.data.DetectionData;
import ch.m3ts.tracker.visualization.ZPosVisualizer;
import ch.m3ts.util.Side;

public class HeatMapHolder {
    private int paintColor;
    private View container;
    private ZPosVisualizer zPosVisualizer;
    private Map<Side, Integer> tableCorners;
    private SurfaceHolder holder;
    private Canvas canvas;

    public HeatMapHolder(View container, SurfaceHolder holder, int paintColor, Map<Side, Integer> tableCorners) {
        this.container = container;
        this.holder = holder;
        this.paintColor = paintColor;
        this.tableCorners = tableCorners;
        drawTable();
    }

    private void drawTable() {
        canvas = holder.lockCanvas();
        Paint tablePaint = new Paint();
        tablePaint.setColor(paintColor);
        tablePaint.setStrokeWidth(5f);
        Paint bouncePaint = new Paint();
        bouncePaint.setColor(paintColor);
        bouncePaint.setAlpha(20);
        int height = container.getHeight();
        float canvasWidth = container.getWidth() * 0.5f;
        zPosVisualizer = new ZPosVisualizer(bouncePaint, tablePaint, (float) container.getWidth() / 2 - canvasWidth / 2, height * 0.01f, canvasWidth);
        zPosVisualizer.drawTableBirdView(canvas);
    }

    public void addDetection(DetectionData detection) {
        if (detection.wasBounce()) {
            zPosVisualizer.drawZPos(canvas, detection, tableCorners.get(Side.LEFT), tableCorners.get(Side.RIGHT));
        }
    }

    public void draw() {
        holder.unlockCanvasAndPost(canvas);
    }
}
