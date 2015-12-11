package com.ecp.gsy.dcs.zirkapp.app.util.beans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.ecp.gsy.dcs.zirkapp.app.R;

/**
 * Created by ecanaveras on 10/12/2015.
 */
public class BadgeDrawable extends Drawable {

    private float textSize;
    private Paint badgePaint;
    private Paint textPaint;
    private Rect textRect = new Rect();

    private String mCount = "";
    private boolean willDraw = false;

    public BadgeDrawable(Context context) {
        textSize = context.getResources().getDimension(R.dimen.badge_text_size);

        badgePaint = new Paint();
        badgePaint.setColor(context.getResources().getColor(R.color.accent_color));
        badgePaint.setAntiAlias(true);
        badgePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!willDraw) {
            return;
        }

        Rect bounds = getBounds();
        float width = bounds.right - bounds.left;
        float height = bounds.bottom - bounds.top;

        //Posicion del badge sobre el icono
        float radius = ((Math.min(width, height) / 2) - 1) / 2;
        float centerX = width - radius - 1;
        float centerY = radius + 1;

        //Dibujar el circulo
        canvas.drawCircle(centerX, centerY, radius, badgePaint);

        //Dibujar el badge count en el circulo
        textPaint.getTextBounds(mCount, 0, mCount.length(), textRect);
        float textHeight = textRect.bottom - textRect.top;
        float textY = centerY + (textHeight / 2f);
        canvas.drawText(mCount, centerX, textY, textPaint);
    }

    /**
     * Establace el contador a mostrar
     *
     * @param mCount
     */
    public void setCount(int mCount) {
        this.mCount = Integer.toString(mCount);
        //Dibujar solo si el mCount es mayor que 0
        willDraw = mCount > 0;
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }
}
