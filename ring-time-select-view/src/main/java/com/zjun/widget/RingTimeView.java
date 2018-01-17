package com.zjun.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * RingTimeView
 *  圆形时间选择器
 *
 * @author Ralap
 * @description 用于选择多段0~60min之间的时间。
 *          1、新增：点击空白处（最大数量由{@link #sectionSum}决定）
 *          2、修改：拖动时间段的起始/终止位置；或开启快速截取功能{@link #quickCutEnable}，直接点击时间段中间进行修改
 *          3、删除：拖动时间段，让起始位置和终止位置一样，则删除
 *          4、回调：新增、拖动修改、拖动删除时，都有回调，当然首先你得设置添加回调接口IOnTimeChangedListener
 *
 * @date 2018-01-12
 * @version
 *          v2: 2018-01-17
 *              1. 增加属性{@link #startMinute}和{@link #endMinute}，为了布局中能自定义起止时间，查看效果
 *              2. 把{@link TimePart}从{@link IOnTimeChangedListener}里提取出来
 *              3. 增加获取当前所有时间段的方法{@link #getTimeSections()}
 *              4. 修复Bug：在{@link #onTouchEvent(MotionEvent)}的MotionEvent.ACTION_DOWN中添加时，未判断界限，导致NullPointerException
 *          v1
 */
public class RingTimeView extends View {
    private static final String TAG = "RingTimeView";

    private static final int GRAVITY_TOP                = 0b00000001;
    private static final int GRAVITY_BOTTOM             = 0b00000010;
    private static final int GRAVITY_CENTER_VERTICAL    = 0b00000011;
    private static final int GRAVITY_LEFT               = 0b00000100;
    private static final int GRAVITY_RIGHT              = 0b00001000;
    private static final int GRAVITY_CENTER_HORIZONTAL  = 0b00001100;

    @IntDef({GRAVITY_TOP, GRAVITY_BOTTOM, GRAVITY_LEFT, GRAVITY_RIGHT, GRAVITY_CENTER_VERTICAL, GRAVITY_CENTER_HORIZONTAL})
    private @interface Gravity{
    }

    /**
     * 弧度制
     */
    private static final double RADIAN      = 180 / Math.PI;

    /**
     * 最小、最大分钟值
     */
    private static final int MIN_MINUTE     = 0;
    private static final int MAX_MINUTE     = 60;

    /**
     * 平滑过渡值，一个滑动误差范围：防止跳跃现象
     */
    private static final int SMOOTH_RANGE_VALUE = 5;

    /**
     * 初始化时间的起始分钟，范围∈[0, 60]
     * 在布局中设置此属性和{@link #endMinute}，能查看时间段和锚点的效果
     */
    private int startMinute;
    /**
     * 初始化时间的终点分钟，范围∈[0, 60]
     * 在布局中设置此属性和{@link #startMinute}，能查看时间段和锚点的效果
     */
    private int endMinute;

    /**
     * 重力
     * 四边，水平垂直居中
     *
     * @see Gravity
     */
    private int gravity;
    /**
     * 创建时间段时，默认的时间段值
     */
    private int initialMinutes;
    /**
     * 圆环宽度
     */
    private float ringWidth;

    /**
     * 背景圆颜色
     */
    private int ringBgColor;

    /**
     * 时间段的最大个数
     */
    private int sectionSum;
    /**
     * 快速截取功能：点击已选时间段的圆环内，把截止时间修改到当前时间点
     */
    private boolean quickCutEnable;
    /**
     * 已选时间的圆环颜色
     */
    private int sectionColor;
    /**
     * 已选时间的圆环颜色2（若设置，则将使用扫描渲染渐变色。默认不设置，值为-1）
     */
    private int sectionColor2;
    /**
     * 已选时间的圆环颜色3（只有在sectionColor2有效的情况下，才有效）
     */
    private int sectionColor3;
    /**
     * 锚点直径
     */
    private float anchorDiameter;
    /**
     * 锚点背景边框宽度
     */
    private float anchorStrokeWidth;
    /**
     * 锚点文字大小
     */
    private float anchorTextSize;
    /**
     * 起始锚点背景颜色
     */
    private int anchorStartColor;
    /**
     * 起始锚点背景边框颜色
     */
    private int anchorStartStrokeColor;
    /**
     * 起始锚点文字
     */
    private String anchorStartText;
    /**
     * 起始锚点文字颜色
     */
    private int anchorStartTextColor;

    /**
     * 终止锚点背景颜色
     */
    private int anchorEndColor;
    /**
     * 终止锚点背景边框颜色
     */
    private int anchorEndStrokeColor;
    /**
     * 终止锚点文字
     */
    private String anchorEndText;
    /**
     * 终止锚点文字颜色
     */
    private int anchorEndTextColor;

    /**
     * 如果重叠，是否需要合并字体
     */
    private boolean anchorNeedMerge;


    /**
     * 分钟刻度颜色
     */
    private int degreeColor;
    /**
     * 刻度长指针长度
     */
    private float degreeLongLength;
    /**
     * 刻度长指针宽度
     */
    private float degreeLongWidth;
    /**
     * 刻度短指针长度
     */
    private float degreeShortLength;
    /**
     * 刻度短指针宽度
     */
    private float degreeShortWidth;
    /**
     * 刻度数字大小
     */
    private float numberSize;
    /**
     * 刻度数字颜色
     */
    private int numberColor;


    /**
     * 圆环半径
     */
    private float mRingRadius;
    /**
     * 锚点半径
     */
    private float mAnchorRadius;

    /**
     * 圆环外圆的范围（= 外圆半径²）
     */
    private float mOuterCircleRange;
    /**
     * 圆环内圆的范围（= 内圆半径²）
     */
    private float mInterCircleRange;
    /**
     * 锚点圆范围（= 锚点半径²）
     */
    private float mAnchorCircleRange;


    /**
     * 圆环画笔
     */
    private Paint mRingPaint;
    /**
     * 时间段圆环画笔
     */
    private Paint mSectionPaint;
    /**
     * 刻度画笔
     */
    private Paint mDegreePaint;
    /**
     * 刻度数值画笔
     */
    private Paint mNumberPaint;
    /**
     * 锚点画笔
     */
    private Paint mAnchorPaint;
    /**
     * 锚点文字画笔
     */
    private Paint mTextPaint;

    /**
     * 时间段集合
     */
    private TimeSection[] mTimeSections;

    /**
     * 监听回调
     */
    private IOnTimeChangedListener mListener;

    public RingTimeView(Context context) {
        this(context, null);
    }

    public RingTimeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RingTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(attrs, defStyleAttr);
        initPaints();
    }

    /**
     * 初始化属性
     */
    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.RingTimeView, defStyle, 0);
        startMinute = ta.getInt(R.styleable.RingTimeView_rtv_startMinute, -1);
        endMinute = ta.getInt(R.styleable.RingTimeView_rtv_endMinute, -1);
        gravity = ta.getInt(R.styleable.RingTimeView_rtv_gravity, GRAVITY_LEFT | GRAVITY_TOP);
        initialMinutes = ta.getInt(R.styleable.RingTimeView_rtv_initialMinutes, 5);

        ringWidth = ta.getDimension(R.styleable.RingTimeView_rtv_ringWidth, dp2px(30));
        ringBgColor = ta.getColor(R.styleable.RingTimeView_rtv_ringBgColor, Color.parseColor("#a7a7a7"));

        sectionSum = ta.getInt(R.styleable.RingTimeView_rtv_sectionSum, 3);
        quickCutEnable = ta.getBoolean(R.styleable.RingTimeView_rtv_quickCutEnable, false);
        sectionColor = ta.getColor(R.styleable.RingTimeView_rtv_sectionColor, Color.parseColor("#148c75"));
        sectionColor2 = ta.getColor(R.styleable.RingTimeView_rtv_sectionColor2, -1);
        sectionColor3 = ta.getColor(R.styleable.RingTimeView_rtv_sectionColor3, -1);

        anchorDiameter = ta.getDimension(R.styleable.RingTimeView_rtv_anchorDiameter, dp2px(50));
        anchorStrokeWidth = ta.getDimension(R.styleable.RingTimeView_rtv_anchorStrokeWidth, dp2px(6));
        anchorTextSize = ta.getDimension(R.styleable.RingTimeView_rtv_anchorTextSize, sp2px(16));
        anchorNeedMerge = ta.getBoolean(R.styleable.RingTimeView_rtv_anchorNeedMerge, true);

        anchorStartColor = ta.getColor(R.styleable.RingTimeView_rtv_anchorStartColor, Color.parseColor("#007ffe"));
        anchorStartStrokeColor = ta.getColor(R.styleable.RingTimeView_rtv_anchorStartStrokeColor, Color.parseColor("#FFFFFF"));
        anchorStartText = ta.getString(R.styleable.RingTimeView_rtv_anchorStartText);
        anchorStartTextColor = ta.getColor(R.styleable.RingTimeView_rtv_anchorStartTextColor, Color.parseColor("#FFFFFF"));

        anchorEndColor = ta.getColor(R.styleable.RingTimeView_rtv_anchorEndColor, anchorStartColor);
        anchorEndStrokeColor = ta.getColor(R.styleable.RingTimeView_rtv_anchorEndStrokeColor, anchorStartStrokeColor);
        anchorEndText = ta.getString(R.styleable.RingTimeView_rtv_anchorEndText);
        anchorEndTextColor = ta.getColor(R.styleable.RingTimeView_rtv_anchorEndTextColor, anchorStartTextColor);

        degreeColor = ta.getColor(R.styleable.RingTimeView_rtv_degreeColor, Color.parseColor("#888888"));
        degreeLongLength = ta.getDimension(R.styleable.RingTimeView_rtv_degreeLongLength, -1);
        degreeLongWidth = ta.getDimension(R.styleable.RingTimeView_rtv_degreeLongWidth, dp2px(2));
        degreeShortLength = ta.getDimension(R.styleable.RingTimeView_rtv_degreeShortLength, -1);
        degreeShortWidth = ta.getDimension(R.styleable.RingTimeView_rtv_degreeShortWidth, degreeLongWidth);

        numberSize = ta.getDimension(R.styleable.RingTimeView_rtv_numberSize, sp2px(14));
        numberColor = ta.getColor(R.styleable.RingTimeView_rtv_numberColor, Color.parseColor("#888888"));
        ta.recycle();

        /*
        检验，并设置其他相关变量
         */
        mAnchorRadius = anchorDiameter * .5f;
        mAnchorCircleRange = mAnchorRadius * mAnchorRadius;
        if (anchorStartText == null) {
            anchorStartText = "ON";
        }
        if (anchorEndText == null) {
            anchorEndText = "OFF";
        }


        if (startMinute < -1 || startMinute > MAX_MINUTE) {
            throw new IllegalArgumentException("The value of startMinute should between 0 and 60");
        }
        if (endMinute < -1 || endMinute > MAX_MINUTE) {
            throw new IllegalArgumentException("The value of endMinute should between 0 and 60");
        }
        if (startMinute > endMinute) {
            throw new IllegalArgumentException("The endMinute must be larger than startMinute");
        }

        if (sectionSum < 1 || sectionSum >= MAX_MINUTE) {
            throw new IllegalArgumentException("The value of sectionSum must between 1 and 60");
        }

        /*
         初始化属性中的时间段
         */
        mTimeSections = new TimeSection[sectionSum];
        if (startMinute != -1 && endMinute != -1) {
            TimeSection.TimeAnchor start = new TimeSection.TimeAnchor();
            TimeSection.TimeAnchor end = new TimeSection.TimeAnchor();
            start.minute = startMinute;
            end.minute = endMinute;

            TimeSection section = new TimeSection();
            section.start = start;
            section.end = end;
            mTimeSections[0] = section;
        }
    }

    /**
     * 初始化画笔，和与画笔相关的值
     */
    private void initPaints() {
        // 圆环画笔
        mRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRingPaint.setAntiAlias(true);
        mRingPaint.setColor(ringBgColor);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth(ringWidth);

        // 时间段的圆环画笔
        mSectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSectionPaint.setAntiAlias(true);
        mSectionPaint.setStyle(Paint.Style.STROKE);
        mSectionPaint.setStrokeWidth(ringWidth);
        if (sectionColor2 != -1) {
            // 扫描渲染着色器
            Shader shader;
            if (sectionColor3 != -1) {
                shader = new SweepGradient(centerX, centerY, new int[]{sectionColor, sectionColor2, sectionColor3, sectionColor}, null);
            } else {
                shader = new SweepGradient(centerX, centerY, new int[]{sectionColor, sectionColor2, sectionColor}, null);
            }
            mSectionPaint.setShader(shader);
        } else {
            mSectionPaint.setColor(sectionColor);
        }

        // 刻度画笔
        mDegreePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDegreePaint.setAntiAlias(true);
        mDegreePaint.setColor(degreeColor);
        mDegreePaint.setStyle(Paint.Style.STROKE);

        // 刻度数值画笔
        mNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNumberPaint.setAntiAlias(true);
        mNumberPaint.setColor(numberColor);
        mNumberPaint.setStyle(Paint.Style.FILL);
        mNumberPaint.setTextSize(numberSize);
        Paint.FontMetricsInt fontMetrics = mNumberPaint.getFontMetricsInt();
        numberHalfHeight = (fontMetrics.top - fontMetrics.bottom) * .5f - fontMetrics.top;

        // 锚点画笔
        mAnchorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnchorPaint.setAntiAlias(true);
        mAnchorPaint.setStyle(Paint.Style.FILL);

        // 锚点文字画笔
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(anchorTextSize);

        /*
         锚点文字绘制起始点，相对于文字中心点的偏移量
         中英文字符宽度不同，但高度一样
          */
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(anchorStartText, 0, anchorStartText.length(), bounds);
        mStartTextOffsetX = bounds.width() * .5f;

        mTextPaint.getTextBounds(anchorEndText, 0, anchorEndText.length(), bounds);
        mEndTextOffsetX = bounds.width() * .5f;

        fontMetrics = mTextPaint.getFontMetricsInt();
        mTextOffsetY = (fontMetrics.top - fontMetrics.bottom) * .5f - fontMetrics.top;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        logD("onMeasure>>>width: mode=0x%X, size=%d; height: mode=0x%X, size=%d"
                , widthMode, widthSize, heightMode, heightSize);

        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingStart, paddingEnd;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            paddingStart = getPaddingStart();
            paddingEnd = getPaddingEnd();
        } else {
            paddingStart = getPaddingLeft();
            paddingEnd = getPaddingRight();
        }
        logD("onMeasure>>>paddingStart=%d, paddingEnd=%d, paddingTop=%d, paddingBottom=%d"
                , paddingStart,  paddingEnd, paddingTop, paddingBottom);

        /*
        确定控件的具体宽和高
            若是wrap_content，则默认用200dp
         */
        final int defaultSize = dp2px(200);
        int desireWidth, desireHeight;
        if (widthMode == MeasureSpec.EXACTLY) {
            desireWidth = widthSize;
        } else {
            desireWidth = defaultSize + paddingStart + paddingEnd;
            if (widthMode == MeasureSpec.AT_MOST) {
                desireWidth = Math.min(desireWidth, widthSize);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            desireHeight = heightSize;
        } else {
            desireHeight = defaultSize + paddingTop + paddingBottom;
            if (heightMode == MeasureSpec.AT_MOST) {
                desireHeight = Math.min(desireHeight, heightSize);
            }
        }
        logD("onMeasure>>>desireWidth=%d, desireHeight=%d", desireWidth, desireHeight);
        setMeasuredDimension(desireWidth, desireHeight);

        // 绘制区域（正方形）大小
        int size = Math.min(desireWidth - paddingStart - paddingEnd, desireHeight - paddingTop - paddingBottom);
        logD("onMeasure>>>size=%d", size);

        /*
        1. 确定圆环的半径
         */
        int halfSize = size >> 1;
        mRingRadius = (size - Math.max(anchorDiameter, ringWidth)) * .5f;

        /*
        2. 确定刻度长、短指针的长度
         */
        if (degreeLongLength == -1) {
            degreeLongLength = mRingRadius / 16;
        }
        if (degreeShortLength == -1) {
            degreeShortLength = degreeLongLength * .5f;
        }

        /*
        3. 确定圆环的外圆与內圆的范围
         */
        float ringHalfWidth = ringWidth * .5f;
        mOuterCircleRange = (float) Math.pow(mRingRadius + ringHalfWidth, 2);
        mInterCircleRange = (float) Math.pow(mRingRadius - ringHalfWidth, 2);
        logD("onMeasure>>>isInBlankArea: mOuterCircleRange=" + mOuterCircleRange);
        logD("onMeasure>>>isInBlankArea: mInterCircleRange=" + mInterCircleRange);

        /*
        4. 根据重力，确定中心点的位置
         */
        // 中心坐标x
        logD("onMeasure>>>gravity=%X", gravity);
        switch (gravity & GRAVITY_CENTER_HORIZONTAL) {
            case GRAVITY_LEFT:
            default:
                centerX = paddingStart + halfSize;
                break;
            case GRAVITY_RIGHT:
                centerX = desireWidth - paddingEnd - halfSize;
                break;
            case GRAVITY_CENTER_HORIZONTAL:
                centerX = paddingStart + (desireWidth - paddingStart - paddingEnd) / 2;
                break;
        }
        // 中心坐标y
        switch (gravity & GRAVITY_CENTER_VERTICAL) {
            case GRAVITY_TOP:
            default:
                centerY = paddingTop + halfSize;
                break;
            case GRAVITY_BOTTOM:
                centerY = desireHeight - paddingBottom - halfSize;
                break;
            case GRAVITY_CENTER_VERTICAL:
                centerY = paddingTop + (desireHeight - paddingTop - paddingBottom) / 2;
                break;
        }

        addInitializeSections();
    }


    /**
     * 初始化时间段的x、y值等。使用前先调用{@link #clearTimeSections()}清除或直接设置{@link #setTimeSections(List)}
     */
    private void addInitializeSections() {
        if (startMinute != -1 && endMinute != -1) {
            mTimeSections[0] = createTimeSection(startMinute, endMinute);
        }
    }

    private TimeSection createTimeSection(int startMinute, int endMinute) {
        if (startMinute < 0 || endMinute > MAX_MINUTE || startMinute >= endMinute) {
            return null;
        }
        TimeSection section = new TimeSection();
        section.start = generateAnchorByMinute(startMinute);
        section.end = generateAnchorByMinute(endMinute);

        Log.d(TAG, "createTimeSection: start.angle=" + section.start.angle
                + ", start.x=" + section.start.x
                + ", start.y=" + section.start.y);
        Log.d(TAG, "createTimeSection: end.angle=" + section.end.angle
                + ", end.x=" + section.end.x
                + ", end.y=" + section.end.y);

        return section;
    }


    /**
     * 根据角度和cos，计算坐标x
     *  sin² + cos² = 1
     *
     * @param angle     角度（0°~360°）
     * @param cos       cos值
     */
    private float calcXByCos(double angle, double cos) {
        if (angle < 180) {
            return (float) (centerX + Math.sqrt(1 - cos * cos) * mRingRadius);
        } else {
            return (float) (centerX - Math.sqrt(1 - cos * cos) * mRingRadius);
        }
    }

    /**
     * 根据角度和cos，计算坐标y
     * @param cos       cos值
     */
    private float calcYByCos(double cos) {
        return centerY + mRingRadius * (float) cos;
    }

    private RectF mSectionRectF;
    private float centerX, centerY;
    private float mStartTextOffsetX, mEndTextOffsetX, mTextOffsetY;
    private float numberHalfHeight;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSectionRectF == null) {
            // 已选圆环弧的矩形
            mSectionRectF = new RectF(centerX - mRingRadius, centerY - mRingRadius, centerX + mRingRadius, centerY + mRingRadius);
        }

        // 1. 绘制背景圆环
        canvas.drawCircle(centerX, centerY, mRingRadius, mRingPaint);

        // 2. 分针刻度
        float startY = centerY - mRingRadius + ringWidth * .5f;
        float shortStopY = startY + degreeShortLength;
        float longStopY = startY + degreeLongLength;
        for (int i = 0; i < MAX_MINUTE; i++) {
            if (i % 5 == 0) {
                // 长针
                mDegreePaint.setStrokeWidth(degreeLongWidth);
                canvas.drawLine(centerX, startY, centerX, longStopY, mDegreePaint);
            } else {
                // 短针
                mDegreePaint.setStrokeWidth(degreeShortWidth);
                canvas.drawLine(centerX, startY, centerX, shortStopY, mDegreePaint);
            }
            canvas.rotate(6, centerX, centerY);
        }

        // 3. 数字
        float oneNumberWidth = mNumberPaint.measureText("0");
        float numberCenterX = centerX;
        // 理论上，若数字贴着长刻度，应该是numberSize/2。但使用numberSize，为了增加numberSize/2的间距
        float numberCenterY = longStopY + numberSize;
        float numberRadius = centerY - numberCenterY;

        for (int i = 0; i < 12; i++) {
            float x = (float) (numberCenterX + Math.sin(Math.PI / 6 * i) * numberRadius);
            float y = (float) (numberCenterY + numberRadius - Math.cos(Math.PI / 6 * i) * numberRadius);
            x = x - oneNumberWidth;
            y = y + numberHalfHeight;
            int num = i * 5;
            String text = num < 10 ? "0" + num : String.valueOf(num);
            canvas.drawText(text, x, y, mNumberPaint);
        }

        // 4. 绘制已选圆环弧
        // 把正在滑动选择的时间段，放置在最上面绘制
        for (int i=0; i< mTimeSections.length; i++) {
            TimeSection section = mTimeSections[i];
            if (section == null || i == mSelectedSectionIndex) {
                Log.d(TAG, String.format("onDraw: will continue, i=%d, mSelectedSectionIndex=%d", i, mSelectedSectionIndex));
                continue;
            }

            Log.d(TAG, String.format("onDraw: Section_%d, start: minute=%d, angle=%f, x=%f, y=%f"
                    , i, section.start.minute, section.start.angle, section.start.x, section.start.y));
            Log.d(TAG, String.format("onDraw: Section_%d, end: minute=%d, angle=%f, x=%f, y=%f"
                    , i, section.end.minute, section.end.angle, section.end.x, section.end.y));

            drawSection(canvas, section, false);
        }

        // 5. 绘制正在滑动移动的时间段
        if (mSelectedSectionIndex != -1 && mTimeSections[mSelectedSectionIndex] != null) {
            TimeSection section = mTimeSections[mSelectedSectionIndex];
            Log.d(TAG, String.format("onDraw: Changing Section, start: minute=%d, angle=%f, x=%f, y=%f"
                    , section.start.minute, section.start.angle, section.start.x, section.start.y));
            Log.d(TAG, String.format("onDraw: Changing Section, end: minute=%d, angle=%f, x=%f, y=%f"
                    , section.end.minute, section.end.angle, section.end.x, section.end.y));
            drawSection(canvas, section, true);
        }
    }

    /**
     * 绘制时间段
     * 若是已选的时间段：需要把已选的锚点放置在最上层
     *
     * @param canvas                画板
     * @param section               时间段
     * @param isSelectedSection     是否正在滑动的时间段
     */
    private void drawSection(Canvas canvas, TimeSection section, boolean isSelectedSection) {
        // 只有不相等的情况下才绘制圆弧。否则会把圆弧绘制成一个整圆
        if (section.start.minute != section.end.minute) {
            float begin; //圆弧的起点位置
            float stop;
            if (section.start.angle > 180 && section.start.angle > section.end.angle) {
                //180  -- 360
                begin = (float) (-Math.abs(section.start.angle - 360) - 90);
                stop = (float) Math.abs(Math.abs(section.start.angle - 360) + section.end.angle);
                logD("begin=%f", begin);
                logD("stop=%f", stop);
            } else if (section.start.angle > section.end.angle) {
                begin = (float) section.start.angle - 90;
                stop = (float) (360 - (section.start.angle - section.end.angle));
            } else {
                begin = (float) section.start.angle - 90;
                stop = (float) Math.abs(section.start.angle - section.end.angle);
            }
            canvas.drawArc(mSectionRectF, begin, stop, false, mSectionPaint);
        }

        //画起始、终止锚点圆
        if (anchorNeedMerge && section.start.minute == MIN_MINUTE && section.end.minute == MAX_MINUTE) {
            drawMergeAnchor(canvas, section.start.x, section.start.y);
        } else {
            if (isSelectedSection && mIsStartAnchor) {
                drawEndAnchor(canvas, section.end);
                drawStartAnchor(canvas, section.start);
            } else {
                drawStartAnchor(canvas, section.start);
                drawEndAnchor(canvas, section.end);
            }
        }
    }

    /**
     * 绘制合并的锚点
     *  边框和背景都使用起始锚点的颜色
     *  字体都缩小一定比例，起始文字在居中上，终止文字在居中下
     *
     * @param canvas    画板
     * @param anchorX   锚点坐标X
     * @param anchorY   锚点坐标Y
     */
    private void drawMergeAnchor(Canvas canvas, float anchorX, float anchorY) {
        // 锚点边框的背景
        if (anchorStrokeWidth != 0) {
            mAnchorPaint.setColor(anchorStartStrokeColor);
            canvas.drawCircle(anchorX, anchorY, mAnchorRadius, mAnchorPaint);
        }

        // 锚点背景
        mAnchorPaint.setColor(anchorStartColor);
        canvas.drawCircle(anchorX, anchorY, mAnchorRadius - anchorStrokeWidth, mAnchorPaint);

        /*
        绘制文字
        字体缩小到原来的70%
         */
        final float scale = .7f;
        mTextPaint.setTextSize(anchorTextSize * scale);
        float offsetY = (mAnchorRadius - 2 * mTextOffsetY * scale) * .5f;

        // 锚点起始文字
        float x = anchorX - mStartTextOffsetX * scale;
        float y = anchorY - offsetY;
        mTextPaint.setColor(anchorStartTextColor);
        canvas.drawText(anchorStartText, x, y, mTextPaint);

        // 锚点终止文字
        x = anchorX - mEndTextOffsetX * scale;
        y = anchorY + offsetY + mTextOffsetY;
        mTextPaint.setColor(anchorEndTextColor);
        canvas.drawText(anchorEndText, x, y, mTextPaint);

        // 还原字体大小
        mTextPaint.setTextSize(anchorTextSize);
    }

    private void drawStartAnchor(Canvas canvas, TimeSection.TimeAnchor anchor) {
        drawAnchor(canvas, anchor, anchorStartStrokeColor, anchorStartColor, anchorStartText, mStartTextOffsetX, anchorStartTextColor);
    }

    private void drawEndAnchor(Canvas canvas, TimeSection.TimeAnchor anchor) {
        drawAnchor(canvas, anchor, anchorEndStrokeColor, anchorEndColor, anchorEndText, mEndTextOffsetX, anchorEndTextColor);
    }

    /**
     * 绘制锚点
     *
     * @param canvas        画板
     * @param anchor        锚点
     * @param strokeColor   边框颜色
     * @param bgColor       背景色
     * @param text          文字
     * @param offsetX       文字的X方向偏移量（中心点相对于起始点的偏移量=Xcenter - Xstart）
     * @param textColor     文字颜色
     */
    private void drawAnchor(Canvas canvas, TimeSection.TimeAnchor anchor
            , int strokeColor, int bgColor, String text,float offsetX, int textColor) {
        // 锚点边框
        if (anchorStrokeWidth != 0) {
            mAnchorPaint.setColor(strokeColor);
            canvas.drawCircle(anchor.x, anchor.y, mAnchorRadius, mAnchorPaint);
        }

        // 锚点背景
        mAnchorPaint.setColor(bgColor);
        canvas.drawCircle(anchor.x, anchor.y, mAnchorRadius - anchorStrokeWidth, mAnchorPaint);

        // 锚点文字
        float x = anchor.x - offsetX;
        float y = anchor.y + mTextOffsetY;
        mTextPaint.setColor(textColor);
        canvas.drawText(text, x, y, mTextPaint);
    }

    private int mDownStartMinute, mDownEndMinute;

    /**
     * 判断点（x, y）是否在圆环内
     */
    private boolean isInRingArea(float x, float y) {
        float circlePoint = (float) (Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
        Log.d(TAG, "isInBlankArea: x=" + x + ", y=" + y);
        Log.d(TAG, "isInBlankArea: circlePoint=" + circlePoint);

        // 判断是否在圆环内
        return circlePoint <= mOuterCircleRange && circlePoint >= mInterCircleRange;
    }

    /**
     * 判断点（x，y）是否在圆环上的已添加时间段上，即空白区域
     * @param x     坐标x
     * @param y     坐标y
     * @return  true-yes
     */
    private boolean isInBlankArea(float x, float y) {
        mDownStartMinute = getMinuteByPoint(x, y);
        mDownEndMinute = mDownStartMinute + initialMinutes;
        Log.d(TAG, "isInBlankArea: mDownStartMinute=" + mDownStartMinute + ", mDownEndMinute=" + mDownEndMinute);

        for (TimeSection section : mTimeSections) {
            if (section != null){
                if (isBetween(mDownStartMinute, section.start.minute, section.end.minute)
                        || isBetween(mDownEndMinute, section.start.minute, section.end.minute)) {
                    Log.d(TAG, "isInBlankArea: the point is in other section");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 根据给定的分钟，查找所在的时间段索引
     *
     * @return  返回时间段的索引值；-1：未找到
     */
    private int findSectionByMinute(int minute) {
        int index = 0;
        for (TimeSection section : mTimeSections) {
            if (section != null){
                if (isBetween(minute, section.start.minute, section.end.minute)) {
                    return index;
                }
            }
            index++;
        }
        return -1;
    }

    private boolean isBetween(int num, int begin, int end) {
        return num >= begin && num <= end;
    }

    private int getMinuteByPoint(float x, float y) {
        float cos = calculateCos(x, y);
        // 通过反三角函数获得角度值
        double angle;   //获取滑动的角度
        if (x < getWidth() / 2) { // 滑动超过180度
            angle = Math.PI * RADIAN + Math.acos(cos) * RADIAN;    //通过计算得到滑动的角度值
        } else { // 没有超过180度
            angle = Math.PI * RADIAN - Math.acos(cos) * RADIAN; //PI 周长比直径    返回弧角度的余弦值
        }
        return calcMinuteByAngle(angle);
    }

    private int mSelectedSectionIndex = -1;
    private boolean mIsStartAnchor;
    private TimeSection.TimeAnchor mNewAnchor;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        boolean needRefresh = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownStartMinute = mDownEndMinute = -1;
                int selectIndex = findAnchorIndexByLocation(x, y);
                Log.d(TAG, "onTouchEvent: selectIndex=" + selectIndex);
                if (selectIndex == -1) {
                    if (isInRingArea(x, y)) {
                        /*
                         在圆环内，未选中锚点时
                         1. 判断是否可以添加的条件：
                            a. 圆环上的非已有时间段上按下
                            b. 将创建的时间段没有超过界限
                         2. 判断是否在已有时间段上
                         */
                        if (isInBlankArea(x, y)) {
                            // 是否超界限
                            if (mDownEndMinute > MAX_MINUTE) {
                                return super.onTouchEvent(event);
                            }
                            // 检查是否还可以添加：还没达到最大数量
                            int addIndex = -1;
                            for (int i = 0; i < mTimeSections.length; i++) {
                                if (mTimeSections[i] == null) {
                                    addIndex = i;
                                    break;
                                }
                            }
                            if(addIndex != -1) {
                                Log.d(TAG, "onTouchEvent: create a section with index is " + addIndex);
                                mTimeSections[addIndex] = createTimeSection(mDownStartMinute, mDownEndMinute);
                                // 刷新并回调
                                refresh();
                                if (mListener != null) {
                                    TimePart part = new TimePart();
                                    part.start = mTimeSections[addIndex].start.minute;
                                    part.end = mTimeSections[addIndex].end.minute;
                                    mListener.onInsert(part);
                                }
                            }
                        } else if (quickCutEnable) {
                            // 快速截取
                            int index = findSectionByMinute(mDownStartMinute);
                            mTimeSections[index].end = generateAnchorByMinute(mDownStartMinute);
                            refresh();
                        }
                    }
                    return super.onTouchEvent(event);
                }

                // 确定具体的锚点
                mSelectedSectionIndex = selectIndex >> 1;
                mIsStartAnchor = (selectIndex & 0x01) == 0;
                if (mIsStartAnchor) {
                    Log.d(TAG, "onTouchEvent: selected point is start of TimeSection_" + mSelectedSectionIndex);
                } else {
                    Log.d(TAG, "onTouchEvent: selected point is end of TimeSection_" + mSelectedSectionIndex);
                }
                mNewAnchor = new TimeSection.TimeAnchor();

                if (mListener != null) {
                    TimeSection.TimeAnchor anchor = mIsStartAnchor ? mTimeSections[mSelectedSectionIndex].start : mTimeSections[mSelectedSectionIndex].end;
                    mListener.onSelectStart(anchor.minute);
                }
                postInvalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mNewAnchor == null) {
                    return super.onTouchEvent(event);
                }

                //通过触摸点算出cos角度值
                double cos = calculateCos(x, y);
                // 通过反三角函数获得角度值
                double angle;   //获取滑动的角度
                if (x < centerX) {
                    // 超过180°
                    angle = Math.PI * RADIAN + Math.acos(cos) * RADIAN;
                } else {
                    // 没有超过180°
                    angle = Math.PI * RADIAN - Math.acos(cos) * RADIAN;
                }

                int minute = calcMinuteByAngle(angle);
                int oldMinute = mNewAnchor.minute;

                /*
                 矫正angle、x、y：重新根据minute，反计算，防止由x、y计算出的minute相同，angle却不同的误差
                  */
                mNewAnchor = generateAnchorByMinute(minute);

                // 改变数据
                logD("onTouchEvent: ACTION_MOVE >>> selected point is %s of TimeSection_%d", mIsStartAnchor ? "start" : "end", mSelectedSectionIndex);
                logD("onTouchEvent: ACTION_MOVE >>> minute=%d, oldMinute=%d", minute, oldMinute);
                if (minute != oldMinute && mTimeSections[mSelectedSectionIndex] != null) {
                    Log.d(TAG, "onTouchEvent: ACTION_MOVE >>> ready to set");
                    if (mIsStartAnchor) {
                        // 确保在正常范围内
                        boolean inRange = minute >= 0 && minute <= mTimeSections[mSelectedSectionIndex].end.minute;
                        // 确保连续，不出现跳跃的情况。如：0~30， 0逆时针滑到30，就会跳跃过去
                        inRange &= Math.abs(minute - mTimeSections[mSelectedSectionIndex].start.minute) <= SMOOTH_RANGE_VALUE;
                        if (inRange) {
                            mTimeSections[mSelectedSectionIndex].start.copyFrom(mNewAnchor);
                            needRefresh = true;
                        }
                    } else if (minute <= MAX_MINUTE && minute >= mTimeSections[mSelectedSectionIndex].start.minute){
                        if (Math.abs(minute - mTimeSections[mSelectedSectionIndex].end.minute) <= SMOOTH_RANGE_VALUE) {
                            mTimeSections[mSelectedSectionIndex].end.copyFrom(mNewAnchor);
                            needRefresh = true;
                        }
                    }
                }

                // 进度改变回调
                if (needRefresh) {
                    logD("onTouchEvent: ACTION_MOVE >>> refresh...");
                    refresh();
                    if (mListener != null) {
                        mListener.onSelectChanged(minute);
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                logD("onTouchEvent: ACTION_UP ---------- mSelectedSectionIndex=%d, mNewAnchor is null: %b"
                        , mSelectedSectionIndex, mNewAnchor == null);
                if (mNewAnchor == null) {
                    return super.onTouchEvent(event);
                }
                /*
                新时间点的逻辑处理：
                1、与本时间段的另一时间比较：
                    1.1 如果一样，则删除
                2、如果新时间点与其他时间段交叉，则合并
                 */
                TimeSection moveSection = mTimeSections[mSelectedSectionIndex];
                if (moveSection != null) {
                    logD("onTouchEvent: ACTION_UP >>> minutes: mNewAnchor=%d, start=%d, end=%d"
                            , mNewAnchor.minute, mTimeSections[mSelectedSectionIndex].start.minute, mTimeSections[mSelectedSectionIndex].end.minute);
                    if (moveSection.start.minute == moveSection.end.minute) {
                        // 1.1 删除
                        Log.d(TAG, "onTouchEvent: ACTION_UP >>> will delete....");
                        mTimeSections[mSelectedSectionIndex] = null;
                        mNewAnchor = null;
                        mSelectedSectionIndex = -1;
                        refreshOnActionUp();
                        return true;
                    }

                }

                // 2 合并
                for (int i = 0, len = mTimeSections.length; i < len - 1; i++) {
                    TimeSection first = mTimeSections[i];
                    if (first == null) {
                        continue;
                    }
                    for (int j = i + 1; j < len; j++) {
                        TimeSection second = mTimeSections[j];
                        if (second == null) {
                            continue;
                        }
                        // 交叉，或包含
                        logD("onTouchEvent: compare %d[%d, %d] and %d[%d, %d]"
                                , i, first.start.minute, first.end.minute, j, second.start.minute, second.end.minute);
                        boolean isCross = !((first.start.minute < second.start.minute && first.end.minute < second.start.minute)
                                || (first.start.minute > second.end.minute && first.end.minute > second.end.minute));
                        if (isCross) {
                            logD("onTouchEvent ACTION_UP >>> : will combine %d and %d", i, j);
                            needRefresh = true;
                            if (second.start.minute < first.start.minute) {
                                first.start = second.start;
                            }
                            if (second.end.minute > first.end.minute) {
                                first.end = second.end;
                            }
                            mTimeSections[j] = null;
                            // 需要重新检查一遍
                            j = i;
                        }
                    }
                }

                // 数据复位
                mNewAnchor = null;
                mSelectedSectionIndex = -1;
                if (needRefresh) {
                    refreshOnActionUp();
                    return true;
                }
                if (mListener != null) {
                    mListener.onSelectFinished();
                }
                break;
            default: break;
        }

        return super.onTouchEvent(event);
    }

    private void refreshOnActionUp() {
        refresh();
        if (mListener != null) {
            mListener.onSelectFinished();
        }
    }


    /**
     * 刷新，并回调
     */
    private void refresh() {
        if (mListener != null) {
            mListener.onChanged(this, getTimeSections());
        }
        postInvalidate();
    }


    /**
     * 根据分钟生产锚点
     */
    private TimeSection.TimeAnchor generateAnchorByMinute(int minute) {
        TimeSection.TimeAnchor anchor = new TimeSection.TimeAnchor();
        anchor.minute = minute;
        anchor.angle = calcAngleByMinute(minute);
        double cos = -Math.cos(Math.toRadians(anchor.angle));
        anchor.x = calcXByCos(anchor.angle, cos);
        anchor.y = calcYByCos(cos);
        return anchor;
    }

    /**
     * 判断坐标点（x, y）是否在时间段的锚点上
     *
     * @param x     坐标点x坐标
     * @param y     坐标点y坐标
     * @return  >=0：在对应的锚点上；-1：不在
     */
    private int findAnchorIndexByLocation(float x, float y) {
        int index = -1;
        TimeSection.TimeAnchor point;
        for (TimeSection section : mTimeSections) {
            if (section != null) {
                index++;
                point = section.start;
                if (isSelectedAnchor(x, y, point)) {
                    return index;
                }
                index++;
                point = section.end;
                if (isSelectedAnchor(x, y, point)) {
                    return index;
                }
            } else {
                index += 2;
            }
        }
        return -1;
    }

    private boolean isSelectedAnchor(float x, float y, TimeSection.TimeAnchor point) {
        return (x - point.x)*(x - point.x) + (y - point.y) * (y - point.y) < mAnchorCircleRange;
    }


    /**
     * 拿到切斜角的cos值
     */
    private float calculateCos(float x, float y) {
        float width = x - centerX;
        float height = y - centerY;
        float slope = (float) Math.sqrt(width * width + height * height);
        return height / slope;
    }

    /**
     * 角度转进度
     */
    private int calcMinuteByAngle(double angle) {
        return Math.round(MAX_MINUTE * ((float) angle / 360));
    }

    /**
     * 根据分钟，计算角度
     */
    private double calcAngleByMinute(int minute) {
        return minute * 360./ MAX_MINUTE ;
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources()
                .getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getContext().getResources()
                .getDisplayMetrics());
    }

    private void logD(String format, Object... args) {
        Log.d(TAG, "zjun@" + String.format(format, args));
    }

    /**
     * 设置时间变化的监听事件
     */
    public void setOnTimeChangeListener(IOnTimeChangedListener listener) {
        mListener = listener;
    }

    /**
     * 获取当前的时间段集合值。这些时间段已经按起始时间，从小到大排好了序
     * @return  时间段集合
     */
    public synchronized List<TimePart> getTimeSections() {
        List<TimePart> list = new ArrayList<>();
        for (TimeSection section : mTimeSections) {
            if (section != null) {
                TimePart part = new TimePart();
                part.start = section.start.minute;
                part.end = section.end.minute;
                list.add(part);
            }
        }

        // 按从小到大排序
        Collections.sort(list, new TimePart.TimePartComparator());
        return list;
    }

    /**
     * 设置时间段集合
     * @param partList  时间段集合
     */
    public synchronized void setTimeSections(List<TimePart> partList) {
        if (partList == null) {
            return;
        }
        if (partList.size() > sectionSum) {
            throw new IllegalArgumentException("The size of partList must equal or smaller than sectionSum");
        }

        for (int i = 0; i < sectionSum; i++) {
            mTimeSections[i] = null;
        }

        int insertPos = 0;
        for (TimePart part : partList) {
            if (part != null) {
                if (!checkSection(part)) {
                    throw new IllegalArgumentException("The minutes of part must between 0 and 60, and end is larger than start!");
                }
                TimeSection section = new TimeSection();
                section.start = generateAnchorByMinute(part.start);
                section.end = generateAnchorByMinute(part.end);

                mTimeSections[insertPos++] = section;
            }
        }
        refresh();
    }

    /**
     * 清除所有时间段
     */
    public synchronized void clearTimeSections(){
        for (int i = 0; i < mTimeSections.length; i++) {
            mTimeSections[i] = null;
        }
        refresh();
    }

    /**
     * 检查是否是正常的TimePart
     */
    private boolean checkSection(TimePart part) {
        return part.start < part.end && part.start >= MIN_MINUTE && part.end <= MAX_MINUTE;
    }

    /**
     * 时间段类：[起始点, 终止点]
     */
    private static class TimeSection{
        /**
         * 起始锚点
         */
        TimeAnchor start;
        /**
         * 终止锚点
         */
        TimeAnchor end;

        /**
         * 时间点类
         */
        private static class TimeAnchor {
            /**
             * 分钟
             */
            int minute;
            /**
             * 角度
             */
            double angle;
            /**
             * 坐标x
             */
            float x;
            /**
             * 坐标y
             */
            float y;

            void copyFrom(TimeAnchor from) {
                if (from != null) {
                    minute = from.minute;
                    angle = from.angle;
                    x = from.x;
                    y = from.y;
                }
            }
        }
    }


    public interface IOnTimeChangedListener {
        /**
         * 当变化时，所有数据提交给回调
         * @param view          本控件
         * @param timePartList  所有时间段集合，已按时间从小到大排序
         */
        void onChanged(RingTimeView view, List<TimePart> timePartList);

        /**
         * 当插入一条时间段时，回调
         */
        void onInsert(TimePart part);

        /**
         * 当开始选择时间时，回调
         * @param minute    选中锚点的分钟
         */
        void onSelectStart(int minute);

        /**
         * 锚点正在变化的分钟值
         * @param minute    选中锚点的分钟
         */
        void onSelectChanged(int minute);

        /**
         * 不再选中时，回调
         */
        void onSelectFinished();


    }

    public static class TimePart {
        private int start;
        private int end;

        public TimePart(){}

        public TimePart(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        static class TimePartComparator implements Comparator<TimePart> {

            @Override
            public int compare(TimePart timePart, TimePart t1) {
                return timePart.start - t1.start;
            }
        }

    }

}
