package net.ym.zzy.draggableflagviewtest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.RelativeLayout;

/**
 * 用来容纳包含小红点视图的容器
 * Created by zengzheying on 15/1/10.
 */
public class DraggableFlagRelativeLayout extends RelativeLayout {

    private static final String TAG = DraggableFlagRelativeLayout.class.getSimpleName();

    private Context context;
    private int originRadius;

    private boolean shouldDrawDraggableCircle;

    private int maxMoveLength;  //最大拖动距离
    private boolean isArrivedMaxMoved;

    private int curRadius; //红点半径
    private int touchedPointRadius;
    private Point startPoint = new Point();
    private Point endPoint = new Point();

    private Paint paint; //绘制红点画笔
    private Paint textPaint;
    private Paint.FontMetrics textFontMetrics;

    private Triangle triangle = new Triangle();

    private String text = "";

    Path path = new Path();  //进行绘制贝塞尔曲线


    private int patientColor = Color.RED;

    private boolean isBeginToDraw = false;
    private boolean isTouched;

    private View flagView;

    public static interface OnDraggableFlagViewListener{
        void onFlagDismiss(View view);   //红点消除时执行的回调方法
    }

    public DraggableFlagRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public DraggableFlagRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DraggableFlagRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        setWillNotDraw(false);
        this.context = context;

        paint = new Paint();
        paint.setColor(patientColor);
        paint.setAntiAlias(true);

        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.0f, context.getResources().getDisplayMetrics()));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textFontMetrics = textPaint.getFontMetrics();

        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        maxMoveLength = dm.heightPixels / 6;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev) || shouldDrawDraggableCircle;
//        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");
        if (shouldDrawDraggableCircle){
            switch (event.getAction()){
                case MotionEvent.ACTION_MOVE:
                    triangle.deltaX = event.getX() - startPoint.x;
                    triangle.deltaY = -1 * (event.getY() - startPoint.y);
                    double distance = Math.sqrt(triangle.deltaX * triangle.deltaX + triangle.deltaY * triangle.deltaY);
                    triangle.hypotenuse = distance;

                    refreshCurRadiusByMoveDistance((int)distance);

                    endPoint.x = (int)event.getX();
                    endPoint.y = (int)event.getY();

                    postInvalidate();

                    break;
                case MotionEvent.ACTION_UP:
                    isTouched = false;
                    if (isArrivedMaxMoved){
                        resetAfterDismiss();
                    }else {
                        startRollBackAnimation(500);
                    }
                    break;
            }
        }
        return true;
//        return super.onTouchEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        onDraw(canvas);
    }

    @SuppressWarnings("NewApi")
    private void startRollBackAnimation(long duration){
        ValueAnimator rollbackAnim = ValueAnimator.ofFloat(curRadius, originRadius);
        rollbackAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float values = (Float)animation.getAnimatedValue();
                curRadius = (int)values;
                postInvalidate();
            }
        });
        rollbackAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                DraggableFlagRelativeLayout.this.clearAnimation();
                shouldDrawDraggableCircle = false;
                flagView.setVisibility(VISIBLE);
            }
        });
        rollbackAnim.setInterpolator(new BounceInterpolator());
        rollbackAnim.setDuration(duration);
        rollbackAnim.start();
    }

    private void resetAfterDismiss(){
        if (flagView != null){
            flagView.setVisibility(GONE);
        }
        text = "";
        isArrivedMaxMoved = false;
        shouldDrawDraggableCircle = false;
        curRadius = 0;
        postInvalidate();
    }

    private void refreshCurRadiusByMoveDistance(int distance){
        if (distance > maxMoveLength){
            isArrivedMaxMoved = true;
            curRadius = 0;
        }else{
            isArrivedMaxMoved = false;
            float calcRadius = (1 - 1f * distance / maxMoveLength) * originRadius;
            float maxRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.0f, context.getResources().getDisplayMetrics());
            curRadius = (int)Math.max(calcRadius, maxRadius);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int startCircleX = 0, startCircleY = 0;
        if (shouldDrawDraggableCircle && isTouched){
            startCircleX = startPoint.x;
            startCircleY = startPoint.y;

            canvas.drawCircle(startCircleX, startCircleY, curRadius, paint);

            if (isBeginToDraw){
                isBeginToDraw = false;
                return;
            }

            int endCircleX = endPoint.x;
            int endCircleY = endPoint.y;
            canvas.drawCircle(endCircleX, endCircleY, originRadius, paint);

            if (!isArrivedMaxMoved){
                path.reset();
                double sin = triangle.deltaY / triangle.hypotenuse;
                double cos = triangle.deltaX / triangle.hypotenuse;

                /**
                 * 贝塞尔曲线所需的四个点
                 * 利用相似三角形的知识可以依此算出
                 */
                path.moveTo((float)(startCircleX - curRadius * sin), (float)(startCircleY - curRadius * cos));
                path.lineTo((float)(startCircleX +  curRadius * sin),(float)(startCircleY + curRadius * cos));
                path.quadTo((startCircleX + endCircleX) / 2, (startCircleY + endCircleY) / 2,
                        (float)(endCircleX +  originRadius * sin),(float)(endCircleY + originRadius * cos));
                path.lineTo((float)(endCircleX - originRadius * sin), (float)(endCircleY - curRadius * cos));
                path.quadTo((startCircleX + endCircleX) / 2, (startCircleY + endCircleY) / 2,
                        (float)(startCircleX - curRadius * sin), (float)(startCircleY - curRadius * cos));
                canvas.drawPath(path, paint);
            }

            float baseline = endCircleY - originRadius + (originRadius * 2  - textFontMetrics.bottom + textFontMetrics.top) / 2 - textFontMetrics.top;
            canvas.drawText(text, endCircleX, baseline, textPaint);
        }else if (shouldDrawDraggableCircle){
            if (curRadius > 0){
                startCircleX = startPoint.x;
                startCircleY = startPoint.y;
                canvas.drawCircle(startCircleX, startCircleY, curRadius, paint);
                if (curRadius == originRadius){
//                    float textH = textFontMetrics.bottom - textFontMetrics.top;
                    float baseline = startCircleY - curRadius + (curRadius * 2  - textFontMetrics.bottom + textFontMetrics.top) / 2 - textFontMetrics.top;
                    canvas.drawText(text, startCircleX, baseline, textPaint);
                }
            }
        }
    }

    /**
     * 在DragView的OnTouchEvent方法调用此方法
     * @param flagView
     * @param text
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @param originRadius
     */

    public void drawDraggableCircleView(View flagView, String text, int startX, int startY, int endX, int endY, int originRadius){
        if (context == null){
            return;
        }
        this.flagView = flagView;
        this.text = text;
        int[] location = new int[2];
        this.getLocationOnScreen(location);
        startPoint.x = startX - location[0];
        startPoint.y = startY - location[1];

        endPoint.x = endX - location[0];
        endPoint.y = endY - location[1];

        this.originRadius = originRadius;
        this.curRadius = originRadius;

        shouldDrawDraggableCircle = true;

        isBeginToDraw = true;
        isTouched = true;

        invalidate();
    }

    /**
     * 计算四个坐标的三角关系
     */

    class Triangle{
        double deltaX;
        double deltaY;
        double hypotenuse;

        public String toString(){
            return "Triangle{" +
                    "deltaX=" + deltaX +
                    ", deltaY=" + deltaY +
                    ", hypotenuse=" + hypotenuse + "}";
        }
    }
}
