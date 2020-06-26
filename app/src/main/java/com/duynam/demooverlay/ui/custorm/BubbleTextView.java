package com.duynam.demooverlay.ui.custorm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.core.view.MotionEventCompat;

import com.duynam.demooverlay.R;
import com.duynam.demooverlay.utils.DensityUtils;

@SuppressLint("AppCompatCustomView")
public class BubbleTextView extends ImageView {
    private static final String TAG = "BubbleTextView";

    private Bitmap deleteBitmap;
    private Bitmap flipVBitmap;
    private Bitmap topBitmap;
    private Bitmap resizeBitmap;
    private Bitmap mBitmap;
    private Bitmap originBitmap;
    private Rect dst_delete;
    private Rect dst_resize;
    private Rect dst_flipV;
    private Rect dst_top;


    private int deleteBitmapWidth;
    private int deleteBitmapHeight;
    private int resizeBitmapWidth;
    private int resizeBitmapHeight;
    private int flipVBitmapWidth;
    private int flipVBitmapHeight;


    private int topBitmapWidth;
    private int topBitmapHeight;
    private Paint localPaint;
    private int mScreenwidth, mScreenHeight;
    private static final float BITMAP_SCALE = 0.7f;
    private PointF mid = new PointF();
    private OperationListener operationListener;
    private float lastRotateDegree;


    private boolean isPointerDown = false;

    private final float pointerLimitDis = 20f;
    private final float pointerZoomCoeff = 0.09f;

    private final float moveLimitDis = 0.5f;

    private float lastLength;
    private boolean isInResize = false;

    private Matrix matrix = new Matrix();

    private boolean isInSide;

    private float lastX, lastY;

    private boolean isInEdit = true;

    private float MIN_SCALE = 0.5f;

    private float MAX_SCALE = 1.5f;

    private double halfDiagonalLength;

    private float oringinWidth = 0;

    private DisplayMetrics dm;


    private final String defaultStr;

    private String mStr = "";


    private float mDefultSize = 16;
    private float mFontSize = 16;

    private final float mMaxFontSize = 30;
    private final float mMinFontSize = 14;


    private final float mDefaultMargin = 20;
    private float mMargin = 20;


    private TextPaint mFontPaint;

    private Canvas canvasText;

    private Paint.FontMetrics fm;

    private float baseline;

    boolean isInit = true;


    private float oldDis;


    private boolean isDown = false;
    private boolean isMove = false;
    private boolean isUp = false;
    private boolean isTop = true;

    private boolean isInBitmap;

    private final int fontColor;

    private final long bubbleId;


    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        defaultStr = getContext().getString(R.string.double_click_input_text);
        this.fontColor = Color.BLACK;
        bubbleId = 0;
        init();
    }

    public BubbleTextView(Context context) {
        super(context);
        defaultStr = getContext().getString(R.string.double_click_input_text);
        this.fontColor = Color.BLACK;
        bubbleId = 0;
        init();
    }

    public BubbleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        defaultStr = getContext().getString(R.string.double_click_input_text);
        this.fontColor = Color.BLACK;
        bubbleId = 0;
        init();
    }

    public BubbleTextView(Context context, int fontColor, long bubbleId) {
        super(context);
        defaultStr = getContext().getString(R.string.double_click_input_text);
        this.fontColor = fontColor;
        this.bubbleId = bubbleId;
        init();
    }


    private void init() {
        dm = getResources().getDisplayMetrics();
        dst_delete = new Rect();
        dst_resize = new Rect();
        dst_flipV = new Rect();
        dst_top = new Rect();
        localPaint = new Paint();
        localPaint.setColor(getResources().getColor(R.color.colorWhite));
        localPaint.setAntiAlias(true);
        localPaint.setDither(true);
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setStrokeWidth(2.0f);
        mScreenwidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        mFontSize = mDefultSize;
        mFontPaint = new TextPaint();
        mFontPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mFontSize, dm));
        mFontPaint.setColor(fontColor);
        mFontPaint.setTextAlign(Paint.Align.CENTER);
        mFontPaint.setAntiAlias(true);
        fm = mFontPaint.getFontMetrics();

        baseline = fm.descent - fm.ascent;
        isInit = true;
        mStr = defaultStr;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            float[] arrayOfFloat = new float[9];
            matrix.getValues(arrayOfFloat);
            float f1 = 0.0F * arrayOfFloat[0] + 0.0F * arrayOfFloat[1] + arrayOfFloat[2];
            float f2 = 0.0F * arrayOfFloat[3] + 0.0F * arrayOfFloat[4] + arrayOfFloat[5];
            float f3 = arrayOfFloat[0] * this.mBitmap.getWidth() + 0.0F * arrayOfFloat[1] + arrayOfFloat[2];
            float f4 = arrayOfFloat[3] * this.mBitmap.getWidth() + 0.0F * arrayOfFloat[4] + arrayOfFloat[5];
            float f5 = 0.0F * arrayOfFloat[0] + arrayOfFloat[1] * this.mBitmap.getHeight() + arrayOfFloat[2];
            float f6 = 0.0F * arrayOfFloat[3] + arrayOfFloat[4] * this.mBitmap.getHeight() + arrayOfFloat[5];
            float f7 = arrayOfFloat[0] * this.mBitmap.getWidth() + arrayOfFloat[1] * this.mBitmap.getHeight() + arrayOfFloat[2];
            float f8 = arrayOfFloat[3] * this.mBitmap.getWidth() + arrayOfFloat[4] * this.mBitmap.getHeight() + arrayOfFloat[5];


            canvas.save();

            mBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
            canvasText.setBitmap(mBitmap);
            canvasText.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            float left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, dm);
            float scalex = arrayOfFloat[Matrix.MSCALE_X];
            float skewy = arrayOfFloat[Matrix.MSKEW_Y];
            float rScale = (float) Math.sqrt(scalex * scalex + skewy * skewy);

            float size = rScale * 0.75f * mDefultSize;
            if (size > mMaxFontSize) {
                mFontSize = mMaxFontSize;
            } else if (size < mMinFontSize) {
                mFontSize = mMinFontSize;
            } else {
                mFontSize = size;
            }
            mFontPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mFontSize, dm));
            String[] texts = autoSplit(mStr, mFontPaint, mBitmap.getWidth() - left * 3);
            float height = (texts.length * (baseline + fm.leading) + baseline);
            float top = (mBitmap.getHeight() - height) / 2;
            top += baseline;
            for (String text : texts) {
                if (TextUtils.isEmpty(text)) {
                    continue;
                }
                canvasText.drawText(text, mBitmap.getWidth() / 2, top, mFontPaint);
                top += baseline + fm.leading;
            }
            canvas.drawBitmap(mBitmap, matrix, null);


            dst_delete.left = (int) (f3 - deleteBitmapWidth / 2);
            dst_delete.right = (int) (f3 + deleteBitmapWidth / 2);
            dst_delete.top = (int) (f4 - deleteBitmapHeight / 2);
            dst_delete.bottom = (int) (f4 + deleteBitmapHeight / 2);

            dst_resize.left = (int) (f7 - resizeBitmapWidth / 2);
            dst_resize.right = (int) (f7 + resizeBitmapWidth / 2);
            dst_resize.top = (int) (f8 - resizeBitmapHeight / 2);
            dst_resize.bottom = (int) (f8 + resizeBitmapHeight / 2);

            dst_top.left = (int) (f1 - topBitmapWidth / 2);
            dst_top.right = (int) (f1 + topBitmapWidth / 2);
            dst_top.top = (int) (f2 - topBitmapHeight / 2);
            dst_top.bottom = (int) (f2 + topBitmapHeight / 2);

            dst_flipV.left = (int) (f5 - topBitmapWidth / 2);
            dst_flipV.right = (int) (f5 + topBitmapWidth / 2);
            dst_flipV.top = (int) (f6 - topBitmapHeight / 2);
            dst_flipV.bottom = (int) (f6 + topBitmapHeight / 2);
            if (isInEdit) {
                canvas.drawLine(f1, f2, f3, f4, localPaint);
                canvas.drawLine(f3, f4, f7, f8, localPaint);
                canvas.drawLine(f5, f6, f7, f8, localPaint);
                canvas.drawLine(f5, f6, f1, f2, localPaint);

                canvas.drawBitmap(deleteBitmap, null, dst_delete, null);
                canvas.drawBitmap(resizeBitmap, null, dst_resize, null);
                //canvas.drawBitmap(flipVBitmap, null, dst_flipV, null);
               // canvas.drawBitmap(topBitmap, null, dst_top, null);
            }
            canvas.restore();
        }
    }

    public void setText(String text) {
        mStr = text;
        invalidate();
    }

    public void setOpacity(int opacity){
        mFontPaint.setAlpha(opacity);
    }

    public void setFont(Typeface font) {
        mFontPaint.setTypeface(font);
    }

    public String getText() {
        return mStr;
    }

    public void setSize(float size){
        mDefultSize = size;
    }

    public void setColor(int color) {
        mFontPaint.setColor(color);
        invalidate();
    }

    @Override
    public void setImageResource(int resId) {
        matrix.reset();

        setBitmap(BitmapFactory.decodeResource(getResources(), resId));
    }


//    public void setBitmap(Bitmap bitmap, BubblePropertyModel model) {
//        mFontSize = mDefultSize;
//        originBitmap = bitmap;
//        mBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
//        canvasText = new Canvas(mBitmap);
//        setDiagonalLength();
//        initBitmaps();
//        int w = mBitmap.getWidth();
//        int h = mBitmap.getHeight();
//        oringinWidth = w;
//
//        mStr = model.getText();
//        float scale = model.getScaling() * mScreenwidth / mBitmap.getWidth();
//        if (scale > MAX_SCALE) {
//            scale = MAX_SCALE;
//        } else if (scale < MIN_SCALE) {
//            scale = MIN_SCALE;
//        }
//        float degree = (float) Math.toDegrees(model.getDegree());
//        matrix.postRotate(-degree, w >> 1, h >> 1);
//        matrix.postScale(scale, scale, w >> 1, h >> 1);
//        float midX = model.getxLocation() * mScreenwidth;
//        float midY = model.getyLocation() * mScreenwidth;
//        float offset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22, dm);
//        midX = midX - (w * scale) / 2 - offset;
//        midY = midY - (h * scale) / 2 - offset;
//        matrix.postTranslate(midX, midY);
//        invalidate();
//    }

    public void setBitmap(Bitmap bitmap) {
        mFontSize = mDefultSize;
        originBitmap = bitmap;
        mBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvasText = new Canvas(mBitmap);
        setDiagonalLength();
        initBitmaps();
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        oringinWidth = w;
        float topbarHeight = DensityUtils.dip2px(getContext(), 50);

        matrix.postTranslate(mScreenwidth / 2 - w / 2, (mScreenwidth) / 2 - h / 2);
        invalidate();
    }

    private void setDiagonalLength() {
        halfDiagonalLength = Math.hypot(mBitmap.getWidth(), mBitmap.getHeight()) / 2;
    }

    private void initBitmaps() {

        float minWidth = mScreenwidth / 8;
        if (mBitmap.getWidth() < minWidth) {
            MIN_SCALE = 1f;
        } else {
            MIN_SCALE = 1.0f * minWidth / mBitmap.getWidth();
        }

        if (mBitmap.getWidth() > mScreenwidth) {
            MAX_SCALE = 1;
        } else {
            MAX_SCALE = 1.0f * mScreenwidth / mBitmap.getWidth();
        }
        //topBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_text_color);
        deleteBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.remove);
        //flipVBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.transparency);
        resizeBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.zoominout);

        deleteBitmapWidth = (int) (deleteBitmap.getWidth() * BITMAP_SCALE);
        deleteBitmapHeight = (int) (deleteBitmap.getHeight() * BITMAP_SCALE);

        resizeBitmapWidth = (int) (resizeBitmap.getWidth() * BITMAP_SCALE);
        resizeBitmapHeight = (int) (resizeBitmap.getHeight() * BITMAP_SCALE);

        //flipVBitmapWidth = (int) (flipVBitmap.getWidth() * BITMAP_SCALE);
        //flipVBitmapHeight = (int) (flipVBitmap.getHeight() * BITMAP_SCALE);

        //topBitmapWidth = (int) (topBitmap.getWidth() * BITMAP_SCALE);
        //topBitmapHeight = (int) (topBitmap.getHeight() * BITMAP_SCALE);

    }

    private long preClicktime;

    private final long doubleClickTimeLimit = 400;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        boolean handled = true;
        isInBitmap = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isInButton(event, dst_delete)) {
                    if (operationListener != null) {
                        operationListener.onDeleteClick();
                    }
                    isDown = false;
                } else if (isInResize(event)) {
                    isInResize = true;
                    lastRotateDegree = rotationToStartPoint(event);
                    midPointToStartPoint(event);
                    lastLength = diagonalLength(event);
                    isDown = false;
                } else if (isInButton(event, dst_flipV)) {
//                    PointF localPointF = new PointF();
//                    midDiagonalPoint(localPointF);
//                    matrix.postScale(-1.0F, 1.0F, localPointF.x, localPointF.y);
//                    isDown = false;
//                    invalidate();
                    operationListener.onSetSize();
                } else if (isInButton(event, dst_top)) {

                    bringToFront();
                    if (operationListener != null) {
                        operationListener.onTop(this);
                    }
                    isDown = false;
                } else if (isInBitmap(event)) {
                    isInSide = true;
                    lastX = event.getX(0);
                    lastY = event.getY(0);
                    isDown = true;
                    isMove = false;
                    isPointerDown = false;
                    isUp = false;
                    isInBitmap = true;

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - preClicktime > doubleClickTimeLimit) {
                        preClicktime = currentTime;
                    } else {
                        if (isInEdit && operationListener != null) {
                            operationListener.onClick(this);
                        }
                    }

                } else {
                    handled = false;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (spacing(event) > pointerLimitDis) {
                    oldDis = spacing(event);
                    isPointerDown = true;
                    midPointToStartPoint(event);
                } else {
                    isPointerDown = false;
                }
                isInSide = false;
                isInResize = false;
                break;
            case MotionEvent.ACTION_MOVE:

                if (isPointerDown) {
                    float scale;
                    float disNew = spacing(event);
                    if (disNew == 0 || disNew < pointerLimitDis) {
                        scale = 1;
                    } else {
                        scale = disNew / oldDis;

                        scale = (scale - 1) * pointerZoomCoeff + 1;
                    }
                    float scaleTemp = (scale * Math.abs(dst_flipV.left - dst_resize.left)) / oringinWidth;
                    if (((scaleTemp <= MIN_SCALE)) && scale < 1 ||
                            (scaleTemp >= MAX_SCALE) && scale > 1) {
                        scale = 1;
                    } else {
                        lastLength = diagonalLength(event);
                    }
                    matrix.postScale(scale, scale, mid.x, mid.y);
                    invalidate();
                } else if (isInResize) {
                    matrix.postRotate((rotationToStartPoint(event) - lastRotateDegree) * 2, mid.x, mid.y);
                    lastRotateDegree = rotationToStartPoint(event);

                    float scale = diagonalLength(event) / lastLength;

                    if (((diagonalLength(event) / halfDiagonalLength <= MIN_SCALE)) && scale < 1 ||
                            (diagonalLength(event) / halfDiagonalLength >= MAX_SCALE) && scale > 1) {
                        scale = 1;
                        if (!isInResize(event)) {
                            isInResize = false;
                        }
                    } else {
                        lastLength = diagonalLength(event);
                    }
                    matrix.postScale(scale, scale, mid.x, mid.y);

                    invalidate();
                } else if (isInSide) {

                    float x = event.getX(0);
                    float y = event.getY(0);

                    if (!isMove && Math.abs(x - lastX) < moveLimitDis
                            && Math.abs(y - lastY) < moveLimitDis) {
                        isMove = false;
                    } else {
                        isMove = true;
                    }
                    matrix.postTranslate(x - lastX, y - lastY);
                    lastX = x;
                    lastY = y;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isInResize = false;
                isInSide = false;
                isPointerDown = false;
                isUp = true;
                break;

        }
        if (handled && operationListener != null) {
            operationListener.onEdit(this);
        }
        return handled;
    }

//    public BubblePropertyModel calculate(BubblePropertyModel model) {
//        float[] v = new float[9];
//        matrix.getValues(v);
//        // translation is simple
//        float tx = v[Matrix.MTRANS_X];
//        float ty = v[Matrix.MTRANS_Y];
//
//        // calculate real scale
//        float scalex = v[Matrix.MSCALE_X];
//        float skewy = v[Matrix.MSKEW_Y];
//        float rScale = (float) Math.sqrt(scalex * scalex + skewy * skewy);
//
//        // calculate the degree of rotation
//        float rAngle = Math.round(Math.atan2(v[Matrix.MSKEW_X], v[Matrix.MSCALE_X]) * (180 / Math.PI));
//
//
//        float minX = (dst_top.centerX() + dst_resize.centerX()) / 2;
//        float minY = (dst_top.centerY() + dst_resize.centerY()) / 2;
//
//
//        model.setDegree((float) Math.toRadians(rAngle));
//        model.setBubbleId(bubbleId);
//
//        float precentWidth = (mBitmap.getWidth() * rScale) / mScreenwidth;
//        model.setScaling(precentWidth);
//
//        model.setxLocation(minX / mScreenwidth);
//        model.setyLocation(minY / mScreenwidth);
//        model.setText(mStr);
//
//        return model;
//    }


    private boolean isInBitmap(MotionEvent event) {
        float[] arrayOfFloat1 = new float[9];
        this.matrix.getValues(arrayOfFloat1);

        float f1 = 0.0F * arrayOfFloat1[0] + 0.0F * arrayOfFloat1[1] + arrayOfFloat1[2];
        float f2 = 0.0F * arrayOfFloat1[3] + 0.0F * arrayOfFloat1[4] + arrayOfFloat1[5];

        float f3 = arrayOfFloat1[0] * this.mBitmap.getWidth() + 0.0F * arrayOfFloat1[1] + arrayOfFloat1[2];
        float f4 = arrayOfFloat1[3] * this.mBitmap.getWidth() + 0.0F * arrayOfFloat1[4] + arrayOfFloat1[5];

        float f5 = 0.0F * arrayOfFloat1[0] + arrayOfFloat1[1] * this.mBitmap.getHeight() + arrayOfFloat1[2];
        float f6 = 0.0F * arrayOfFloat1[3] + arrayOfFloat1[4] * this.mBitmap.getHeight() + arrayOfFloat1[5];

        float f7 = arrayOfFloat1[0] * this.mBitmap.getWidth() + arrayOfFloat1[1] * this.mBitmap.getHeight() + arrayOfFloat1[2];
        float f8 = arrayOfFloat1[3] * this.mBitmap.getWidth() + arrayOfFloat1[4] * this.mBitmap.getHeight() + arrayOfFloat1[5];

        float[] arrayOfFloat2 = new float[4];
        float[] arrayOfFloat3 = new float[4];

        arrayOfFloat2[0] = f1;
        arrayOfFloat2[1] = f3;
        arrayOfFloat2[2] = f7;
        arrayOfFloat2[3] = f5;

        arrayOfFloat3[0] = f2;
        arrayOfFloat3[1] = f4;
        arrayOfFloat3[2] = f8;
        arrayOfFloat3[3] = f6;
        return pointInRect(arrayOfFloat2, arrayOfFloat3, event.getX(0), event.getY(0));
    }


    private boolean pointInRect(float[] xRange, float[] yRange, float x, float y) {

        double a1 = Math.hypot(xRange[0] - xRange[1], yRange[0] - yRange[1]);
        double a2 = Math.hypot(xRange[1] - xRange[2], yRange[1] - yRange[2]);
        double a3 = Math.hypot(xRange[3] - xRange[2], yRange[3] - yRange[2]);
        double a4 = Math.hypot(xRange[0] - xRange[3], yRange[0] - yRange[3]);

        double b1 = Math.hypot(x - xRange[0], y - yRange[0]);
        double b2 = Math.hypot(x - xRange[1], y - yRange[1]);
        double b3 = Math.hypot(x - xRange[2], y - yRange[2]);
        double b4 = Math.hypot(x - xRange[3], y - yRange[3]);

        double u1 = (a1 + b1 + b2) / 2;
        double u2 = (a2 + b2 + b3) / 2;
        double u3 = (a3 + b3 + b4) / 2;
        double u4 = (a4 + b4 + b1) / 2;


        double s = a1 * a2;
        double ss = Math.sqrt(u1 * (u1 - a1) * (u1 - b1) * (u1 - b2))
                + Math.sqrt(u2 * (u2 - a2) * (u2 - b2) * (u2 - b3))
                + Math.sqrt(u3 * (u3 - a3) * (u3 - b3) * (u3 - b4))
                + Math.sqrt(u4 * (u4 - a4) * (u4 - b4) * (u4 - b1));
        return Math.abs(s - ss) < 0.5;


    }


    private boolean isInButton(MotionEvent event, Rect rect) {
        int left = rect.left;
        int right = rect.right;
        int top = rect.top;
        int bottom = rect.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    private boolean isInResize(MotionEvent event) {
        int left = -20 + this.dst_resize.left;
        int top = -20 + this.dst_resize.top;
        int right = 20 + this.dst_resize.right;
        int bottom = 20 + this.dst_resize.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    private void midPointToStartPoint(MotionEvent event) {
        float[] arrayOfFloat = new float[9];
        matrix.getValues(arrayOfFloat);
        float f1 = 0.0f * arrayOfFloat[0] + 0.0f * arrayOfFloat[1] + arrayOfFloat[2];
        float f2 = 0.0f * arrayOfFloat[3] + 0.0f * arrayOfFloat[4] + arrayOfFloat[5];
        float f3 = f1 + event.getX(0);
        float f4 = f2 + event.getY(0);
        mid.set(f3 / 2, f4 / 2);
    }

    private void midDiagonalPoint(PointF paramPointF) {
        float[] arrayOfFloat = new float[9];
        this.matrix.getValues(arrayOfFloat);
        float f1 = 0.0F * arrayOfFloat[0] + 0.0F * arrayOfFloat[1] + arrayOfFloat[2];
        float f2 = 0.0F * arrayOfFloat[3] + 0.0F * arrayOfFloat[4] + arrayOfFloat[5];
        float f3 = arrayOfFloat[0] * this.mBitmap.getWidth() + arrayOfFloat[1] * this.mBitmap.getHeight() + arrayOfFloat[2];
        float f4 = arrayOfFloat[3] * this.mBitmap.getWidth() + arrayOfFloat[4] * this.mBitmap.getHeight() + arrayOfFloat[5];
        float f5 = f1 + f3;
        float f6 = f2 + f4;
        paramPointF.set(f5 / 2.0F, f6 / 2.0F);
    }


    private float rotationToStartPoint(MotionEvent event) {

        float[] arrayOfFloat = new float[9];
        matrix.getValues(arrayOfFloat);
        float x = 0.0f * arrayOfFloat[0] + 0.0f * arrayOfFloat[1] + arrayOfFloat[2];
        float y = 0.0f * arrayOfFloat[3] + 0.0f * arrayOfFloat[4] + arrayOfFloat[5];
        double arc = Math.atan2(event.getY(0) - y, event.getX(0) - x);
        return (float) Math.toDegrees(arc);
    }


    private float diagonalLength(MotionEvent event) {
        float diagonalLength = (float) Math.hypot(event.getX(0) - mid.x, event.getY(0) - mid.y);
        return diagonalLength;
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    public interface OperationListener {
        void onDeleteClick();

        void onEdit(BubbleTextView bubbleTextView);

        void onClick(BubbleTextView bubbleTextView);

        void onTop(BubbleTextView bubbleTextView);

        void onSetSize();
    }

    public void setOperationListener(OperationListener operationListener) {
        this.operationListener = operationListener;
    }

    public void setInEdit(boolean isInEdit) {
        this.isInEdit = isInEdit;
        invalidate();
    }


    private String[] autoSplit(String content, Paint p, float width) {
        int length = content.length();
        float textWidth = p.measureText(content);
        if (textWidth <= width) {
            return new String[]{content};
        }

        int start = 0, end = 1, i = 0;
        int lines = (int) Math.ceil(textWidth / width);
        String[] lineTexts = new String[lines];
        while (start < length) {
            if (p.measureText(content, start, end) > width) {
                lineTexts[i++] = (String) content.subSequence(start, end);
                start = end;
            }
            if (end == length) {
                lineTexts[i] = (String) content.subSequence(start, end);
                break;
            }
            end += 1;
        }
        return lineTexts;
    }

    public String getmStr() {
        return mStr;
    }
}
