package com.wanwan.customview.View.WaveView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.wanwan.customview.R;

import java.util.Random;


/**
 * @author wanwan
 * @email wanwan2017@foxmail.com
 * @date 2017/12/29
 * @desc 方向波浪View
 */
public class WaveView extends View {

    /**
     * 进度
     */
    private int mProgress;
    /**
     * 幅度
     */
    private int mRange;

    /**
     * 一屏显示多少个完整的正弦波
     */
    private float mWaveMultiple;
    /**
     * 当前视图的最大高度
     */
    private int mMaxHeight;

    /**
     * 正弦波最大高度
     */
    private float mWaveMaxHeight;

    /**
     * 上层正弦波滚动速度0-100
     */
    private int mAboveSpeed;

    /**
     * 下层正弦波滚动速度0-100
     */
    private int mBlowSpeed;

    /**
     * 两个正弦波的初始偏移量0-100
     */
    private int mOffset;

    /**
     * 上层波浪线视图填充色
     */
    private int mAboveWaveColor;
    /**
     * 下层波浪线视图填充色
     */
    private int mBlowWaveColor;
    /**
     * 上层波浪线视图边界色
     */
    private int mAboveWaveStokeColor;
    /**
     * 下层波浪线视图边界色
     */
    private int mBlowWaveStokeColor;

    /**
     * 上层波浪形视图路径
     */
    private Path mAboveWavePath = new Path();
    /**
     * 下层波浪形视图路径
     */
    private Path mBlowWavePath = new Path();
    /**
     * 上层波浪形视图边界路径
     */
    private Path mAboveStokePath = new Path();
    /**
     * 下层波浪形视图边界路径
     */
    private Path mBlowStokePath = new Path();


    /**
     * 上层波浪形视图画笔
     */
    private Paint mAboveWavePaint = new Paint();
    /**
     * 下层层波浪形视图画笔
     */
    private Paint mBlowWavePaint = new Paint();
    /**
     * 上层波浪形视图边界画笔
     */
    private Paint mAboveStokePaint = new Paint();
    /**
     * 下层波浪形视图边界画笔
     */
    private Paint mBlowStokePaint = new Paint();

    public final int DEFAULT_ABOVE_WAVE_ALPHA = 255;
    public final int DEFAULT_BLOW_WAVE_ALPHA = 30;

    private final int DEFAULT_ABOVE_WAVE_COLOR = Color.WHITE;
    private final int DEFAULT_BLOW_WAVE_COLOR = Color.BLUE;
    private final int DEFAULT_PROGRESS = 50;
    private final int DEFAULT_RANGE = 50;
    private final int DEFAULT_WAVE_MULTIPLE = 2;
    private final int DEFAULT_ABOVE_SPEED = 20;
    private final int DEFAULT_BLOW_SPEED = 25;
    private final int DEFAULT_OFFSET = 25;

    private final float X_SPACE = 20;
    private final double PI2 = 2 * Math.PI;

    private float mAboveOffset = 0.0f;
    private float mBlowOffset = 0.0f;

    private int left, right, bottom;
    private float mMaxRight;
    private double omega;

    private RefreshProgressRunnable mRefreshProgressRunnable;

    public void setProgress(int progress) {
        this.mProgress = progress > 100 ? 100 : progress;
    }

    public void setRange(int range) {
        this.mRange = range > 100 ? 100 : range;
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (View.GONE == visibility) {
            removeCallbacks(mRefreshProgressRunnable);
        } else {
            removeCallbacks(mRefreshProgressRunnable);
            mRefreshProgressRunnable = new RefreshProgressRunnable();
            post(mRefreshProgressRunnable);
        }
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //load styled attributes.
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaveView, R.attr.waveViewStyle, 0);
        mProgress = attributes.getInt(R.styleable.WaveView_progress, DEFAULT_PROGRESS);
        mRange = attributes.getInt(R.styleable.WaveView_range, DEFAULT_RANGE);
        mWaveMultiple = attributes.getFloat(R.styleable.WaveView_wave_multiple, DEFAULT_WAVE_MULTIPLE);
        mWaveMaxHeight = attributes.getDimension(R.styleable.WaveView_wave_max_height, 0f);
        mAboveSpeed = attributes.getInt(R.styleable.WaveView_above_speed, DEFAULT_ABOVE_SPEED);
        mBlowSpeed = attributes.getInt(R.styleable.WaveView_blow_speed, DEFAULT_BLOW_SPEED);
        mOffset = attributes.getInt(R.styleable.WaveView_offset, DEFAULT_OFFSET);
        mAboveWaveColor = attributes.getColor(R.styleable.WaveView_above_wave_color, DEFAULT_ABOVE_WAVE_COLOR);
        mBlowWaveColor = attributes.getColor(R.styleable.WaveView_blow_wave_color, DEFAULT_BLOW_WAVE_COLOR);
        mAboveWaveStokeColor = attributes.getColor(R.styleable.WaveView_above_wave_stoke_color, DEFAULT_ABOVE_WAVE_COLOR);
        mBlowWaveStokeColor = attributes.getColor(R.styleable.WaveView_blow_wave_stoke_color, DEFAULT_BLOW_WAVE_COLOR);
        attributes.recycle();

        //初始化画笔
        initializePainters();

        setProgress(mProgress);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mMaxRight < 0.1f) {
            startWave();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(mBlowWavePath, mBlowWavePaint);
        canvas.drawPath(mAboveWavePath, mAboveWavePaint);
        canvas.drawPath(mAboveStokePath, mAboveStokePaint);
        canvas.drawPath(mBlowStokePath, mBlowStokePaint);
    }

    /**
     * 初始化画笔
     */
    public void initializePainters() {
        mAboveWavePaint.setColor(mAboveWaveColor);
        mAboveWavePaint.setAlpha(DEFAULT_ABOVE_WAVE_ALPHA);
        mAboveWavePaint.setStyle(Paint.Style.FILL);
        mAboveWavePaint.setAntiAlias(true);

        mBlowWavePaint.setColor(mBlowWaveColor);
        mBlowWavePaint.setAlpha(DEFAULT_BLOW_WAVE_ALPHA);
        mBlowWavePaint.setStyle(Paint.Style.FILL);
        mBlowWavePaint.setAntiAlias(true);

        mAboveStokePaint.setAntiAlias(true);            //设置画笔为无锯齿
        mAboveStokePaint.setColor(mBlowWaveColor);       //白色背景
        mAboveStokePaint.setAlpha(200);
        mAboveStokePaint.setStrokeWidth((float) 3.0);     //线宽
        mAboveStokePaint.setStyle(Paint.Style.STROKE);

        mBlowStokePaint.setAntiAlias(true);            //设置画笔为无锯齿
        mBlowStokePaint.setColor(mBlowWaveColor);       //白色背景
        mBlowStokePaint.setAlpha(100);
        mBlowStokePaint.setStrokeWidth((float) 3.0);     //线宽
        mBlowStokePaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * 设置初始值
     */
    private void startWave() {
        if (mMaxRight < 0.1) {
            int width = getWidth();
            left = 0;
            right = getRight();
            bottom = getBottom() + 2;
            mMaxRight = width + X_SPACE;
            omega = mWaveMultiple * PI2 / width;
            mBlowOffset=(float) (PI2 * mOffset / 100);
        }
    }

    /**
     * calculate wave track
     */
    private void calculatePath() {
        mAboveWavePath.reset();
        mBlowWavePath.reset();
        mAboveStokePath.reset();
        mBlowStokePath.reset();

        getWaveOffset();


        float y1, y2;
        mAboveWavePath.moveTo(left, bottom);
        mBlowWavePath.moveTo(left, bottom);

        int rangeHeight = (int) mWaveMaxHeight * mRange / 100;
        int baseHeight = (int) (getHeight()-(getHeight() - 2 * mWaveMaxHeight) * mProgress / 100 - mWaveMaxHeight);
        for (float x = left; x <= mMaxRight; x += X_SPACE) {
            y1 = (float) (rangeHeight * Math.sin(omega * x + mAboveOffset) + baseHeight);
            y2 = (float) (rangeHeight * Math.sin(omega * x + mBlowOffset) + baseHeight);
            mAboveWavePath.lineTo(x, y1 > y2 ? y1 : y2);
            mBlowWavePath.lineTo(x, y1 > y2 ? y2 : y1);
            if (x == left) {
                mAboveStokePath.moveTo(x, y1);
                mBlowStokePath.moveTo(x, y2);
            } else {
                mAboveStokePath.lineTo(x, y1);
                mBlowStokePath.lineTo(x, y2);
            }

        }
        mAboveWavePath.lineTo(right, bottom);
        mBlowWavePath.lineTo(right, bottom);
    }

    private void getWaveOffset() {
        Random random = new Random(1);
        int num = Math.abs(random.nextInt()%6);
        if (mBlowOffset > Float.MAX_VALUE - 100) {
            mBlowOffset = 0;
        } else {
            mBlowOffset += (float)(PI2 * (mBlowSpeed+num) / 360 );
        }

        if (mAboveOffset > Float.MAX_VALUE - 100) {
            mAboveOffset = 0;
        } else {
            mAboveOffset +=(float)(PI2 * (mAboveSpeed-num) / 360) ;
        }
    }

    private class RefreshProgressRunnable implements Runnable {
        public void run() {
            synchronized (WaveView.this) {
                long start = System.currentTimeMillis();

                calculatePath();

                invalidate();

                long gap = 16 - (System.currentTimeMillis() - start);
                postDelayed(this, gap < 0 ? 0 : gap);
            }
        }
    }


    @Override
    public Parcelable onSaveInstanceState() {
        // Force our ancestor class to save its state
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.progress = mProgress;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setProgress(ss.progress);
        setRange(ss.range);
    }


    private static class SavedState extends BaseSavedState {
        int progress;
        int range;

        /**
         * Constructor called from {@link android.widget.ProgressBar#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            progress = in.readInt();
            range = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(progress);
            out.writeInt(range);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
