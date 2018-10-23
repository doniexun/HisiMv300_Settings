package com.android.settings.iptv.nettest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author libeibei
 * Created by libeibei on 2018/1/17 0017.
 * 思路是：
 * 1. 绘制出来雷达的表盘，五个同心圆和四条对角线
 * 2. 一个雷达圆形图片radar_scan_img.png
 *    图片中有一道高亮竖线
 *    让这个图片旋转起来，高亮的竖线会出现扫描的效果
 * 3. 可以添加高亮的点，制造效果
 */


public class RadarView extends View {

    private Context mcontext;
    /**
     * mPaint：画笔
     * radarBitmap：执行雷达扫描的图片；
     * normalPointBitmap：低亮扫描点；
     * lightPointBitmap：高亮扫描点；
     * mPointCount:亮点的总个数；
     * mPointArray:存储已经添加的亮点的位置；
     * scanAngle:每次扫描动作的偏转角度；
     */
    private Paint mPaint;
    private Bitmap radarBitmap;
    private Bitmap normalPointBitmap;
    private Bitmap lightPointBitmap;
    private int mPointCount = 0;
    private List<String> mPointArray = new ArrayList<String>();
    private int scanAngle = 0;

    private int mWidth,mHeight;
    private Random random = new Random();
    /**
     * mOutWidth：外圆高度;
     * mCenterX：中心点X轴位置;
     * mCenterY：中心点Y轴位置;
     * mInsideRadius：内圆半径;
     * mOutsideRadius：外圆半径;
     */
    private int mOutWidth;
    private int mCenterX,mCenterY;
    private int mInsideRadius,mOutsideRadius;


    private boolean isSearching = false;

    /**
     * 构造函数
     * @param context
     */
    public RadarView(Context context) {
        this(context,null);
    }
    public RadarView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }
    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mcontext = context;
        initView();
    }

    /**
     * 初始化 画笔和图片资源
     */
    private void initView(){
        mPaint = new Paint();
        normalPointBitmap = Bitmap.createBitmap(BitmapFactory.decodeResource
                (mcontext.getResources(), R.drawable.radar_default_point_ico));
        lightPointBitmap = Bitmap.createBitmap(BitmapFactory.decodeResource
                (mcontext.getResources(),R.drawable.radar_light_point_ico));



    }

    /**
     * 测量视图及内容，确定其在父控件中的长度和宽度；
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mWidth == 0 || mHeight ==0){
            final int MinimumWidth = getSuggestedMinimumWidth();
            final int MinimumHeight = getSuggestedMinimumHeight();
            mWidth = resolveMeasure(widthMeasureSpec,MinimumWidth);
            mHeight = resolveMeasure(heightMeasureSpec,MinimumHeight);

            radarBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource
                    (mcontext.getResources(),R.drawable.radar_scan_img),
                    mWidth-mOutWidth,mWidth-mOutWidth,false);

            //获取X轴Y轴中心点
            mCenterX = mWidth /2;
            mCenterY = mHeight /2;

            //获取外圆环的厚度的两倍（即圆环左厚度右厚度的和）
            mOutWidth = mWidth /10;

            //外圆半径
            mOutsideRadius = mWidth /2;
            // 内圆的半径,除最外层,其它圆的半径=层数*insideRadius
            mInsideRadius = (mWidth - mOutWidth)/4/2;
        }



    }

    /**
     * 视图的绘制
     * 从外部向内部绘制
     * Android在用画笔的时候有三种Style，分别是
     * Paint.Style.STROKE 只绘制图形轮廓（描边）
     * Paint.Style.FILL 只绘制图形内容
     * Paint.Style.FILL_AND_STROKE 既绘制轮廓也绘制内容
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //设置画笔：抗锯齿，填充样式，画笔颜色；
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xffB8DCFC);
        //第一步：绘制外圆
        canvas.drawCircle(mCenterX,mCenterY,mOutsideRadius,mPaint);
        //第二步：绘制第一个内圆（最外层内圆）
        mPaint.setColor(0xff3278B4);
        canvas.drawCircle(mCenterX,mCenterY,mInsideRadius*4,mPaint);

        //第三部：绘制第二个内圆（倒数第二外层内圆）
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xff31C9F2);
        canvas.drawCircle(mCenterX,mCenterY,mInsideRadius*3,mPaint);
        //第四步：绘制第三个内圆（倒数第三外层内圆）
        canvas.drawCircle(mCenterX,mCenterY,mInsideRadius*2,mPaint);
        //第五步：绘制第四个内圆（最里面的内圆）
        canvas.drawCircle(mCenterX,mCenterY,mInsideRadius*1,mPaint);

        //第六步：绘制0°和180°对角线
        canvas.drawLine(mOutWidth/2,mCenterY,mWidth-mOutWidth/2,mCenterY,mPaint);
        //第七步：绘制90°和270°对角线
        canvas.drawLine(mWidth/2,mOutWidth/2,mWidth/2,mHeight-mOutWidth/2,mPaint);
        // 根据角度绘制对角线
        int startX, startY, endX, endY;
        double radian;
        // 第八步：绘制45°~225°对角线
        // 计算开始位置x/y坐标点
        // 将角度转换为弧度
        radian = Math.toRadians((double) 45);
        // 通过圆心坐标、半径和当前角度计算当前圆周的某点横坐标
        // 通过圆心坐标、半径和当前角度计算当前圆周的某点纵坐标
        startX = (int) (mCenterX + mInsideRadius * 4 * Math.cos(radian));
        startY = (int) (mCenterY + mInsideRadius * 4 * Math.sin(radian));
        // 计算结束位置x/y坐标点
        radian = Math.toRadians((double) 45 + 180);
        endX = (int) (mCenterX + mInsideRadius * 4 * Math.cos(radian));
        endY = (int) (mCenterY + mInsideRadius * 4 * Math.sin(radian));
        canvas.drawLine(startX, startY, endX, endY, mPaint);
        // 第九步：绘制135°~315°对角线
        // 计算开始位置x/y坐标点
        radian = Math.toRadians((double) 135);
        startX = (int) (mCenterX + mInsideRadius * 4 * Math.cos(radian));
        startY = (int) (mCenterY + mInsideRadius * 4 * Math.sin(radian));
        // 计算结束位置x/y坐标点
        radian = Math.toRadians((double) 135 + 180);
        endX = (int) (mCenterX + mInsideRadius * 4 * Math.cos(radian));
        endY = (int) (mCenterY + mInsideRadius * 4 * Math.sin(radian));
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        //第十步：绘制扫描的扇形
        // 用来保存Canvas的状态.save之后，可以调用Canvas的平移、放缩、旋转、错切、裁剪等操作.
        canvas.save();
        if(isSearching){
            // 绘制旋转角度,参数一：角度;参数二：x中心;参数三：y中心.
            canvas.rotate(scanAngle,mCenterX,mCenterY);
            canvas.drawBitmap(radarBitmap,mCenterX-radarBitmap.getWidth()/2,
                    mCenterY-radarBitmap.getHeight()/2,null);
            //扫描动作每次角度加3
            scanAngle += 3;
        }else{
            canvas.drawBitmap(radarBitmap,mCenterX-radarBitmap.getWidth()/2,
                    mCenterY-radarBitmap.getHeight()/2,null);
        }

        //第十一步：绘制扫描出来的动态点
        //重置canvas
        canvas.restore();
        if(mPointCount>0){
            //当前亮点个数比已经存储的亮点个数大时；
            //证明新增加了一个亮点，那么就随机生成一个亮点的坐标；
            //并将新增的坐标位置保存到List中；
            if(mPointCount > mPointArray.size()){
                int x = mOutWidth/2+random.nextInt(mInsideRadius*8);
                int y = mOutWidth/2+random.nextInt(mInsideRadius*8);
                mPointArray.add(x +"/"+ y);
            }

            //绘制已经存储了坐标的亮点
            for(int i =0;i<mPointArray.size();i++){
                String [] point = mPointArray.get(i).split("/");

                //最后添加的那个亮点要高亮
                //else，其他的亮点都低亮
                if(i < mPointArray.size()-1){
                    canvas.drawBitmap(normalPointBitmap,
                            Integer.parseInt(point[0]),Integer.parseInt(point[1]),null);
                }else{
                    canvas.drawBitmap(lightPointBitmap,
                            Integer.parseInt(point[0]),Integer.parseInt(point[1]),null);
                }

            }

        }


        if(isSearching){
            this.invalidate();
        }

    }

    /**
     *开始扫描
     */
    public void startSearching(){
        this.isSearching = true;
        this.invalidate();
    }

    /**
     * 停止扫描
     */
    public void stopSearching(){
        this.isSearching = false;
        this.invalidate();
    }
    /**
     * 添加亮点个数
     */
    public void addPoint(){
        this.mPointCount ++;
        this.invalidate();
    }

    /**
     * 解析数据，获取控件的宽和高
     * @param measureSpec
     * @param minimumWidth
     * @return
     */
    private int resolveMeasure(int measureSpec,int minimumWidth){

        int result =0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)){
            case MeasureSpec.AT_MOST:
                result = Math.min(specSize,minimumWidth);
                 break;
            case MeasureSpec.UNSPECIFIED:
                result = minimumWidth;
                break;

            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
                break;
        }

        return result;
    }


}
