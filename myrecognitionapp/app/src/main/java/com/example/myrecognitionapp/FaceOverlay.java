package com.example.myrecognitionapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class FaceOverlay extends View {
    private List<RectF> faceBoundingBoxes = new ArrayList<>();
    private Paint paint;

    public FaceOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8.0f);
    }

    // Méthode pour mettre à jour les coordonnées des visages détectés
    public void setFaceBoundingBoxes(List<RectF> faceBoundingBoxes) {
        this.faceBoundingBoxes = faceBoundingBoxes;
        invalidate(); // Redessiner l'écran à chaque changement
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Dessiner les rectangles autour des visages détectés
        for (RectF face : faceBoundingBoxes) {
            canvas.drawRect(face, paint);
        }
    }
}
