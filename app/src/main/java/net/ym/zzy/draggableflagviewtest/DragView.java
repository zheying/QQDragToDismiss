package net.ym.zzy.draggableflagviewtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

/**
 * Created by zengzheying on 15/1/10.
 */
public class DragView extends View {

    Paint paint = new Paint();

    DraggableFlagRelativeLayout draggableFlageRelativeLayout;

    private String text = "3";
    Paint.FontMetrics textFontMetrics;
    Paint textPaint;

    public DragView(Context context) {
        super(context);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.0f, context.getResources().getDisplayMetrics()));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textFontMetrics = textPaint.getFontMetrics();
    }

    public DragView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 7.0f, context.getResources().getDisplayMetrics()));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textFontMetrics = textPaint.getFontMetrics();
    }

    public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.0f, context.getResources().getDisplayMetrics()));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textFontMetrics = textPaint.getFontMetrics();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        while(parent != null && ! (parent instanceof DraggableFlagRelativeLayout)){
            parent = parent.getParent();
        }
        if (parent != null && parent instanceof DraggableFlagRelativeLayout){
            draggableFlageRelativeLayout = (DraggableFlagRelativeLayout)parent;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int radius = Math.min(getWidth(), getHeight()) / 2;
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, paint);
        float baseline = 0 + (getHeight()  - textFontMetrics.bottom + textFontMetrics.top) / 2 - textFontMetrics.top;
        canvas.drawText(text, getWidth() / 2, baseline, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            if (draggableFlageRelativeLayout != null){
                int[] location = new int[2];
                this.getLocationOnScreen(location);
                draggableFlageRelativeLayout.drawDraggableCircleView(this, text,
                        (int)(location[0] + getWidth() /2),
                        (int)(location[1] + getHeight() / 2),
                        (int)(event.getX() + location[0]),
                        (int)(event.getY() + location[1]),
                        Math.min(getWidth(), getHeight()) / 2);
                setVisibility(INVISIBLE);
            }
        }
        return super.onTouchEvent(event);
    }
}
