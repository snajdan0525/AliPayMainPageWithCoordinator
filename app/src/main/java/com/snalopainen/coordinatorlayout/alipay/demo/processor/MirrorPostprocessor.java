package com.snalopainen.coordinatorlayout.alipay.demo.processor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * Created by jinyan on 17/2/16.
 */

public class MirrorPostprocessor extends BasePostprocessor {

    @Override
    public void process(Bitmap destBitmap, Bitmap sourceBitmap) {
        Matrix m = new Matrix();

        final int width = sourceBitmap.getWidth();
        final int height = sourceBitmap.getHeight();

        int centerX = width >> 1;
        int centerY = height >> 1;
        m.reset();
        m.postTranslate(-centerX, -centerY);
        m.postScale(-1, 1);
        m.postTranslate(centerX, centerY);

        Canvas canvas = new Canvas(destBitmap);
        canvas.drawBitmap(sourceBitmap, m, null);
    }

}
