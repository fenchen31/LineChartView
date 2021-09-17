package com.practice.linechartview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * @author 黎亮亮
 * @Date 2021/9/9
 * @describe 折线图
 * 大致思路：
 * 1.初始化画笔和路径等，设置模拟数据等
 * 2.获取activity中设置的数据信息
 * 3.在onMeasure()中设置宽高百分比等信息(根据数据将宽度等分，可用高度由外部传入（以百分比形式占据原控件的高），用数据的最大y和最小y的差值作为百分比等比例划分)
 * 4.算出每个点的坐标，绘制点，线和阴影并连线
 * 注意:1.当文字宽度超过10*2个像素时，第一个点的内容会从paddingLeft开始，最后一个点会到paddingRight结束
 * 2.点需在阴影上方，要么将点作为前景绘制，要么画点在画阴影之后
 */
public class LineChartView extends View {

    private Paint linePaint, shaderPaint, circlePointPaint, textPaint;
    private Path linePath, shaderPath;
    private float textSize = 14;
    private Rect textRect;//文字宽高的rect
    private final int left = dp2px(10), right = dp2px(10), top = dp2px(10), bottom = dp2px(10);
    private float weightHeight = dp2px(300) + top + bottom;
    private float weightWidth = dp2px(300) + left + right;//控件宽高
    private float heightCut, widthCut;//每一份占据的宽高
    private int lineWidth = dp2px(3), circlePointWidth = dp2px(3);
    private ArrayList<DataBean> data;
    private float availableHeight;//可用高度
    private float minHeight;//点的最小高度
    private int textRow = dp2px(5);//文字行间距
    private int heightPercent = 66;

    public LineChartView(Context context) {
        this(context, null);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attr) {
        linePath = new Path();
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));//画虚线

        circlePointPaint = new Paint();
        circlePointPaint.setAntiAlias(true);
        circlePointPaint.setColor(getResources().getColor(R.color.teal_200));
        circlePointPaint.setStrokeWidth(circlePointWidth);
        circlePointPaint.setStyle(Paint.Style.FILL);
        textRect = new Rect();

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);

        shaderPath = new Path();
        shaderPaint = new Paint();
        shaderPaint.setAntiAlias(true);
        shaderPaint.setColor(Color.BLACK);
        shaderPaint.setStrokeWidth(lineWidth);
        shaderPaint.setStyle(Paint.Style.FILL);
        int[] shaderColor = {ContextCompat.getColor(getContext(), R.color.color_FFDDE1), ContextCompat.getColor(getContext(), R.color.color_FFFFFF)};
        /**
         * 注意阴影的起点位置和终点位置，如果要以整个图像的最下方为终点，则初始化过程需要等到数据被传入时才能看到直接效果
         * 这里为了模拟数据，直接设置了阴影的终点位置
         */
        Shader shader = new LinearGradient(0, 0, dp2px(300), dp2px(300), shaderColor, null, Shader.TileMode.CLAMP);
        shaderPaint.setShader(shader);

        /*TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TestView);
        1 和2的区别在于当stringTest在xml中没有赋值时，获取的会是空值，效果就是“你好”被空值替换，
        而2表示只有当stringTest在xml中被赋值时才会重置，否则将使用“你好”
        //方法1
        boolean booleanTest = array.getBoolean(R.styleable.TestView_test_boolean, false);
        stringTest = array.getString(R.styleable.TestView_test_string);
        int intTest = array.getInt(R.styleable.TestView_test_enum, 1);
        //方法2
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int index = array.getIndex(i);
            switch (index) {
                case R.styleable.TestView_test_string:
                    stringTest = array.getString(R.styleable.TestView_test_string);
                    break;
            }
        }*/
        TypedArray array = getContext().obtainStyledAttributes(attr, R.styleable.LineChartView);
        for (int i = 0; i < array.getIndexCount(); i++) {
            int index = array.getIndex(i);
            switch (index) {
                case R.styleable.LineChartView_lineColor: {
                    linePaint.setColor(array.getColor(index, Color.GREEN));
                    break;
                }
                case R.styleable.LineChartView_android_textSize: {
                    textPaint.setTextSize(array.getFloat(index, 14f));
                    break;
                }
                case R.styleable.LineChartView_android_textColor: {
                    textPaint.setColor(array.getColor(index, Color.BLACK));
                    break;
                }
                case R.styleable.LineChartView_pointColor: {
                    circlePointPaint.setColor(array.getColor(index, getResources().getColor(R.color.teal_200)));
                    break;
                }
            }
        }
        mokeData(8);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widhtMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (widhtMode == MeasureSpec.AT_MOST) {
            width = (int) weightWidth + getPaddingLeft() + getPaddingRight();
        } else {
            weightWidth = width;
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            height = (int) weightHeight + getPaddingTop() + getPaddingBottom();
        } else {
            weightHeight = height;
        }
        getHeightAndWidth(heightPercent);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawContent(canvas);
    }

    private void drawContent(Canvas canvas) {
        if (data != null && data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                if (i == 0) {
                    linePath.moveTo(left + getPaddingLeft() + (i * widthCut), getPaddingTop() + top + (data.get(i).getyCut() - minHeight) * heightCut);
                    shaderPath.moveTo(left + getPaddingLeft() + (i * widthCut), getPaddingTop() + top + (data.get(i).getyCut() - minHeight) * heightCut);
                } else {
                    linePath.lineTo(left + getPaddingLeft() + (i * widthCut), getPaddingTop() + top + (data.get(i).getyCut() - minHeight) * heightCut);
                    shaderPath.lineTo(left + getPaddingLeft() + (i * widthCut), getPaddingTop() + top + (data.get(i).getyCut() - minHeight) * heightCut);
                    if (i == data.size() - 1) {
                        //阴影路径绘至最后一点时，从可绘制范围的下边界将第一点和最后一点连接起来
                        shaderPath.lineTo(left + getPaddingLeft() + (i * widthCut), getPaddingTop() + top + availableHeight);
                        shaderPath.lineTo(left + getPaddingLeft(), getPaddingTop() + top + availableHeight);
                        //close()方法本身就有连接起点和终点成为一个封闭图形的含义，故最后一个点的坐标可写可不写
                        //shaderPath.lineTo(left + getPaddingLeft(), getPaddingTop() + top + (data.get(0).getyCut() - minHeight) * heightCut);
                        shaderPath.close();
                    }
                }
                drawText(canvas, data.get(i).getDate(), left + getPaddingLeft() + (i * widthCut), false, i);
                drawText(canvas, data.get(i).getPrice(), left + getPaddingLeft() + (i * widthCut), true, i);
            }
            canvas.drawPath(linePath, linePaint);
            canvas.drawPath(shaderPath, shaderPaint);
            //注意点和线条及阴影的遮盖关系
            for (int i = 0; i < data.size(); i++) {
                canvas.drawCircle(getPaddingLeft() + left + (i * widthCut), getPaddingTop() + top + (data.get(i).getyCut() - minHeight) * heightCut, circlePointWidth, circlePointPaint);
            }
        }
    }

    /**
     * 关于文字绘制的居中
     * 文字的最左和最右两个点，控件默认为左left和右right都留了dp2px(10)个像素点，在绘制这两个点的文字时，
     * 如果文字宽度的一半超过了left但是没有超过每一份所占据的宽度widthCut，此时最左点的文字会从paddingLeft
     * 开始绘制，结束点会超过对点居中的右边（也就是没有对点居中，而是会靠右），有可能会和第二个点的内容重叠，暂时不对
     * 该情况进行处理，处理思路：将文字设置为自动滚播
     */
    private void drawText(Canvas canvas, String text, float x, boolean price, int index) {
        textPaint.getTextBounds(text, 0, text.length(), textRect);
        int textHeight = textRect.height();
        int textWidth = textRect.width();
        /**
         * 因为中文文字高度都相同，故默认取一行文字高度时，直接选择任意文字即可
         * 日期在上，价格在下
         * 文字是以基线（中文当中的基线可以大致理解为文字底部，详细的介绍还是自己百度一下比较好，毕竟勤能补拙）
         * 开始绘制，即drawText(String text, float x, float y, Paint paint)中的y，
         * 这里的y采用文字高度的1/2为基线进行绘制
         */
        if (textWidth > left * 2) {
            if (index == 0) {
                if (price) {
                    canvas.drawText(text, getPaddingLeft(), availableHeight + textRow * 2 + textHeight + top + getPaddingTop(), textPaint);
                } else {
                    canvas.drawText(text, getPaddingLeft(), availableHeight + textRow + textHeight / 2 + top + getPaddingTop(), textPaint);
                }
            } else if (index == data.size() - 1) {
                if (price) {
                    canvas.drawText(text, weightWidth - getPaddingRight() - textWidth, availableHeight + textRow * 2 + textHeight + top + getPaddingTop(), textPaint);
                } else {
                    canvas.drawText(text, weightWidth - getPaddingRight() - textWidth, availableHeight + textRow + textHeight / 2 + top + getPaddingTop(), textPaint);
                }
            } else {
                if (price) {
                    canvas.drawText(text, x - textWidth / 2, availableHeight + textRow * 2 + textHeight + top + getPaddingTop(), textPaint);
                } else {
                    canvas.drawText(text, x - textWidth / 2, availableHeight + textRow + textHeight / 2 + top + getPaddingTop(), textPaint);
                }
            }
        } else {
            if (price) {
                canvas.drawText(text, x - textWidth / 2, availableHeight + textRow * 2 + textHeight + top + getPaddingTop(), textPaint);
            } else {
                canvas.drawText(text, x - textWidth / 2, availableHeight + textRow + textHeight / 2 + top + getPaddingTop(), textPaint);
            }
        }
    }

    private void mokeData(int size) {//模拟数据
        data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            DataBean bean = new DataBean();
            bean.setxCut(i);
            bean.setyCut(i / 2);
            bean.setDate("202" + i + ".0" + i + ".0" + i);
            bean.setPrice("2" + i + ".1" + i);
            data.add(bean);
        }
    }

    /**
     * @param heightPercent 绘图高度所占据的整个控件高度（除去上下padding）的百分比
     *                      例：控件高度120，上下padding共20，heightPercent为60，即绘图的可用高度为 （120 - 20） * 60 / 100 = 60
     */
    private void getHeightAndWidth(int heightPercent) {
        int maxHeight = 0;
        for (DataBean bean : data) {
            minHeight = Math.min(minHeight, bean.getyCut());
            maxHeight = Math.max(maxHeight, bean.getyCut());
        }
        availableHeight = (weightHeight - top - bottom - getPaddingTop() - getPaddingBottom()) * heightPercent / 100;
        /**
         * 每一份占据的高度 = （数据的最大高度 - 数据最小高度） / 画图的可用高度
         * 画图可用高度：一般底部都会留有一部分用来绘制文字内容，比如日期，文字说明等，
         * 故在绘制折线图时，不能使画图区域占满整个控件
         */
        heightCut = availableHeight / (maxHeight - minHeight);
        widthCut = (weightWidth - left - right - getPaddingLeft() - getPaddingRight()) / (data.size() - 1);
    }

    public void setData(ArrayList<DataBean> data, int heightPercent) {
        if (data != null && data.size() > 0) {
            this.data = data;
            this.heightPercent = heightPercent;
            invalidate();//更新UI
        }
    }

    private int dp2px(int dp) {
        return (int) ((getResources().getDisplayMetrics().density * dp) + 0.5f);
    }

    private int px2dp(int px) {
        return (int) ((px - 0.5f) / getResources().getDisplayMetrics().density);
    }
}
