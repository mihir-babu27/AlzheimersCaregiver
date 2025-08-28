package com.mihir.alzheimerscaregiver.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingCanvasView extends View {

    private final Paint paint = new Paint();
    private final Path path = new Path();

    public DrawingCanvasView(Context context) {
        super(context);
        init();
    }

    public DrawingCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;
    }

    public void clear() {
        path.reset();
        invalidate();
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }
}


