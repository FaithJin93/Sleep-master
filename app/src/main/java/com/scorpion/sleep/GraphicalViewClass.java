package com.scorpion.sleep;

import java.io.*;
import java.util.*;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;


import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.style.UpdateAppearance;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

public class GraphicalViewClass extends Activity{

	private static final String TAG = "GraphicalViewClass";
	private static GraphicalView chart;
	private static XYMultipleSeriesDataset dataset1;
	private static TimeSeries series1;

	private static int addY = -1;
	private static long addX;
	/**时间数据*/
	private static Date[] xcache = new Date[450];
	/**数据*/
	private static int[] ycache = new int[450];

	public static  boolean is_run = false;
	long len =0;
	boolean isrunnng = true;

	//---
	private static SurfaceHolder holder = null;    //画图使用，可以控制一个SurfaceView
	private static Paint paint = null;      //画笔
	SurfaceView surface = null;     //
	static Timer timer = new Timer();       //一个时间控制的对象，用于控制实时的x的坐标，
	//使其递增，类似于示波器从前到后扫描
	static TimerTask task = null;   //时间控制对象的一个任务
	static int HEIGHT=640;   //设置画图范围高度
	static int WIDTH=640;    //画图范围宽度
	final static int X_OFFSET = 5;  //x轴（原点）起始位置偏移画图范围一点
	private static int cx = 1;  //实时x的坐标
	static int centerY = HEIGHT/2 ;  //y轴的位置
	static int paintflag=1;//绘图是否暂停标志位，0为暂停
	static int temp = 0;                //临时变量用于保存接收到的数据
	private Random random=new Random();
	private Handler updateHandler;
	private Thread clockThread;

	private boolean is_readHistory = false;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.chart);
		//新页面接收数据
		Bundle bundle = this.getIntent().getExtras();
		//接收name值
		is_readHistory = bundle.getBoolean("read");
		Log.i("获取到的read值为",String.valueOf(is_readHistory).toString());


		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		WIDTH = dm.widthPixels;// 获取屏幕分辨率宽度
		HEIGHT = dm.heightPixels;
		System.out.println("W H" + WIDTH + HEIGHT);
		is_run = true;
		LinearLayout layout = (LinearLayout)findViewById(R.id.line_chart);
		//生成图表
		chart = ChartFactory.getTimeChartView(this, getDateDemoDataset(), getDemoRenderer(), "hh:mm:ss");
		layout.addView(chart, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));

		updateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				//刷新图表
				updateChart_test(temp);
//        			read();
				super.handleMessage(msg);
			}
		};
		task = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 200;
				updateHandler.sendMessage(message);
			}
		};
		timer.schedule(task, 1,100);

		clockThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(isrunnng){
					read();
				}
			}

		});

		if(is_readHistory == true){
			isrunnng = true;
			clockThread.start();
		}
	}


	private void updateChart_test(int data) {
		//设定长度为20
		int length = series1.getItemCount();
		if(length>=450) length = 450;
//	    if (addY == 0) addY = random.nextInt()%10;
//	    else 
		addY=data%10;//random.nextInt()%10;
		addX=new Date().getTime();
		//将前面的点放入缓存
		for (int i = 0; i < length; i++) {
			xcache[i] =  new Date((long)series1.getX(i));
			ycache[i] = (int) series1.getY(i);
		}
		series1.clear();
		//将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
		//这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
		series1.add(new Date(addX), addY);//new Date(addX)
		for (int k = 0; k < length; k++) {
			series1.add(xcache[k], ycache[k]);
		}
		//在数据集中添加新的点集
		dataset1.removeSeries(series1);
		dataset1.addSeries(series1);
		//曲线更新
		chart.invalidate();
	}

	/**
	 * 数据对象
	 * @return
	 */
	public XYMultipleSeriesDataset getDateDemoDataset() {
		dataset1 = new XYMultipleSeriesDataset();
		final int nr = 50;
		long value = new Date().getTime();
		Random r = new Random();
		for (int i = 0; i < 1; i++) {   // 画线 1条
			series1 = new TimeSeries("睡眠曲线 " + (i + 1));
			for (int k = 0; k < nr; k++) { //填充数据,一条标题150个点
				series1.add(new Date(value+k*1000), 0);
			}
			dataset1.addSeries(series1);
		}
		Log.i(TAG, dataset1.toString());
		return dataset1;
	}

	/**
	 * 曲线图(日期数据集) : 创建曲线图数据集, x轴是日期, y轴是具体的数值
	 *
	 * @param titles 各条曲线的标题, 放在一个数组中
	 * @param xValues x轴的日志值数组组成的集合
	 * @param yValusey轴具体的数据值数组组成的集合
	 * @return 具体的曲线图图表
	 */
//	   protected XYMultipleSeriesDataset buildDateDataset(String[] x, List<Date[]> xValues,
//	       List<double[]> yValues) {
//	     XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();/* 创建图表数据集 */
//	     int length = titles.length;                                     /* 获取曲线个数, 每个曲线都有一个标题 */
//	     for (int i = 0; i < length; i++) {
//	       TimeSeries series = new TimeSeries(titles[i]);                /* 带日期的单条曲线数据 */
//	       Date[] xV = xValues.get(i);                                   /* 获取该条曲线数据的 日期数组 */
//	       double[] yV = yValues.get(i);                                 /* 获取该条曲线数据的 值数组 */
//	       int seriesLength = xV.length;                                 /* 获取该条曲线的值的个数, 即x轴点个数 */
//	       for (int k = 0; k < seriesLength; k++) {
//	         series.add(xV[k], yV[k]);                                   /* 将日期数组 和 值数组设置给 带日期的单条曲线数据 */
//	       }
//	       dataset.addSeries(series);                                    /* 将单条曲线数据设置给 图标曲线数据集 */
//	     }
//	     return dataset;
//	   }

	/**
	 * 设定如表样式
	 * @return
	 */
	private XYMultipleSeriesRenderer getDemoRenderer() {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setChartTitle("实时脑电波曲线");//标题
		renderer.setChartTitleTextSize(20);
		renderer.setXTitle("时间");    //x轴说明
		renderer.setYTitle("参数(单位%)");    //y轴说明
		renderer.setAxisTitleTextSize(15);  //  坐标轴标题字体大小：15
		renderer.setChartTitleTextSize(10); // 图表标题字体大小：20
		renderer.setLabelsTextSize(15); // 轴标签字体大小：15
		renderer.setShowGrid(false); // 设置网格显示
		renderer.setAxesColor(Color.BLACK);   // 设置XY轴颜色
		renderer.setLabelsTextSize(10);    //数轴刻度字体大小
		renderer.setLabelsColor(Color.BLACK);  // 设置轴标签颜色
		renderer.setLegendTextSize(10);    //曲线说明
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0,Color.BLACK);
		renderer.setShowLegend(true);
		renderer.setMargins(new int[] {30, 15, 20, 0});  //left top right bottom
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.BLUE);
		r.setChartValuesTextSize(15);
		r.setChartValuesSpacing(1);
		r.setPointStyle(PointStyle.POINT);
		r.setFillBelowLine(true);
		r.setFillBelowLineColor(Color.WHITE);
		r.setFillPoints(true);
		renderer.addSeriesRenderer(r);
		renderer.setMarginsColor(Color.WHITE);
		renderer.setPanEnabled(false,false);
		renderer.setYLabels(3);
//			    renderer.setXLabels(3);
		renderer.setShowGrid(false);
		renderer.setYAxisMax(30);
		renderer.setYAxisMin(-30);
		renderer.setInScroll(true);  //调整大小
		return renderer;
	}
//		   	public static   void updateChart(int data) {
//////		   		if(is_run == false)
////		   		temp = data;
////		   			new DrawThread().start();
//			    //设定长度为20
//			    int length = series1.getItemCount();
//			    
//			    addY=data%10;//.nextInt()%10;
//			    addX=new Date().getTime();
//			    
//			    System.out.println("Length:" +length + "  addY:" + addY + " addX:" + addX);
//			    if(length>=250) length = 250;
//			    //将前面的点放入缓存
//				for (int i = 0; i < length; i++) {
//					xcache[i] =  new Date((long)series1.getX(i));
//					ycache[i] = (int) series1.getY(i);
//				}
//			    
//				series1.clear();
//				//将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
//				//这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
//				series1.add(new Date(addX), addY);
//				for (int k = 0; k < length; k++) {
//		    		series1.add(xcache[length], ycache[length]);
//		    	}
//				//在数据集中添加新的点集
//				dataset1.removeSeries(series1);
//				dataset1.addSeries(series1);
//				//曲线更新
//				chart.invalidate();
//		    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		is_run = false;
		is_readHistory = false;
		System.out.println("OnDestory:" + is_run);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	private void read()
	{
		long cnt = 0;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			File file = new File(Environment
					.getExternalStorageDirectory().toString()
					+ File.separator
					+ "H0" + File.separator + "log.txt") ;	// 定义File类对象

			if (! file.getParentFile().exists()) {
				Toast.makeText(this, "暂无历史记录", 1).show();
				return;
//					file.getParentFile().mkdirs() ;

			}

			Scanner scan = null ;							// 扫描输入
			try {
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
				ByteArrayOutputStream out = new ByteArrayOutputStream(1024);

				System.out.println("Available bytes:" + in.available());

				byte[] tempb = new byte[1024];
				int size = 0;
				while ((size = in.read(tempb)) != -1) {
					out.write(tempb, 0, size);
				}
				in.close();

				byte[] content = out.toByteArray();
//	                System.out.println("Readed bytes count:" + content.length);

				if(len==0)
					len = content.length;
				System.out.println("Len:" + len);
				for(int i = 0  ; i<content.length  ;i++){
					len--;
					if(len <= 1)
					{
						isrunnng = false;
						clockThread.interrupt();
						System.out.println("Stop"  + len);
						finish();
					}

					System.out.println("Stop"  + len);
					this.temp = content[i] *256 + content[(i+1)];
					if(temp >= 32768) temp = temp - 65536;

					temp = temp / 100;
					try {
						Thread.sleep(20);
					}catch (InterruptedException e) {
						e.printStackTrace();
					}

//	                System.out.println("ByteData:" + temp);//MainActivity.byteToString(content[i])

//	                if(content[i] == (byte)0xAA ){
//	                	
//	                	if(content[i+1] == (byte)0xAA ) {
//	                		
//	                		if(content[i+2] == (byte)0x20) {
//	                			System.out.println("AAAA******" + len++);
//	                			GraphicalViewClass.temp = temp[40+i]; 
//	                		}
//	                			
//	                	}
//	                }

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


}
