package com.hz.zhangos.graffiti.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

/**
 * Created by zhangos on 2018/09/21
 */
public class GraffitiView extends View {
    private final Object LOCK = new Object();
    private static final int mBackgroundColor = Color.WHITE;
    private static final int PAINT_STROKE_WIDTH = 3;
    private static final int PAINT_COLOR = Color.BLACK;
    //DrawPath的集合，用于记录所有画过的Path
    private DrawPath mCurrentDrawPath;
    private Stack<DrawPath> cacheDrawPathStack = new Stack<>();
    private Paint mPaint;
    private Path mPath;
    private Canvas mCanvas;
    private Bitmap mBitmap;

    private float tempX;
    private float tempY;

    public GraffitiView(Context context) {
        super(context);
        initParameter();
    }

    public GraffitiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initParameter();
    }

    public GraffitiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParameter();
    }

    public GraffitiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initParameter();
    }

    private void initParameter() {
        initPaint();
        setBackgroundColor(mBackgroundColor);
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStrokeWidth(PAINT_STROKE_WIDTH);
        mPaint.setColor(PAINT_COLOR);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eX = event.getX();
        float eY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                createBitmap();

                tempX = eX;
                tempY = eY;
                //创建Path
                mPath = new Path();
                mPath.moveTo(eX, eY);

                mCurrentDrawPath = new DrawPath();
                mCurrentDrawPath.path = mPath;
                mCurrentDrawPath.paint = createCurrentPaint();

                AddCachePathList(mCurrentDrawPath);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.quadTo(tempX, tempY, eX, eY);
                tempX = eX;
                tempY = eY;
                drawPathOnBitmap();
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        invalidate();
        return true;
    }

    private void drawPathOnBitmap() {
        synchronized (LOCK) {
            mCanvas.drawPath(mCurrentDrawPath.path, mCurrentDrawPath.paint);
        }
    }

    private void createBitmap() {
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
    }

    private void AddCachePathList(DrawPath drawPath) {
        synchronized (LOCK) {
            cacheDrawPathStack.push(drawPath);
        }
    }

    private Paint createCurrentPaint() {
        Paint _paint = new Paint();
        _paint.setStrokeWidth(mPaint.getStrokeWidth());
        _paint.setColor(mPaint.getColor());
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeCap(Paint.Cap.ROUND);
        _paint.setStrokeJoin(Paint.Join.ROUND);
        return _paint;
    }

    public void clearAllPath() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
        mBitmap = null;
        createBitmap();
        invalidate();
    }

    public void undo() {
        synchronized (LOCK) {
            if (cacheDrawPathStack != null && !cacheDrawPathStack.empty()) {
                cacheDrawPathStack.pop();
                clearAllPath();
                for (DrawPath dPath : cacheDrawPathStack) {
                    mCanvas.drawPath(dPath.path, dPath.paint);
                }
                invalidate();
            }
        }
    }

    public Bitmap clearBlank(Bitmap bp, int blank) {
        int _bmHeight = bp.getHeight();
        int _bmWidth = bp.getWidth();
        int top = 0;
        int left = 0;
        int right = _bmWidth;
        int bottom = _bmHeight;
        int[] pixArr = new int[_bmWidth];
        boolean isStop;
        for (int y = 0; y < _bmHeight; y++) {
            bp.getPixels(pixArr, 0, _bmWidth, 0, y, _bmWidth, 1);
            isStop = false;
            for (int pix : pixArr) {
                if (pix != mBackgroundColor) {
                    top = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        for (int y = _bmHeight - 1; y >= 0; y--) {
            bp.getPixels(pixArr, 0, _bmWidth, 0, y, _bmWidth, 1);
            isStop = false;
            for (int pix : pixArr) {
                if (pix != mBackgroundColor) {
                    bottom = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }

        int scanHeight = bottom - top;
        pixArr = new int[scanHeight];
        for (int x = 0; x < _bmWidth; x++) {
            bp.getPixels(pixArr, 0, 1, x, top, 1, scanHeight);
            isStop = false;
            for (int pix : pixArr) {
                if (pix != mBackgroundColor) {
                    left = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        for (int x = _bmWidth - 1; x > 0; x--) {
            bp.getPixels(pixArr, 0, 1, x, top, 1, scanHeight);
            isStop = false;
            for (int pix : pixArr) {
                if (pix != mBackgroundColor) {
                    right = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        if (blank < 0) {
            blank = 0;
        }
        left = left - blank > 0 ? left - blank : 0;
        top = top - blank > 0 ? top - blank : 0;
        right = right + blank > _bmWidth - 1 ? _bmWidth - 1 : right + blank;
        bottom = bottom + blank > _bmHeight - 1 ? _bmHeight - 1 : bottom + blank;
        return Bitmap.createBitmap(bp, left, top, right - left, bottom - top);
    }

    public void save(String path, boolean clearBlank, int blank) throws IOException {
        synchronized (LOCK) {
            Bitmap bitmap = mBitmap;
            if (clearBlank) {
                bitmap = clearBlank(bitmap, blank);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] buffer = bos.toByteArray();
            if (buffer != null) {
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                OutputStream outputStream = new FileOutputStream(file);
                outputStream.write(buffer);
                outputStream.close();
            }
        }
    }

    public void setPaintColor(int color){
        mPaint.setColor(color);
    }

    public void setPaintStrokeWidth(int width){
        mPaint.setStrokeWidth(width);
    }
    private class DrawPath {
        private Path path;
        private Paint paint;
    }
}

