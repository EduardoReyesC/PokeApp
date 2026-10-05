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

    // 0: HP, 1: Atk, 2: Def, 3: Spe, 4: S.Def, 5: S.Atk
    private float[] stats = new float[]{50, 50, 50, 50, 50, 50};
    private final String[] labels = new String[]{"HP", "Atk", "Def", "Spe", "S.Def", "S.Atk"};
    private static final float MAX_STAT = 220.0f;

    public RadarStatsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        webPaint.setColor(Color.parseColor("#4D94A3B8"));
        webPaint.setStyle(Paint.Style.STROKE);
        webPaint.setStrokeWidth(1.2f);

        fillPaint.setColor(Color.parseColor("#8022C55E"));
        fillPaint.setStyle(Paint.Style.FILL);

        linePaint.setColor(Color.parseColor("#22C55E"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);

        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(true);
    }

    public void setStats(int hp, int atk, int def, int speed, int spDef, int spAtk) {
        this.stats = new float[]{hp, atk, def, speed, spDef, spAtk};
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        float centerX = w / 2f;
        float centerY = h / 2f;

        // Tamaño de texto adaptado estrictamente al ancho de la pantalla (entre 9sp y 11sp equivalentes)
        float dynamicTextSize = Math.max(9f, Math.min(w * 0.055f, h * 0.075f));
        textPaint.setTextSize(dynamicTextSize);

        // Radio proporcional compacto: ocupa un 40% del radio máximo para garantizar
        // que quepan perfectamente "S.Atk: 160" sin tocar los límites
        float maxDim = Math.min(centerX, centerY);
        float radius = maxDim * 0.46f;
        if (radius <= 6) return;

        // 1. Niveles concéntricos
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

        // 2. Líneas radiales y textos
        float textOffset = dynamicTextSize * 0.45f;
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60 - 90);
            float endX = (float) (centerX + radius * Math.cos(angle));
            float endY = (float) (centerY + radius * Math.sin(angle));
            canvas.drawLine(centerX, centerY, endX, endY, webPaint);

            String text = labels[i] + ": " + (int) stats[i];
            float textX = (float) (centerX + (radius + textOffset) * Math.cos(angle));
            float textY = (float) (centerY + (radius + textOffset) * Math.sin(angle));

            switch (i) {
                case 0: // HP (Arriba)
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    textY -= 2f;
                    break;
                case 1: // Atk (Arriba Derecha)
                case 2: // Def (Abajo Derecha)
                    textPaint.setTextAlign(Paint.Align.LEFT);
                    textX += 3f;
                    textY += (dynamicTextSize * 0.32f);
                    break;
                case 3: // Spe (Abajo)
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    textY += (dynamicTextSize * 0.85f);
                    break;
                case 4: // S.Def (Abajo Izquierda)
                case 5: // S.Atk (Arriba Izquierda)
                    textPaint.setTextAlign(Paint.Align.RIGHT);
                    textX -= 3f;
                    textY += (dynamicTextSize * 0.32f);
                    break;
            }

            canvas.drawText(text, textX, textY, textPaint);
        }

        // 3. Polígono del Pokémon
        Path statPath = new Path();
        for (int i = 0; i < 6; i++) {
            float normalized = Math.min(Math.max(stats[i] / MAX_STAT, 0.16f), 1.0f);
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