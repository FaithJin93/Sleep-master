package com.scorpion.sleep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.os.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

public class MainActivity extends Activity {

    private static boolean BLUETOOTH_ENABLED = false;

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    private static SharedPreferences sp;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend,RdHistoryButton,DeleteHistoryButton;
    ArrayList<Double> yList;

    public static String filename="sleep.txt"; //用来保存存储的文件名
    private Context mContext;

    //for test
    int radMax = 100;
    int radMin = 1;
    int le = 0;

    private Timer timer = new Timer();
    private GraphicalView chart;
//    private int addY = -1;
//	private long addX;

    double _cnt = 0;
    /**曲线数量*/
    private static final int SERIES_NR=1;
    private TimeSeries series1;
    private XYMultipleSeriesDataset dataset1;
    static String fileNamePath = "";
//    /**时间数据*/
//    Date[] xcache = new Date[20];
//	/**数据*/
//    int[] ycache = new int[20];

    byte[] sdCardSaveValue = new byte[20];
    public static byte[] __sdCardSaveValue = new byte[1024];

    public static byte[] SYNC__sdCardSaveValue1 = new byte[20];
    public static byte[] SYNC__sdCardSaveValue2 = new byte[20];
    public static byte[] SYNC__sdCardSaveValue  = new byte[40];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        BLUETOOTH_ENABLED = true;
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            BLUETOOTH_ENABLED = false;
//            finish();
//            return;
        }
        mContext = this;
        sp = this.getSharedPreferences("sleep", Context.MODE_WORLD_READABLE);

        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        RdHistoryButton = (Button)findViewById(R.id.RdHistoryButton);
        DeleteHistoryButton = (Button)findViewById(R.id.DeleteHistoryButton);
        LinearLayout layout = (LinearLayout)findViewById(R.id.line);
        //生成图表
        chart = ChartFactory.getTimeChartView(this, getDateDemoDataset(), getDemoRenderer(), "hh:mm:ss");
        layout.addView(chart, new LayoutParams(LayoutParams.WRAP_CONTENT,380));
        if (BLUETOOTH_ENABLED) {
            service_init();
        }

        RdHistoryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(mContext,GraphicalViewClass.class);
                Bundle bundle=new Bundle();
                bundle.putBoolean("read", true);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        DeleteHistoryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                File file_name = new File(Environment.getExternalStorageDirectory().toString()
                        +File.separator
                        +"H0" + File.separator
                        +"log.txt");
                if(file_name.exists()){
                    file_name.delete();
                } else {
                    Toast.makeText(mContext, "暂无历史数据", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Handler Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtAdapter != null && !mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    if (btnConnectDisconnect.getText().equals("Connect")){

                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed
                        if (mDevice!=null)
                        {
                            mService.disconnect();

                        }
                    }
                }
            }
        });
        // Handler Send button  
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,GraphicalViewClass.class);
                Bundle bundle=new Bundle();
                bundle.putBoolean("read", false);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });

        // Set initial UI state

    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("Disconnect");
//                             edtMessage.setEnabled(true);
                        RdHistoryButton.setEnabled(false);
                        btnSend.setEnabled(true);
                        mState = UART_PROFILE_CONNECTED;
                        Intent intent = new Intent(mContext,GraphicalViewClass.class);
                        Bundle bundle=new Bundle();
                        bundle.putBoolean("read", false);
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("Connect");
//                             edtMessage.setEnabled(false);
                        RdHistoryButton.setEnabled(true);
                        btnSend.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();

                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
//                 final int length = intent.getIntExtra(UartService.EXTRA_DATA_1,0);

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            int raw = (txValue[0] * 256)+txValue[1];
                            if(raw >= 32768) raw = raw - 65536;
                            WriteLog(txValue[0] ,txValue[1]);
                            Message msg = new Message();
                            msg.what = 2;
                            msg.arg1 = (raw/100);
                            mhandler.sendMessage(msg);
//                         	}
                        } catch (Exception e) {
                            Log.e("错误", e.toString());
                            e.printStackTrace();
                        }

                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        try {
            unbindService(mServiceConnection);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (mService != null) {
            mService.stopSelf();
            mService= null;
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mBtAdapter != null && !mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    mService.connect(deviceAddress);


                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

//    @Override
//    public void onCheckedChanged(RadioGroup group, int checkedId) {
//       
//    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//   	                finish();
                            android.os.Process.killProcess(android.os.Process.myPid()) ;   //获取PID
                            System.exit(0);
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }


    Handler mhandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            System.out.println("RUN:" + GraphicalViewClass.is_run);
            if(GraphicalViewClass.is_run == false){
            } else{
                GraphicalViewClass.temp = ((int)msg.arg1);


                System.out.println("BLEpaketCnt:" + UartService.BLEPaketCnt);
            }
            super.handleMessage(msg);
        }
//	 	
    };

    private void WriteLog(byte b1,byte b2 ){
        FileOutputStream stream =null;
        File file_name = new File(Environment.getExternalStorageDirectory().toString()
                +File.separator
                +"H0" + File.separator
                +"log.txt");

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){

            if(!file_name.getParentFile().exists()){
                file_name.getParentFile().mkdir();
            }

            try {
                stream = new FileOutputStream(file_name,true);  //打开文件输入流
                stream.write(b1);
                stream.write(b2);
                stream.flush();
                Log.i("TAG", "OutPrintln");
            } catch (Exception e) {
                // TODO: handle exception
            }
            finally{
                if(stream != null){
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Log.i("提示:", "SD卡不存在" );
        }
    }

    public static  void writeSD(byte[] b,byte[] b1)
    {
        if(b[0] == (byte)0xAA && b[1] == (byte)0xAA && b[2] == (byte)0x20 && b[3] == (byte)0x02 && b[4] == (byte)0x00){
            FileOutputStream stream =null;
            try{
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    File sdCardDir = Environment.getExternalStorageDirectory();  //得到SD卡根目录
                    File BuildDir = new File(sdCardDir, "/H0");   //打开目录，如不存在则生成
                    if(BuildDir.exists()==false) {
                        BuildDir.mkdirs();
                        System.out.println("first make dir");
                    }
                    File saveFile =new File(BuildDir, filename);  //新建文件句柄，如已存在仍新建文档
                    if(!saveFile.exists()){
                        System.out.println("不已经存在");
                        Editor editor = sp.edit();
                        editor.putBoolean("file", true);
                        editor.commit();


                    }
                    fileNamePath = saveFile.getAbsolutePath();
                    if(sp.getBoolean("file", false) && saveFile.exists()){

                        Editor editor = sp.edit();
                        editor.putBoolean("file", false);
                        editor.commit();
                        System.out.println("clear flg");
                        Date date = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm:ss");

                        String currentDateTimeString = sdf.format(date);
                        beiju(0,"yubin               "+ ">"+currentDateTimeString,fileNamePath);
                    }

                    stream = new FileOutputStream(saveFile,true);  //打开文件输入流
                    stream.write(b);
                    stream.write(b1);
                    stream.flush();
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }finally{
                try {
                    stream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        else{
            System.out.println("丢失数据！****");
        }
    }
    /**
     * 设定如表样式
     * @return
     */
    private XYMultipleSeriesRenderer getDemoRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setChartTitle("实时脑电波曲线");//标题
        renderer.setChartTitleTextSize(20);
        renderer.setXTitle("时间");    //x轴说明
        renderer.setAxisTitleTextSize(16);
        renderer.setAxesColor(Color.BLACK);
        renderer.setLabelsTextSize(15);    //数轴刻度字体大小
        renderer.setLabelsColor(Color.BLACK);
        renderer.setLegendTextSize(15);    //曲线说明
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0,Color.BLACK);
        renderer.setShowLegend(false);
        renderer.setMargins(new int[] {20, 30, 100, 0});
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(Color.BLUE);
        r.setChartValuesTextSize(15);
        r.setChartValuesSpacing(3);
        r.setPointStyle(PointStyle.CIRCLE);
        r.setFillBelowLine(true);
        r.setFillBelowLineColor(Color.WHITE);
        r.setFillPoints(true);
        renderer.addSeriesRenderer(r);
        renderer.setMarginsColor(Color.WHITE);
        renderer.setPanEnabled(false,false);
        renderer.setShowGrid(true);
        renderer.setYAxisMax(255);
        renderer.setYAxisMin(-255);
        renderer.setInScroll(true);  //调整大小
        return renderer;
    }

    /**
     * 数据对象
     * @return
     */
    public XYMultipleSeriesDataset getDateDemoDataset() {
        dataset1 = new XYMultipleSeriesDataset();
        final int nr = 20;
        long value = new Date().getTime();
        Random r = new Random();
        for (int i = 0; i < SERIES_NR; i++) {
            series1 = new TimeSeries("Demo series " + (i + 1));
            for (int k = 0; k < nr; k++) {
                series1.add(new Date(value+k*1000), r.nextInt() % 10);
            }
            dataset1.addSeries(series1);
        }
        Log.i(TAG, dataset1.toString());
        return dataset1;
    }

    public  String bytesToHexString(byte[] bytes) {
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }

    public static String byteToString(byte b) {
        byte high, low;
        byte maskHigh = (byte) 0xf0;
        byte maskLow = 0x0f;
        high = (byte) ((b & maskHigh) >> 4);
        low = (byte) (b & maskLow);
        StringBuffer buf = new StringBuffer();
        buf.append(findHex(high));
        buf.append(findHex(low));
        return buf.toString();
    }
    private static char findHex(byte b) {
        int t = new Byte(b).intValue();
        t = t < 0 ? t + 16 : t;
        if ((0 <= t) && (t <= 9)) {
            return (char) (t + '0');
        }
        return (char) (t - 10 + 'A');
    }

    /**
     *
     * @param skip 跳过多少过字节进行插入数据
     * @param str 要插入的字符串
     * @param fileName 文件路径
     */
    public static void beiju(long skip, String str, String fileName){
        try {
            RandomAccessFile raf = new RandomAccessFile(fileName,"rw");
            if(skip <  0 || skip > raf.length()){
                System.out.println("跳过字节数无效");
                return;
            }
            byte[] b = str.getBytes();
            raf.setLength(raf.length() + b.length);
            for(long i = raf.length() - 1; i > b.length + skip - 1; i--){
                raf.seek(i - b.length);
                byte temp = raf.readByte();
                raf.seek(i);
                raf.writeByte(temp);
            }
            raf.seek(skip);
            raf.write(b);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //byte 数组与 int 的相互转换
    public static int byteArrayToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }
}
