package com.example.pokeappicesba;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class RadarStatsView extends View {

    private final Paint webPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // HP, Attack, Defense, Speed, SpDef, SpAtk
    private float[] stats = new float[]{50, 50, 50, 50, 50, 50};
    private final String[] labels = new String[]{"HP", "Atk", "Def", "Spe", "S.Def", "S.Atk"};
    private static final float MAX_STAT = 255.0f;

    public RadarStatsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        webPaint.setColor(Color.parseColor("#4DE2E8F0"));
        webPaint.setStyle(Paint.Style.STROKE);
        webPaint.setStrokeWidth(2f);

        fillPaint.setColor(Color.parseColor("#9922C55E"));
        fillPaint.setStyle(Paint.Style.FILL);

        linePaint.setColor(Color.parseColor("#22C55E"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(26f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setStats(int hp, int atk, int def, int speed, int spDef, int spAtk) {
        this.stats = new float[]{hp, atk, def, speed, spDef, spAtk};
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - 55f;

        if (radius <= 0) return;

        // 1. Dibujar redes concéntricas (niveles 0.5 y 1.0)
        for (float level = 0.5f; level <= 1.0f; level += 0.5f) {
            Path webPath = new Path();
            for (int i = 0; i < 6; i++) {
                double angle = Math.toRadians(i * 60 - 90);
                float x = (float) (centerX + radius * level * Math.cos(angle));
                float y = (float) (centerY + radius * level * Math.sin(angle));
                if (i == 0) webPath.moveTo(x, y);
                else webPath.lineTo(x, y);
            }
            webPath.close();
            canvas.drawPath(webPath, webPaint);
        }

        // 2. Líneas radiales desde el centro
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60 - 90);
            float x = (float) (centerX + radius * Math.cos(angle));
            float y = (float) (centerY + radius * Math.sin(angle));
            canvas.drawLine(centerX, centerY, x, y, webPaint);

            // Etiquetas
            float textX = (float) (centerX + (radius + 35f) * Math.cos(angle));
            float textY = (float) (centerY + (radius + 35f) * Math.sin(angle)) + 8f;
            canvas.drawText(labels[i] + ": " + (int) stats[i], textX, textY, textPaint);
        }

        // 3. Polígono de estadísticas del Pokémon
        Path statPath = new Path();
        for (int i = 0; i < 6; i++) {
            float normalized = Math.min(stats[i] / MAX_STAT, 1.0f);
            double angle = Math.toRadians(i * 60 - 90);
            float x = (float) (centerX + radius * normalized * Math.cos(angle));
            float y = (float) (centerY + radius * normalized * Math.sin(angle));
            if (i == 0) statPath.moveTo(x, y);
            else statPath.lineTo(x, y);
        }
        statPath.close();

        canvas.drawPath(statPath, fillPaint);
        canvas.drawPath(statPath, linePaint);
    }
}