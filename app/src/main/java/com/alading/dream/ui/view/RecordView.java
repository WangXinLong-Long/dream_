package com.alading.dream.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alading.dream.R;
import com.alading.libcommon.utils.PixUtils;

import java.util.logging.LogRecord;

public class RecordView extends View implements View.OnClickListener, View.OnLongClickListener {
    private static final int PROGRESS_INTERVAL = 100;
    private int radius;
    private int maxDuration;
    private int progressWidth;
    private int fillColor;
    private int progressColor;
    private int progressMaxValue;
    private Paint fillPaint;
    private Paint progressPaint;
    private float progressValue;
    private boolean isRecording;
    private long startRecordTime;

    public RecordView(Context context) {
        this(context, null);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView, defStyleAttr, defStyleRes);

        radius = typedArray.getDimensionPixelOffset(R.styleable.RecordView_radius, 0);
        maxDuration = typedArray.getDimensionPixelOffset(R.styleable.RecordView_duration, 10);
        progressWidth = typedArray.getDimensionPixelOffset(R.styleable.RecordView_progress_width, PixUtils.dp2px(3));
        fillColor = typedArray.getColor(R.styleable.RecordView_fill_color, Color.WHITE);
        progressColor = typedArray.getColor(R.styleable.RecordView_progress_color, Color.RED);
        setMaxDuration(maxDuration);
        typedArray.recycle();

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(fillColor);
        fillPaint.setStyle(Paint.Style.FILL);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressWidth);
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                progressValue++;
                postInvalidate();
                if (progressValue <= progressMaxValue) {
                    sendEmptyMessageDelayed(0, PROGRESS_INTERVAL);
                } else {
                    finishRecord();
                }
            }
        };
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isRecording = true;
                    startRecordTime = System.currentTimeMillis();
                    handler.sendEmptyMessage(0);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    long now = System.currentTimeMillis();
                    if (now - startRecordTime > ViewConfiguration.getLongPressTimeout()) {
                        finishRecord();
                    }
                    handler.removeCallbacksAndMessages(null);
                    isRecording = false;
                    startRecordTime = 0;
                    progressValue = 0;

                    postInvalidate();
                }
                return false;
            }
        });

        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    private void finishRecord() {
        if (onRecordListener != null) {
            onRecordListener.onFinish();
        }
    }

    private void setMaxDuration(int maxDuration) {
        progressMaxValue = maxDuration * 1000 / PROGRESS_INTERVAL;
    }

    OnRecordListener onRecordListener;

    public void setOnRecordListener(OnRecordListener onRecordListener) {
        this.onRecordListener = onRecordListener;
    }

    @Override
    public void onClick(View v) {
        if (onRecordListener != null) {
            onRecordListener.onClick();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (onRecordListener != null) {
            onRecordListener.onLongClick();
        }
        return true;
    }

    public interface OnRecordListener {
        void onClick();

        void onLongClick();

        void onFinish();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (isRecording) {
            canvas.drawCircle(width / 2, height / 2, width / 2, fillPaint);
            int left = 0;
            int top = 0;
            int right = width;
            int bottom = height;
            float sweepAngle = progressValue / progressMaxValue * 360;
            canvas.drawArc(left, top, right, bottom, -90, sweepAngle, false, progressPaint);
        } else {

            canvas.drawCircle(width / 2, height / 2, radius, fillPaint);
        }
    }
}
