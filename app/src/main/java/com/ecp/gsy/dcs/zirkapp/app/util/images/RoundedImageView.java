package com.ecp.gsy.dcs.zirkapp.app.util.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Elder on 02/03/2015.
 */
public class RoundedImageView extends ImageView {

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Bitmap bitmap1 = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap2 = bitmap1.copy(Bitmap.Config.ARGB_8888, true);

        int w = getWidth();
        int h = getHeight();

        Bitmap roundBitmap = getCroppedBitmap(bitmap2, w);
        canvas.drawBitmap(roundBitmap, 0, 0, null);
    }

    public static Bitmap getCroppedBitmap(Bitmap inBitmap, int radio) {
        Bitmap otherBitmap;
        if (inBitmap.getWidth() != radio || inBitmap.getHeight() != radio) {
            otherBitmap = Bitmap.createScaledBitmap(inBitmap, radio, radio, false);
        } else {
            otherBitmap = inBitmap;
        }

        Bitmap outBitmap = Bitmap.createBitmap(otherBitmap.getWidth(), otherBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(outBitmap);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, otherBitmap.getWidth(), otherBitmap.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(otherBitmap.getWidth() / 2 + 0.7f, otherBitmap.getHeight() / 2 + 0.7f, otherBitmap.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(otherBitmap, rect, rect, paint);

        return outBitmap;
    }


}
