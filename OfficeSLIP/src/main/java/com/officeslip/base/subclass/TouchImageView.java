/*
 * TouchImageView.java
 * By: Michael Ortiz
 * Updated By: Patrick Lackemacher
 * Updated By: Babay88
 * Updated By: @ipsilondev
 * Updated By: hank-cp
 * Updated By: singpolyma
 * -------------------
 * Extends Android ImageView to include pinch zooming, panning, fling and double tap zoom.
 */

package com.officeslip.base.subclass;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.OverScroller;
import android.widget.Scroller;

/**
 * 이미지 원본보기의 viewPager를 컨트롤.
 */

public class TouchImageView extends androidx.appcompat.widget.AppCompatImageView {

    private static final String DEBUG = "DEBUG";

    //
    // SuperMin and SuperMax multipliers. Determine how much the m_thumbData can be
    // zoomed below or above the zoom boundaries, before animating back to the
    // min/max zoom boundary.
    //
    private static final float SUPER_MIN_MULTIPLIER = .75f;
    private static final float SUPER_MAX_MULTIPLIER = 1.25f;

    //
    // Scale of m_thumbData ranges from minScale to maxScale, where minScale == 1
    // when the m_thumbData is stretched to fit ja.view.
    //
    private float normalizedScale;

    //
    // Matrix applied to m_thumbData. MSCALE_X and MSCALE_Y should always be equal.
    // MTRANS_X and MTRANS_Y are the other values used. prevMatrix is the matrix
    // saved prior to the screen rotating.
    //
    private Matrix matrix, prevMatrix;

    public enum State { NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM }
    public State state;

    private float minScale;
    private float maxScale;
    private float superMinScale;
    private float superMaxScale;
    private float[] m;

    private Context context;
    private Fling fling;

    private ScaleType mScaleType;

    private boolean imageRenderedAtLeastOnce;
    private boolean onDrawReady;

    private ZoomVariables delayedZoomVariables;

    //
    // Size of ja.view and previous ja.view size (ie before rotation)
    //
    public int viewWidth, viewHeight, prevViewWidth, prevViewHeight;

    //
    // Size of m_thumbData when it is stretched to fit ja.view. Before and After rotation.
    //
    private float matchViewWidth, matchViewHeight, prevMatchViewWidth, prevMatchViewHeight;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private GestureDetector.OnDoubleTapListener doubleTapListener = null;
    private OnTouchListener userTouchListener = null;
    private OnTouchImageViewListener touchImageViewListener = null;

    //   private ArrayList<EntryMemo> memoEntry;
    public float ratioW = -1;
    public float ratioH = -1;

    private ImageViewDelegate delegate = null;

    private boolean touchEnable = true;


//    /**
//     * Implements detect document ja.view
//     * @param context
//     */
//
//    private Paint mBackgroundPaint = new Paint();
//    private Paint mBorderPaint = new Paint();
//    private Paint mCircleBorderPaint = new Paint();
//    private Path mSelectionPath = new Path();
//    private Path mBackgroundPath = new Path();
//
//    //Paints for each circle
//    private Paint mUpperLeftCirclePaint = new Paint();
//    private Paint mUpperRightCirclePaint = new Paint();
//    private Paint mLowerRightCirclePaint = new Paint();
//    private Paint mLowerLeftCirclePaint = new Paint();
//
//    private List<PointF> m_points  = null;
//    private int m_orientation = 0;
//
//    //Square positions
//    private PointF mUpperLeftPoint = null;
//    private PointF mUpperRightPoint = null;
//    private PointF mLowerLeftPoint = null;
//    private PointF mLowerRightPoint = null;
//    //Last position
//    private PointF mLastTouchedPoint = null;
//
//    private boolean m_isTouched = false;
//    private int m_nCirCleWidth = 15;
//
//    //Set last action
//    private int m_lastAction = ACTION_MOVE;
//
//
//    private static final int ACTION_TOUCH = 1001;
//    private static final int  ACTION_MOVE = 1002;
//
//    private static enum EditMode { VIEW, CROP};
//    private EditMode editMode;

    public interface  ImageViewDelegate {

        public void onSizeChanged(int w, int h, int oldw, int ildh);

        public void onLayout(boolean changed, int left, int top, int right, int bottom);

        public void onDraw(Canvas canvas);

        public void onTouchEvent(MotionEvent e);

//        public void onMatrix(Matrix m, TouchImageView ja.view);

    }

    public void setDelegate(ImageViewDelegate delegate)
    {
        this.delegate = delegate;
    }

    public void setTouchEnable(boolean isEnable)
    {
        this.touchEnable = isEnable;
    }

    public TouchImageView(Context context) {
        super(context);
        sharedConstructing(context);
        //init(null, 0);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
        //init(attrs, 0);
    }

    public TouchImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, 1);
        sharedConstructing(context);
        //init(attrs, defStyle);
    }


//    private void init()
//    {
//        mBackgroundPaint.setColor(ContextCompat.getColor(context, R.color.backgroundOverlay));
//
//        mBorderPaint.setColor(ContextCompat.getColor(context, R.color.Red));
//        mBorderPaint.setAntiAlias(true);
//        mBorderPaint.setStyle(Paint.Style.STROKE);
//        mBorderPaint.setStrokeWidth(6);
//
//        mUpperLeftCirclePaint.setColor(ContextCompat.getColor(context, R.color.docDetectCircle));
//        mUpperLeftCirclePaint.setAntiAlias(true);
//        mUpperLeftCirclePaint.setStyle(Paint.Style.FILL);
//        mUpperLeftCirclePaint.setStrokeWidth(4);
//
//        mUpperRightCirclePaint.setColor(ContextCompat.getColor(context, R.color.docDetectCircle));
//        mUpperRightCirclePaint.setAntiAlias(true);
//        mUpperRightCirclePaint.setStyle(Paint.Style.FILL);
//        mUpperRightCirclePaint.setStrokeWidth(4);
//
//        mLowerLeftCirclePaint.setColor(ContextCompat.getColor(context, R.color.docDetectCircle));
//        mLowerLeftCirclePaint.setAntiAlias(true);
//        mLowerLeftCirclePaint.setStyle(Paint.Style.FILL);
//        mLowerLeftCirclePaint.setStrokeWidth(4);
//
//        mLowerRightCirclePaint.setColor(ContextCompat.getColor(context, R.color.docDetectCircle));
//        mLowerRightCirclePaint.setAntiAlias(true);
//        mLowerRightCirclePaint.setStyle(Paint.Style.FILL);
//        mLowerRightCirclePaint.setStrokeWidth(4);
//
//
//        mCircleBorderPaint.setColor(ContextCompat.getColor(context, R.color.White));
//        mCircleBorderPaint.setAntiAlias(true);
//        mCircleBorderPaint.setStyle(Paint.Style.STROKE);
//        mCircleBorderPaint.setStrokeWidth(4);
//    }
//
//    public void setCurPoints(List<PointF> points)
//    {
//        this.m_points = points;
//    }
//
//    public void setOrientation(int nOrientation)
//    {
//        this.m_orientation = nOrientation;
//    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(context, new GestureListener());
        matrix = new Matrix();
        prevMatrix = new Matrix();
        m = new float[9];
        normalizedScale = 1;
        if (mScaleType == null) {
            mScaleType = ScaleType.FIT_CENTER;
        }
        minScale = 1;
        maxScale = 3;
        superMinScale = SUPER_MIN_MULTIPLIER * minScale;
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale;
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
        setState(State.NONE);
        onDrawReady = false;
        super.setOnTouchListener(new PrivateOnTouchListener());
    }
    @Override
    public void setOnTouchListener(View.OnTouchListener l) {
        userTouchListener = l;
    }

    public void setOnTouchImageViewListener(OnTouchImageViewListener l) {
        touchImageViewListener = l;
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener l) {
        doubleTapListener = l;
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        savePreviousImageValues();
        fitImageToView();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        savePreviousImageValues();
        fitImageToView();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        savePreviousImageValues();
        fitImageToView();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        savePreviousImageValues();
        fitImageToView();
    }


    @Override
    public void setScaleType(ScaleType type) {
        if (type == ScaleType.FIT_START || type == ScaleType.FIT_END) {
            throw new UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END");
        }
        if (type == ScaleType.MATRIX) {
            super.setScaleType(ScaleType.MATRIX);

        } else {
            mScaleType = type;
            if (onDrawReady) {
                //
                // If the m_thumbData is already rendered, scaleType has been called programmatically
                // and the TouchImageView should be updated with the new scaleType.
                //
                setZoom(this);
            }
        }
    }

    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(delegate!=null)
        {
            delegate.onSizeChanged(w,h,oldw,oldh);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(delegate!=null)
        {
            delegate.onLayout(changed,left,top,right,bottom);
        }
    }

    /**
     * Returns false if m_thumbData is in initial, unzoomed state. False, otherwise.
     * @return true if m_thumbData is zoomed
     */
    public boolean isZoomed() {
        return normalizedScale != 1;
    }

    /**
     * Return a Rect representing the zoomed m_thumbData.
     * @return rect representing zoomed m_thumbData
     */
    public RectF getZoomedRect() {
        if (mScaleType == ScaleType.FIT_XY) {
            throw new UnsupportedOperationException("getZoomedRect() not supported with FIT_XY");
        }
        PointF topLeft = transformCoordTouchToBitmap(0, 0, true);
        PointF bottomRight = transformCoordTouchToBitmap(viewWidth, viewHeight, true);

        float w = getDrawable().getIntrinsicWidth();
        float h = getDrawable().getIntrinsicHeight();
        return new RectF(topLeft.x / w, topLeft.y / h, bottomRight.x / w, bottomRight.y / h);
    }

    public RectF getCurrentRect() {
        if (mScaleType == ScaleType.FIT_XY) {
            throw new UnsupportedOperationException("getZoomedRect() not supported with FIT_XY");
        }
        PointF topLeft = transformCoordTouchToBitmap(0, 0, true);
//    	float imgRatioW = getDrawable().getintr;
//    	float imgRatioH = 0;
        PointF bottomRight = transformCoordTouchToBitmap(viewWidth, viewHeight, true);
        return new RectF(topLeft.x, topLeft.y , bottomRight.x, bottomRight.y);
    }

    /**
     * Save the current matrix and ja.view dimensions
     * in the prevMatrix and prevView variables.
     */
    private void savePreviousImageValues() {
        if (matrix != null && viewHeight != 0 && viewWidth != 0) {
            matrix.getValues(m);
            prevMatrix.setValues(m);
            prevMatchViewHeight = matchViewHeight;
            prevMatchViewWidth = matchViewWidth;
            prevViewHeight = viewHeight;
            prevViewWidth = viewWidth;
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putFloat("saveScale", normalizedScale);
        bundle.putFloat("matchViewHeight", matchViewHeight);
        bundle.putFloat("matchViewWidth", matchViewWidth);
        bundle.putInt("viewWidth", viewWidth);
        bundle.putInt("viewHeight", viewHeight);
        matrix.getValues(m);
        bundle.putFloatArray("matrix", m);
        bundle.putBoolean("imageRendered", imageRenderedAtLeastOnce);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            normalizedScale = bundle.getFloat("saveScale");
            m = bundle.getFloatArray("matrix");
            prevMatrix.setValues(m);
            prevMatchViewHeight = bundle.getFloat("matchViewHeight");
            prevMatchViewWidth = bundle.getFloat("matchViewWidth");
            prevViewHeight = bundle.getInt("viewHeight");
            prevViewWidth = bundle.getInt("viewWidth");
            imageRenderedAtLeastOnce = bundle.getBoolean("imageRendered");
            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        onDrawReady = true;
        imageRenderedAtLeastOnce = true;
        if (delayedZoomVariables != null) {
            setZoom(delayedZoomVariables.scale, delayedZoomVariables.focusX, delayedZoomVariables.focusY, delayedZoomVariables.scaleType);
            delayedZoomVariables = null;
        }
        super.onDraw(canvas);

//        if(memoEntry != null) {
//            for(int i = 0; i < memoEntry.size();i++) {
//                String pageRect[] = memoEntry.get(i).getRect().split(",");
//                //원본과 현재 뷰어의 비율계산
//                if(getDrawable()!= null)
//                {
//                    if(ratioW == -1)
//                    {
//                        ratioW = getDrawable().getIntrinsicWidth() / (Float.parseFloat(pageRect[0])+Float.parseFloat(pageRect[2]));
//                    }
//                    if(ratioH == -1)
//                    {
//                        ratioH = getDrawable().getIntrinsicHeight() /  (Float.parseFloat(pageRect[1])+Float.parseFloat(pageRect[3]));
//                    }
//
//                    drawRects(canvas, memoEntry.get(i),ratioW,ratioH);
//                }
//            }
//        }

        if(delegate != null)
        {
            delegate.onDraw(canvas);
        }

    }

//    //메모 그리기
//    private void drawRects(Canvas canvas, EntryMemo memoEntry, float ratioW, float ratioH) {
//        String rect[] = null;
//        if(memoEntry.getCategory().equals("LightPen"))
//            rect = memoEntry.getPenRect().split(",");
//        else if(memoEntry.getCategory().equals("Shape"))
//            rect = memoEntry.getShapeRect().split(",");
//        else if(memoEntry.getCategory().equals("Note"))
//            rect = memoEntry.getNoteRect().split(",");
////
////    	Log.d("viewWidth",viewWidth+"A");
////    	Log.d("viewHeight",viewHeight+"A");
////    	Log.d("getImageWidth()",getImageWidth()+"A");
////    	Log.d("getImageHeight()",getImageHeight()+"A");
////    	Log.d("getCurrentZoom()",getCurrentZoom()+"A");
////    	Log.d("getDrawable().getIntrinsicWidth()",getDrawable().getIntrinsicWidth()+"A");
////    	Log.d("getDrawable().getIntrinsicHeight()",getDrawable().getIntrinsicHeight()+"A");
////    	Log.d("ratioW",ratioW+"A");
////    	Log.d("ratioH",ratioH+"A");
////    	Log.d("getCurrentRect().left",getCurrentRect().left+"A");
////    	Log.d("getCurrentRect().top",getCurrentRect().top+"A");
////    	Log.d("getCurrentRect().right",getCurrentRect().right+"A");
////    	Log.d("getCurrentRect().bottom",getCurrentRect().bottom+"A");
////
//
//        if(rect != null) {
//            //실제 이미지 좌표
//            float zoomedWRatio = getImageWidth() / (getDrawable().getIntrinsicWidth() * getCurrentZoom());
//            float zoomedHRatio = getImageHeight() /(getDrawable().getIntrinsicHeight() * getCurrentZoom());
//            float left = Float.parseFloat(rect[0]) * ratioW;
//            float top = Float.parseFloat(rect[1]) * ratioH;
//            float right = Float.parseFloat(rect[2]) * ratioW;
//            float bottom = Float.parseFloat(rect[3]) * ratioH;
//
//            float resultLeft = 0;
//            float resultTop = 0;
//            float resultRight = 0;
//            float resultBottom = 0;
//
//            float veticalSpace = (viewWidth - getImageWidth()) / 2;
//            float horizontalSpace = (viewHeight - getImageHeight()) / 2;
//
//            float drawableLeft = (left * getCurrentZoom())* zoomedWRatio;
//            float drawableTop = (top * getCurrentZoom())* zoomedHRatio;
//            float drawableRight = (right * getCurrentZoom())* zoomedWRatio;
//            float drawableBottom = (bottom * getCurrentZoom())* zoomedHRatio;
//
//            float screenLeft = (getCurrentRect().left * getCurrentZoom()) * zoomedWRatio;
//            float screenTop = (getCurrentRect().top * getCurrentZoom()) * zoomedHRatio;
//            float screenRight =  (getCurrentRect().right * getCurrentZoom()) * zoomedWRatio;
//            float screenBottom =  (getCurrentRect().bottom * getCurrentZoom()) * zoomedHRatio;
//
////
////			Log.d("left",left+"A");
////			Log.d("zoomedWRatio",zoomedWRatio+"A");
////			Log.d("zoomedHRatio",zoomedHRatio+"A");
////			Log.d("resultImageWidth ",((viewWidth - getImageWidth()) / 2) +"A");
//
//            //뷰 화면보다 크기가 작을경우( 보통화면 or 축소화면 )
//            if(viewWidth >= getImageWidth()) {
//
//                resultLeft 	= veticalSpace + drawableLeft;
//                resultRight 	= veticalSpace + drawableRight;
//            }
//
//            //뷰 화면보다 크기가 클 경우( 보통화면 or 확대화면 )
//            else {
//
//                resultLeft 	= veticalSpace + drawableLeft - screenLeft - veticalSpace;
//                resultRight 	= veticalSpace + drawableRight - screenLeft - veticalSpace;
//            }
//
//            //뷰 화면보다 크기가 작을경우( 보통화면 )
//            if(viewHeight >= getImageHeight()) {
//
//                resultTop 		= horizontalSpace + drawableTop;
//                resultBottom 	=  horizontalSpace + drawableBottom;
//            }
//
//            //뷰 화면보다 크기가 클 경우( 확대화면 )
//            else {
//
//                resultTop 		= ((top * getCurrentZoom()) - (getCurrentRect().top * getCurrentZoom())) * zoomedHRatio;
//                resultBottom 	= ((bottom * getCurrentZoom()) - (getCurrentRect().top * getCurrentZoom())) * zoomedHRatio;
//            }
////
////			Log.d("resultLeft",resultLeft+"A");
////	    	Log.d("resultTop",resultTop+"A");
////	    	Log.d("resultRight",resultRight+"A");
////	    	Log.d("resultBottom",resultBottom+"A");
//
//            //펜
//            if(memoEntry.getCategory().equals("LightPen")) {
//                String rgb[] 	= memoEntry.getPenLineColor().split(",");
//
//                int textSize		= Integer.parseInt(memoEntry.getPenLineWidth());
//
//                Paint paint 		= new Paint();
//                if(rgb != null)
//                    paint.setColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
//                else
//                    return;
//
//                paint.setTextSize(textSize);
//                paint.setAlpha(128);
//                paint.setAntiAlias(true);
//                //그리기
//                canvas.drawRect(resultLeft, resultTop, resultRight, resultBottom,	paint);
//            }
//            //도형
//            else if(memoEntry.getCategory().equals("Shape")) {
//                String lineColor[] 		= memoEntry.getShapeLineColor().split(",");
//                String backColor[] 	= memoEntry.getShapeBackColor().split(",");
//                int lineWidth			= Integer.parseInt(memoEntry.getShapeLineWidth());
//                int alpha					= Integer.parseInt(memoEntry.getShapeAlpha());
//
//                //그리기
//                Paint paint 		= new Paint();
//                //선 색상
//                if(lineColor != null)
//                    paint.setColor(Color.rgb(Integer.parseInt(lineColor[0]), Integer.parseInt(lineColor[1]), Integer.parseInt(lineColor[2])));
//                else
//                    return;
//                paint.setStyle(Style.STROKE);
//                paint.setStrokeWidth(lineWidth);
//
//                paint.setAntiAlias(true);
//
//                if(memoEntry.getShapeStyle().equals("RECTANGLE")) {
//                    canvas.drawRect(resultLeft, resultTop, resultRight, resultBottom,	paint);
//
//                    //배경색
//                    if(backColor != null)
//                        paint.setColor(Color.rgb(Integer.parseInt(backColor[0]), Integer.parseInt(backColor[1]), Integer.parseInt(backColor[2])));
//                    else
//                        return;
//                    paint.setStyle(Style.FILL);
//                    paint.setAlpha(alpha);
//                    canvas.drawRect(resultLeft, resultTop, resultRight, resultBottom,	paint);
//                } else if(memoEntry.getShapeStyle().equals("ELLIPSE")) {
//                    canvas.drawOval(new RectF(resultLeft,resultTop,resultRight,resultBottom), paint);
//
//                    //배경색
//                    if(backColor != null)
//                        paint.setColor(Color.rgb(Integer.parseInt(backColor[0]), Integer.parseInt(backColor[1]), Integer.parseInt(backColor[2])));
//                    else
//                        return;
//                    paint.setStyle(Style.FILL);
//                    paint.setAlpha(alpha);
//                    canvas.drawOval(new RectF(resultLeft,resultTop,resultRight,resultBottom), paint);
//                }
//            }
//            //메모
//            else if(memoEntry.getCategory().equals("Note")) {
//                String lineColor[] 		= memoEntry.getNoteLineColor().split(",");
//                String backColor[] 	= memoEntry.getNoteBackColor().split(",");
//                String fontColor[] 	= memoEntry.getNoteTextColor().split(",");
//                String fontFamily		= memoEntry.getNoteFontName();
//                String m_thumbData			= memoEntry.getNoteImage();
//                String comment		= memoEntry.getNoteComment();
//                int alpha					= Integer.parseInt(memoEntry.getNoteAlpha());
//                int bold					= Integer.parseInt(memoEntry.getNoteBold());
//                int italic					= Integer.parseInt(memoEntry.getNoteItalic());
//                float fontSize			= Integer.parseInt(memoEntry.getNoteFontSize()) * getCurrentZoom();
//
//                //그리기
//                Paint paint 		= new Paint();
//                //선 색상
//                if(lineColor != null)
//                    paint.setColor(Color.rgb(Integer.parseInt(lineColor[0]), Integer.parseInt(lineColor[1]), Integer.parseInt(lineColor[2])));
//                else
//                    return;
//                paint.setStyle(Style.STROKE);
//                paint.setStrokeWidth(1);
//                paint.setAntiAlias(true);
//                //메모가 사각형일 시
//                if(memoEntry.getNoteType().equals("RECT")) {
//                    canvas.drawRect(resultLeft, resultTop, resultRight, resultBottom,	paint);
//                    //배경색
//                    if(backColor != null)
//                        paint.setColor(Color.rgb(Integer.parseInt(backColor[0]), Integer.parseInt(backColor[1]), Integer.parseInt(backColor[2])));
//                    else
//                        return;
//                    paint.setStyle(Style.FILL);
//                    paint.setAlpha(alpha);
//                    canvas.drawRect(resultLeft, resultTop, resultRight, resultBottom,	paint);
//                    //글자 색
//                    if(fontColor != null)
//                        paint.setColor(Color.rgb(Integer.parseInt(fontColor[0]), Integer.parseInt(fontColor[1]), Integer.parseInt(fontColor[2])));
//                    else
//                        return;
//                    paint.setTextSize(fontSize);
//                    if(bold == 1)
//                        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//                    if(italic == 1)
//                        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
//                    canvas.drawText(comment, resultLeft, resultTop+fontSize, paint);
//                }
//            }
//        }
//        //도형의 좌표가 없을 시
//        else {
//            return;
//        }
//    }

    // dp 를 pixel로 변환
    public int DPtoPixel(int i) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((WindowManager) getContext().getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(
                displaymetrics);
        return (int) ((float) i * displaymetrics.density);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        savePreviousImageValues();
    }

    /**
     * Get the max zoom multiplier.
     * @return max zoom multiplier.
     */
    public float getMaxZoom() {
        return maxScale;
    }

    /**
     * Set the max zoom multiplier. Default value: 3.
     * @param max max zoom multiplier.
     */
    public void setMaxZoom(float max) {
        maxScale = max;
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale;
    }

    /**
     * Get the min zoom multiplier.
     * @return min zoom multiplier.
     */
    public float getMinZoom() {
        return minScale;
    }

    /**
     * Get the current zoom. This is the zoom relative to the initial
     * scale, not the original resource.
     * @return current zoom multiplier.
     */
    public float getCurrentZoom() {
        return normalizedScale;
    }

    /**
     * Set the min zoom multiplier. Default value: 1.
     * @param min min zoom multiplier.
     */
    public void setMinZoom(float min) {
        minScale = min;
        superMinScale = SUPER_MIN_MULTIPLIER * minScale;
    }

    /**
     * Reset zoom and translation to initial state.
     */
    public void resetZoom() {
        normalizedScale = 1;
        fitImageToView();
    }

    /**
     * Set zoom to the specified scale. Image will be centered by default.
     * @param scale
     */
    public void setZoom(float scale) {
        setZoom(scale, 0.5f, 0.5f);
    }

    /**
     * Set zoom to the specified scale. Image will be centered around the point
     * (focusX, focusY). These floats range from 0 to 1 and denote the focus point
     * as a fraction from the left and top of the ja.view. For example, the top left
     * corner of the m_thumbData would be (0, 0). And the bottom right corner would be (1, 1).
     * @param scale
     * @param focusX
     * @param focusY
     */
    public void setZoom(float scale, float focusX, float focusY) {
        setZoom(scale, focusX, focusY, mScaleType);
    }

    /**
     * Set zoom to the specified scale. Image will be centered around the point
     * (focusX, focusY). These floats range from 0 to 1 and denote the focus point
     * as a fraction from the left and top of the ja.view. For example, the top left
     * corner of the m_thumbData would be (0, 0). And the bottom right corner would be (1, 1).
     * @param scale
     * @param focusX
     * @param focusY
     * @param scaleType
     */
    public void setZoom(float scale, float focusX, float focusY, ScaleType scaleType) {
        //
        // setZoom can be called before the m_thumbData is on the screen, but at this point,
        // m_thumbData and ja.view sizes have not yet been calculated in onMeasure. Thus, we should
        // delay calling setZoom until the ja.view has been measured.
        //
        if (!onDrawReady) {
            delayedZoomVariables = new ZoomVariables(scale, focusX, focusY, scaleType);
            return;
        }

        if (scaleType != mScaleType) {
            setScaleType(scaleType);
        }
        resetZoom();
        scaleImage(scale, viewWidth / 2, viewHeight / 2, true);
        matrix.getValues(m);
        m[Matrix.MTRANS_X] = -((focusX * getImageWidth()) - (viewWidth * 0.5f));
        m[Matrix.MTRANS_Y] = -((focusY * getImageHeight()) - (viewHeight * 0.5f));
        matrix.setValues(m);
        fixTrans();
        setImageMatrix(matrix);
        savePreviousImageValues();
    }

    /**
     * Set zoom parameters equal to another TouchImageView. Including scale, position,
     * and ScaleType.
     * @param
     */
    public void setZoom(TouchImageView img) {
        PointF center = img.getScrollPosition();
        setZoom(img.getCurrentZoom(), center.x, center.y, img.getScaleType());
    }

    /**
     * Return the point at the center of the zoomed m_thumbData. The PointF coordinates range
     * in value between 0 and 1 and the focus point is denoted as a fraction from the left
     * and top of the ja.view. For example, the top left corner of the m_thumbData would be (0, 0).
     * And the bottom right corner would be (1, 1).
     * @return PointF representing the scroll position of the zoomed m_thumbData.
     */
    public PointF getScrollPosition() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return null;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        PointF point = transformCoordTouchToBitmap(viewWidth / 2, viewHeight / 2, true);
        point.x /= drawableWidth;
        point.y /= drawableHeight;
        return point;
    }

    /**
     * Set the focus point of the zoomed m_thumbData. The focus points are denoted as a fraction from the
     * left and top of the ja.view. The focus points can range in value between 0 and 1.
     * @param focusX
     * @param focusY
     */
    public void setScrollPosition(float focusX, float focusY) {
        setZoom(normalizedScale, focusX, focusY);
    }

    /**
     * Performs boundary checking and fixes the m_thumbData matrix if it
     * is out of bounds.
     */
    private void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, getImageWidth());
        float fixTransY = getFixTrans(transY, viewHeight, getImageHeight());

        if (fixTransX != 0 || fixTransY != 0) {
            matrix.postTranslate(fixTransX, fixTransY);
        }
    }

    /**
     * When transitioning from zooming from focus to zoom from center (or vice versa)
     * the m_thumbData can become unaligned within the ja.view. This is apparent when zooming
     * quickly. When the content size is less than the ja.view size, the content will often
     * be centered incorrectly within the ja.view. fixScaleTrans first calls fixTrans() and
     * then makes sure the m_thumbData is centered correctly within the ja.view.
     */
    private void fixScaleTrans() {
        fixTrans();
        matrix.getValues(m);
        if (getImageWidth() < viewWidth) {
            m[Matrix.MTRANS_X] = (viewWidth - getImageWidth()) / 2;
        }

        if (getImageHeight() < viewHeight) {
            m[Matrix.MTRANS_Y] = (viewHeight - getImageHeight()) / 2;
        }
        matrix.setValues(m);
    }

    private float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;

        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    private float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    public float getImageWidth() {
        return matchViewWidth * normalizedScale;
    }

    public float getImageHeight() {
        return matchViewHeight * normalizedScale;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
            setMeasuredDimension(0, 0);
            return;
        }

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        viewWidth = setViewSize(widthMode, widthSize, drawableWidth);
        viewHeight = setViewSize(heightMode, heightSize, drawableHeight);

        //
        // Set ja.view dimensions
        //
        setMeasuredDimension(viewWidth, viewHeight);

        //
        // Fit content within ja.view
        //
        fitImageToView();
    }

    /**
     * If the normalizedScale is equal to 1, then the m_thumbData is made to fit the screen. Otherwise,
     * it is made to fit the screen according to the dimensions of the previous m_thumbData matrix. This
     * allows the m_thumbData to maintain its zoom after rotation.
     */
    public void fitImageToView() {
        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
            return;
        }
        if (matrix == null || prevMatrix == null) {
            return;
        }

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        //
        // Scale m_thumbData for ja.view
        //
        float scaleX = (float) viewWidth / drawableWidth;
        float scaleY = (float) viewHeight / drawableHeight;

        switch (mScaleType) {
            case CENTER:
                scaleX = scaleY = 1;
                break;

            case CENTER_CROP:
                scaleX = scaleY = Math.max(scaleX, scaleY);
                break;

            case CENTER_INSIDE:
                scaleX = scaleY = Math.min(1, Math.min(scaleX, scaleY));

            case FIT_CENTER:
                scaleX = scaleY = Math.min(scaleX, scaleY);
                break;

            case FIT_XY:
                break;

            default:
                //
                // FIT_START and FIT_END not supported
                //
                throw new UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END");

        }

        //
        // Center the m_thumbData
        //
        float redundantXSpace = viewWidth - (scaleX * drawableWidth);
        float redundantYSpace = viewHeight - (scaleY * drawableHeight);
        matchViewWidth = viewWidth - redundantXSpace;
        matchViewHeight = viewHeight - redundantYSpace;

        if (!isZoomed() && !imageRenderedAtLeastOnce) {
            //
            // Stretch and center m_thumbData to fit ja.view
            //
            matrix.setScale(scaleX, scaleY);
            matrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2);
            normalizedScale = 1;

        } else {
            //
            // These values should never be 0 or we will set viewWidth and viewHeight
            // to NaN in translateMatrixAfterRotate. To avoid this, call savePreviousImageValues
            // to set them equal to the current values.
            //
            if (prevMatchViewWidth == 0 || prevMatchViewHeight == 0) {
                savePreviousImageValues();
            }

            prevMatrix.getValues(m);

            //
            // Rescale Matrix after rotation
            //
            m[Matrix.MSCALE_X] = matchViewWidth / drawableWidth * normalizedScale;
            m[Matrix.MSCALE_Y] = matchViewHeight / drawableHeight * normalizedScale;

            //
            // TransX and TransY from previous matrix
            //
            float transX = m[Matrix.MTRANS_X];
            float transY = m[Matrix.MTRANS_Y];

            //
            // Width
            //
            float prevActualWidth = prevMatchViewWidth * normalizedScale;
            float actualWidth = getImageWidth();
            translateMatrixAfterRotate(Matrix.MTRANS_X, transX, prevActualWidth, actualWidth, prevViewWidth, viewWidth, drawableWidth);

            //
            // Height
            //
            float prevActualHeight = prevMatchViewHeight * normalizedScale;
            float actualHeight = getImageHeight();
            translateMatrixAfterRotate(Matrix.MTRANS_Y, transY, prevActualHeight, actualHeight, prevViewHeight, viewHeight, drawableHeight);

            //
            // Set the matrix to the adjusted scale and translate values.
            //
            matrix.setValues(m);
        }
        fixTrans();
        setImageMatrix(matrix);
    }

    /**
     * Set ja.view dimensions based on layout params
     *
     * @param mode
     * @param size
     * @param drawableWidth
     * @return
     */
    private int setViewSize(int mode, int size, int drawableWidth) {
        int viewSize;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                viewSize = size;
                break;

            case MeasureSpec.AT_MOST:
                viewSize = Math.min(drawableWidth, size);
                break;

            case MeasureSpec.UNSPECIFIED:
                viewSize = drawableWidth;
                break;

            default:
                viewSize = size;
                break;
        }
        return viewSize;
    }

    /**
     * After rotating, the matrix needs to be translated. This function finds the area of m_thumbData
     * which was previously centered and adjusts translations so that is again the center, post-rotation.
     *
     * @param axis Matrix.MTRANS_X or Matrix.MTRANS_Y
     * @param trans the value of trans in that axis before the rotation
     * @param prevImageSize the width/weight of the m_thumbData before the rotation
     * @param imageSize width/weight of the m_thumbData after rotation
     * @param prevViewSize width/weight of ja.view before rotation
     * @param viewSize width/weight of ja.view after rotation
     * @param drawableSize width/weight of drawable
     */
    private void translateMatrixAfterRotate(int axis, float trans, float prevImageSize, float imageSize, int prevViewSize, int viewSize, int drawableSize) {
        if (imageSize < viewSize) {
            //
            // The width/weight of m_thumbData is less than the ja.view's width/weight. Center it.
            //
            m[axis] = (viewSize - (drawableSize * m[Matrix.MSCALE_X])) * 0.5f;

        } else if (trans > 0) {
            //
            // The m_thumbData is larger than the ja.view, but was not before rotation. Center it.
            //
            m[axis] = -((imageSize - viewSize) * 0.5f);

        } else {
            //
            // Find the area of the m_thumbData which was previously centered in the ja.view. Determine its distance
            // from the left/top side of the ja.view as a fraction of the entire m_thumbData's width/weight. Use that percentage
            // to calculate the trans in the new ja.view width/weight.
            //
            float percentage = (Math.abs(trans) + (0.5f * prevViewSize)) / prevImageSize;
            m[axis] = -((percentage * imageSize) - (viewSize * 0.5f));
        }
    }

    private void setState(State state) {
        this.state = state;
    }

    public boolean canScrollHorizontallyFroyo(int direction) {
        return canScrollHorizontally(direction);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        matrix.getValues(m);
        float x = m[Matrix.MTRANS_X];

        if (getImageWidth() < viewWidth) {
            return false;

        } else if (x >= -1 && direction < 0) {
            return false;

        } else if (Math.abs(x) + viewWidth + 1 >= getImageWidth() && direction > 0) {
            return false;
        }

        return true;
    }

    /**
     * Gesture Listener detects a single click or long click and passes that on
     * to the ja.view's listener.
     * @author Ortiz
     *
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            if(doubleTapListener != null) {
                return doubleTapListener.onSingleTapConfirmed(e);
            }
            return performClick();
        }

        @Override
        public void onLongPress(MotionEvent e)
        {
            performLongClick();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            if(touchEnable) {
                if (fling != null) {
                    //
                    // If a previous fling is still active, it should be cancelled so that two flings
                    // are not run simultaenously.
                    //
                    fling.cancelFling();
                }
                fling = new Fling((int) velocityX, (int) velocityY);
                compatPostOnAnimation(fling);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            boolean consumed = false;
            if(doubleTapListener != null && touchEnable) {
                consumed = doubleTapListener.onDoubleTap(e);
            }
            if (state == State.NONE && touchEnable) {
                float targetZoom = (normalizedScale == minScale) ? maxScale : minScale;
                DoubleTapZoom doubleTap = new DoubleTapZoom(targetZoom, e.getX(), e.getY(), false);
                compatPostOnAnimation(doubleTap);
                consumed = true;
            }
            return consumed;
        }
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if(doubleTapListener != null && touchEnable) {
                return doubleTapListener.onDoubleTapEvent(e);
            }
            return false;
        }
    }

    public interface OnTouchImageViewListener {
        public void onMove();
    }

    /**
     * Responsible for all touch events. Handles the heavy lifting of drag and also sends
     * touch events to Scale Detector and Gesture Detector.
     * @author Ortiz
     *
     */
    private class PrivateOnTouchListener implements OnTouchListener {

        //
        // Remember last point position for dragging
        //
        private PointF last = new PointF();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
          //  v.getParent().requestDisallowInterceptTouchEvent(true);
            mScaleDetector.onTouchEvent(event);
            mGestureDetector.onTouchEvent(event);
            PointF curr = new PointF(event.getX(), event.getY());

            if (state == State.NONE || state == State.DRAG || state == State.FLING) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        last.set(curr);
                        if (fling != null)
                            fling.cancelFling();
                        setState(State.DRAG);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (state == State.DRAG && touchEnable) {
                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;
                            float fixTransX = getFixDragTrans(deltaX, viewWidth, getImageWidth());
                            float fixTransY = getFixDragTrans(deltaY, viewHeight, getImageHeight());
                            matrix.postTranslate(fixTransX, fixTransY);
                            fixTrans();
                            last.set(curr.x, curr.y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        setState(State.NONE);
                        break;
                }
            }

            setImageMatrix(matrix);

            //
            // User-defined OnTouchListener
            //
            if(userTouchListener != null) {
                userTouchListener.onTouch(v, event);
            }

            //
            // OnTouchImageViewListener is set: TouchImageView dragged by user.
            //
            if (touchImageViewListener != null & touchEnable) {
                touchImageViewListener.onMove();
            }

            //
            // indicate event was handled
            //

            if(delegate!=null)
            {
                delegate.onTouchEvent(event);
            }

            savePreviousImageValues();
            return true;
        }
    }

    /**
     * ScaleListener detects user two finger scaling and scales m_thumbData.
     * @author Ortiz
     *
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            setState(State.ZOOM);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if(touchEnable)
                scaleImage(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY(), true);

            //
            // OnTouchImageViewListener is set: TouchImageView pinch zoomed by user.
            //
            if (touchImageViewListener != null && touchEnable) {
                touchImageViewListener.onMove();
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            setState(State.NONE);
            boolean animateToZoomBoundary = false;
            float targetZoom = normalizedScale;
            if (normalizedScale > maxScale) {
                targetZoom = maxScale;
                animateToZoomBoundary = true;

            } else if (normalizedScale < minScale) {
                targetZoom = minScale;
                animateToZoomBoundary = true;
            }

            if (animateToZoomBoundary) {
                DoubleTapZoom doubleTap = new DoubleTapZoom(targetZoom, viewWidth / 2, viewHeight / 2, true);
                compatPostOnAnimation(doubleTap);
            }


        }
    }

    private void scaleImage(double deltaScale, float focusX, float focusY, boolean stretchImageToSuper) {

        float lowerScale, upperScale;
        if (stretchImageToSuper) {
            lowerScale = superMinScale;
            upperScale = superMaxScale;

        } else {
            lowerScale = minScale;
            upperScale = maxScale;
        }

        float origScale = normalizedScale;
        normalizedScale *= deltaScale;
        if (normalizedScale > upperScale) {
            normalizedScale = upperScale;
            deltaScale = upperScale / origScale;
        } else if (normalizedScale < lowerScale) {
            normalizedScale = lowerScale;
            deltaScale = lowerScale / origScale;
        }



        matrix.postScale((float) deltaScale, (float) deltaScale, focusX, focusY);
        fixScaleTrans();
    }

    /**
     * DoubleTapZoom calls a series of runnables which apply
     * an animated zoom in/out graphic to the m_thumbData.
     * @author Ortiz
     *
     */
    private class DoubleTapZoom implements Runnable {

        private long startTime;
        private static final float ZOOM_TIME = 500;
        private float startZoom, targetZoom;
        private float bitmapX, bitmapY;
        private boolean stretchImageToSuper;
        private AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        private PointF startTouch;
        private PointF endTouch;

        DoubleTapZoom(float targetZoom, float focusX, float focusY, boolean stretchImageToSuper) {
            setState(State.ANIMATE_ZOOM);
            startTime = System.currentTimeMillis();
            this.startZoom = normalizedScale;
            this.targetZoom = targetZoom;
            this.stretchImageToSuper = stretchImageToSuper;
            PointF bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false);
            this.bitmapX = bitmapPoint.x;
            this.bitmapY = bitmapPoint.y;

            //
            // Used for translating m_thumbData during scaling
            //
            startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY);
            endTouch = new PointF(viewWidth / 2, viewHeight / 2);
        }

        @Override
        public void run() {
            float t = interpolate();
            double deltaScale = calculateDeltaScale(t);
            scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper);
            translateImageToCenterTouchPosition(t);
            fixScaleTrans();
            setImageMatrix(matrix);

            //
            // OnTouchImageViewListener is set: double tap runnable updates listener
            // with every frame.
            //
            if (touchImageViewListener != null && touchEnable) {
                touchImageViewListener.onMove();
            }

            if (t < 1f) {
                //
                // We haven't finished zooming
                //
                compatPostOnAnimation(this);

            } else {
                //
                // Finished zooming
                //
                setState(State.NONE);
            }
        }

        /**
         * Interpolate between where the m_thumbData should start and end in order to translate
         * the m_thumbData so that the point that is touched is what ends up centered at the end
         * of the zoom.
         * @param t
         */
        private void translateImageToCenterTouchPosition(float t) {
            float targetX = startTouch.x + t * (endTouch.x - startTouch.x);
            float targetY = startTouch.y + t * (endTouch.y - startTouch.y);
            PointF curr = transformCoordBitmapToTouch(bitmapX, bitmapY);
            matrix.postTranslate(targetX - curr.x, targetY - curr.y);
        }

        /**
         * Use interpolator to get t
         * @return
         */
        private float interpolate() {
            long currTime = System.currentTimeMillis();
            float elapsed = (currTime - startTime) / ZOOM_TIME;
            elapsed = Math.min(1f, elapsed);
            return interpolator.getInterpolation(elapsed);
        }

        /**
         * Interpolate the current targeted zoom and get the delta
         * from the current zoom.
         * @param t
         * @return
         */
        private double calculateDeltaScale(float t) {
            double zoom = startZoom + t * (targetZoom - startZoom);
            return zoom / normalizedScale;
        }
    }

    /**
     * This function will transform the coordinates in the touch event to the coordinate
     * system of the drawable that the imageview contain
     * @param x x-coordinate of touch event
     * @param y y-coordinate of touch event
     * @param clipToBitmap Touch event may occur within ja.view, but outside m_thumbData content. True, to clip return value
     * 			to the bounds of the bitmap size.
     * @return Coordinates of the point touched, in the coordinate system of the original drawable.
     */
    private PointF transformCoordTouchToBitmap(float x, float y, boolean clipToBitmap) {
        matrix.getValues(m);
        float origW = getDrawable().getIntrinsicWidth();
        float origH = getDrawable().getIntrinsicHeight();
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];
        float finalX = ((x - transX) * origW) / getImageWidth();
        float finalY = ((y - transY) * origH) / getImageHeight();

        if (clipToBitmap) {
            finalX = Math.min(Math.max(finalX, 0), origW);
            finalY = Math.min(Math.max(finalY, 0), origH);
        }

        return new PointF(finalX , finalY);
    }

    /**
     * Inverse of transformCoordTouchToBitmap. This function will transform the coordinates in the
     * drawable's coordinate system to the ja.view's coordinate system.
     * @param bx x-coordinate in original bitmap coordinate system
     * @param by y-coordinate in original bitmap coordinate system
     * @return Coordinates of the point in the ja.view's coordinate system.
     */
    private PointF transformCoordBitmapToTouch(float bx, float by) {
        matrix.getValues(m);
        float origW = getDrawable().getIntrinsicWidth();
        float origH = getDrawable().getIntrinsicHeight();
        float px = bx / origW;
        float py = by / origH;
        float finalX = m[Matrix.MTRANS_X] + getImageWidth() * px;
        float finalY = m[Matrix.MTRANS_Y] + getImageHeight() * py;
        return new PointF(finalX , finalY);
    }

    /**
     * Fling launches sequential runnables which apply
     * the fling graphic to the m_thumbData. The values for the translation
     * are interpolated by the Scroller.
     * @author Ortiz
     *
     */
    private class Fling implements Runnable {

        CompatScroller scroller;
        int currX, currY;

        Fling(int velocityX, int velocityY) {
            setState(State.FLING);
            scroller = new CompatScroller(context);
            matrix.getValues(m);

            int startX = (int) m[Matrix.MTRANS_X];
            int startY = (int) m[Matrix.MTRANS_Y];
            int minX, maxX, minY, maxY;

            if (getImageWidth() > viewWidth) {
                minX = viewWidth - (int) getImageWidth();
                maxX = 0;

            } else {
                minX = maxX = startX;
            }

            if (getImageHeight() > viewHeight) {
                minY = viewHeight - (int) getImageHeight();
                maxY = 0;

            } else {
                minY = maxY = startY;
            }

            scroller.fling(startX, startY, (int) velocityX, (int) velocityY, minX,
                    maxX, minY, maxY);
            currX = startX;
            currY = startY;
        }

        public void cancelFling() {
            if (scroller != null) {
                setState(State.NONE);
                scroller.forceFinished(true);
            }
        }

        @Override
        public void run() {

            //
            // OnTouchImageViewListener is set: TouchImageView listener has been flung by user.
            // Listener runnable updated with each frame of fling animation.
            //
            if (touchImageViewListener != null && touchEnable) {
                touchImageViewListener.onMove();
            }

            if (scroller.isFinished()) {
                scroller = null;
                return;
            }

            if (scroller.computeScrollOffset()) {
                int newX = scroller.getCurrX();
                int newY = scroller.getCurrY();
                int transX = newX - currX;
                int transY = newY - currY;
                currX = newX;
                currY = newY;
                matrix.postTranslate(transX, transY);
                fixTrans();
                setImageMatrix(matrix);

                compatPostOnAnimation(this);

            }
        }
    }


    private class CompatScroller {
        Scroller scroller;
        OverScroller overScroller;
        boolean isPreGingerbread;

        public CompatScroller(Context context) {
            if (VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) {
                isPreGingerbread = true;
                scroller = new Scroller(context);

            } else {
                isPreGingerbread = false;
                overScroller = new OverScroller(context);
            }
        }

        public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
            if (isPreGingerbread) {
                scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            } else {
                overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            }
        }

        public void forceFinished(boolean finished) {
            if (isPreGingerbread) {
                scroller.forceFinished(finished);
            } else {
                overScroller.forceFinished(finished);
            }
        }

        public boolean isFinished() {
            if (isPreGingerbread) {
                return scroller.isFinished();
            } else {
                return overScroller.isFinished();
            }
        }

        public boolean computeScrollOffset() {
            if (isPreGingerbread) {
                return scroller.computeScrollOffset();
            } else {
                overScroller.computeScrollOffset();
                return overScroller.computeScrollOffset();
            }
        }

        public int getCurrX() {
            if (isPreGingerbread) {
                return scroller.getCurrX();
            } else {
                return overScroller.getCurrX();
            }
        }

        public int getCurrY() {
            if (isPreGingerbread) {
                return scroller.getCurrY();
            } else {
                return overScroller.getCurrY();
            }
        }
    }

    // @TargetApi(Build.VERSION_CODES.)
    private void compatPostOnAnimation(Runnable runnable) {
//    	if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
//            postOnAnimation(runnable);
//
//        } else {
        postDelayed(runnable, 10);
        savePreviousImageValues();
        //   }
    }

    private class ZoomVariables {
        public float scale;
        public float focusX;
        public float focusY;
        public ScaleType scaleType;

        public ZoomVariables(float scale, float focusX, float focusY, ScaleType scaleType) {
            this.scale = scale;
            this.focusX = focusX;
            this.focusY = focusY;
            this.scaleType = scaleType;
        }
    }

    private void printMatrixInfo() {
        float[] n = new float[9];
        matrix.getValues(n);
        Log.d(DEBUG, "Scale: " + n[Matrix.MSCALE_X] + " TransX: " + n[Matrix.MTRANS_X] + " TransY: " + n[Matrix.MTRANS_Y]);
    }
//
//    public void setMemoEntry(ArrayList<EntryMemo> entry) {
//        this.memoEntry = entry;
//    }
}