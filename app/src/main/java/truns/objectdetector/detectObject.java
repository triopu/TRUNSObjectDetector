package truns.objectdetector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by trio_pu on 12/10/17.
 */

public class detectObject extends Activity implements OnTouchListener, CvCameraViewListener2, View.OnClickListener {

    private static final String TAG = "TRUNS Activity";

    private boolean             isColorSelectedObj1 = false;
    private boolean             isColorSelectedObj2 = false;

    private boolean             getObj1             = false;
    private boolean             getObj2             = false;
    private boolean             viewObj1            = false;
    private boolean             viewObj2            = false;
    private boolean             sendXYa             = false;

    private Mat                 rGba;
    private Scalar              objColorRgba;
    private Scalar              objColorHsv;
    private Scalar              objColorHsv1;
    private Scalar              objColorHsv2;

    private theDetectorObj1     theDetectorObj1;
    private theDetectorObj2     theDetectorObj2;
    private Mat                 theSpectrum;
    private Size                spectrumSize;
    private Scalar              contourColor1, contourColor2;
    private Scalar              lineColor1, lineColor2;
    private Scalar              centerColor1, centerColor2;
    private int                 xCent               = 0;
    private int                 yCent               = 0;
    private int                 theArea             = 0;


    String                      x1,y1,area1,x2,y2,area2,frameW, frameH, regionSet, areaSet;
    int                         regionS, areaS, cols, rows;

    Button          bBluetooth;
    Button          bBTDisconnect;
    ToggleButton    sendMthd;

    ToggleButton    tbObj1;
    ToggleButton    tbObj2;

    TextView        xObj1;
    TextView        yObj1;
    TextView        areaObj1;
    TextView        xObj2;
    TextView        yObj2;
    TextView        areaObj2;

    TextView        textView;
    TextView        textView2;
    TextView        textView3;
    TextView        textView4;

    TextView        frameInfo;

    EditText        offsetEdit1;
    EditText        editArea1;
    EditText        offsetEdit2;
    EditText        editArea2;

    ToggleButton    tbCheckObj1;
    ToggleButton    tbCheckObj2;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case bluetoothActivity.SUCCESS_CONNECT:
                    bluetoothActivity.connectedThread = new bluetoothActivity.ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(),"Connected!",Toast.LENGTH_SHORT).show();
                    String s = "Successfully  Connected";
                    bluetoothActivity.connectedThread.start();
                    break;
                case bluetoothActivity.MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    int i = 0;
                    for (i = 0; i < readBuf.length && readBuf[i] != 0; i++) {
                    }
                    final String Income = new String(readBuf,0,i);
                    String[] items = Income.split("\\*");
                    for(String item : items){
                    }
                    break;
            }
        }
    };

    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG,"OpenCV not loaded");
        }else{
            Log.d(TAG, "OpenCV loaded");
        }
    }

    private CameraBridgeViewBase openCvCameraView;
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:{
                    Log.i(TAG, "OpenCV loaded successfully");
                    openCvCameraView.enableView();
                    openCvCameraView.setOnTouchListener(detectObject.this);
                }break;
                default:{
                    super.onManagerConnected(status);
                }break;
            }
        }
    };

    public detectObject(){
        Log.i(TAG,"Instantiated new"+ this.getClass());
    }

    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.detectobject);
        openCvCameraView = (JavaCameraView)findViewById(R.id.camCV);
        openCvCameraView.setVisibility(SurfaceView.VISIBLE);
        openCvCameraView.setCvCameraViewListener(this);
        bluetoothActivity.gethandler(mHandler);
        initButton();
    }

    public void initButton(){

        bBluetooth = (Button)findViewById(R.id.bBT);
        bBluetooth.setOnClickListener(this);

        bBTDisconnect = (Button)findViewById(R.id.bBTDisconnect);
        bBTDisconnect.setOnClickListener(this);

        sendMthd = (ToggleButton)findViewById((R.id.sendMthd));
        sendMthd.setOnClickListener(this);

        tbObj1 = (ToggleButton)findViewById(R.id.tbObj1);
        tbObj1.setOnClickListener(this);
        tbObj2 = (ToggleButton)findViewById(R.id.tbObj2);
        tbObj2.setOnClickListener(this);

        xObj1 = (TextView) findViewById(R.id.textX1);
        xObj1.setMovementMethod(new ScrollingMovementMethod());
        yObj1 = (TextView) findViewById(R.id.textY1);
        yObj1.setMovementMethod(new ScrollingMovementMethod());
        areaObj1 = (TextView) findViewById(R.id.textArea1);
        areaObj1.setMovementMethod(new ScrollingMovementMethod());

        xObj2 = (TextView) findViewById(R.id.textX2);
        xObj2.setMovementMethod(new ScrollingMovementMethod());
        yObj2 = (TextView) findViewById(R.id.textY2);
        yObj2.setMovementMethod(new ScrollingMovementMethod());
        areaObj2 = (TextView) findViewById(R.id.textArea2);
        areaObj2.setMovementMethod(new ScrollingMovementMethod());

        frameInfo = (TextView) findViewById(R.id.frameInfo);
        frameInfo.setMovementMethod(new ScrollingMovementMethod());

        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView2 = (TextView) findViewById(R.id.textView2);
        textView2.setMovementMethod(new ScrollingMovementMethod());
        textView3 = (TextView) findViewById(R.id.textView3);
        textView3.setMovementMethod(new ScrollingMovementMethod());
        textView4 = (TextView) findViewById(R.id.textView4);
        textView4.setMovementMethod(new ScrollingMovementMethod());

        offsetEdit1  = (EditText) findViewById(R.id.offsetEdit1);
        editArea1    = (EditText) findViewById(R.id.editArea1);
        offsetEdit2  = (EditText) findViewById(R.id.offsetEdit2);
        editArea2    = (EditText) findViewById(R.id.editArea2);

        tbCheckObj1 = (ToggleButton) findViewById(R.id.tbCheckObj1);
        tbCheckObj1.setOnClickListener(this);

        tbCheckObj2 = (ToggleButton) findViewById(R.id.tbCheckObj2);
        tbCheckObj2.setOnClickListener(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        if(openCvCameraView != null)
            openCvCameraView.disableView();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback);
        }else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (openCvCameraView != null)
            openCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height){
        rGba            = new Mat(height, width, CvType.CV_8UC4);
        theDetectorObj1 = new theDetectorObj1();
        theDetectorObj2 = new theDetectorObj2();
        theSpectrum     = new Mat();
        objColorRgba    = new Scalar(255);
        objColorHsv     = new Scalar(255);
        spectrumSize    = new Size(200,64);
        contourColor1   = new Scalar(255,0,0,255);
        contourColor2   = new Scalar(0,255,0,255);
        lineColor1      = new Scalar(0,255,255,255);
        lineColor2      = new Scalar(255,255,0,255);
        centerColor1    = new Scalar(255,100,100,255);
        centerColor2    = new Scalar(255,0,255,255);
        cols = rGba.cols();
        rows = rGba.rows();
        frameW = Integer.toString(cols);
        frameH = Integer.toString(rows);
        String theFrame = String.format("%s x %s",frameW,frameH);
        frameInfo.setText(theFrame);
    }

    public void onCameraViewStopped(){
        rGba.release();
    }

    public boolean onTouch(View v, MotionEvent event){
        int cols = rGba.cols();
        int rows = rGba.rows();
        int xOffset = (openCvCameraView.getWidth() - cols) / 2;
        int yOffset = (openCvCameraView.getHeight() - rows) / 2;

        int x = (int) event.getX() - xOffset;
        int y = (int) event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x > 4) ? x - 4 : 0;
        touchedRect.y = (y > 4) ? y - 4 : 0;

        touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = rGba.submat(touchedRect);
        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        objColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;
        for (int i = 0; i < objColorHsv.val.length; i++)
            objColorHsv.val[i] /= pointCount;

        objColorRgba = convertScalarHsv2Rgba(objColorHsv);

        Log.i(TAG, "Touched rgba color: (" + objColorRgba.val[0] + ", " + objColorRgba.val[1] +
                ", " + objColorRgba.val[2] + ", " + objColorRgba.val[3] + ")");

        if(isColorSelectedObj1)theDetectorObj1.setHsvColor(objColorHsv1);
        else theDetectorObj1.setHsvColor(objColorHsv);

        if(isColorSelectedObj2)theDetectorObj2.setHsvColor(objColorHsv2);
        else theDetectorObj2.setHsvColor(objColorHsv);

        Imgproc.resize(theDetectorObj1.getSpectrum(), theSpectrum, spectrumSize);
        Imgproc.resize(theDetectorObj2.getSpectrum(), theSpectrum, spectrumSize);

        if(getObj1){
            isColorSelectedObj1 = true;
            objColorHsv1 = objColorHsv;
        }

        if(getObj2){
            isColorSelectedObj2 = true;
            objColorHsv2 = objColorHsv;
        }

        touchedRegionRgba.release();
        touchedRegionHsv.release();
        return false;
    }

    public void drawLine(){
        Point midTop    = new Point();
        Point midBottom = new Point();
        Point midLeft   = new Point();
        Point midRight  = new Point();

        midBottom.x     = cols/2; midBottom.y     = rows;
        midTop.x        = cols/2; midTop.y        = 0;

        double midBx = midBottom.x;
        double midBy = midBottom.y;

        midLeft.x       = 0; midLeft.y            = rows/2;
        midRight.x      = cols; midRight.y        = rows/2;

        Log.d("Garis Tengah",Double.toString(midBx)+":"+Double.toString(midBy));

        Imgproc.line(rGba,midTop,midBottom,lineColor1);
        Imgproc.line(rGba,midLeft,midRight,lineColor2);
    }

    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        rGba = inputFrame.rgba();
        drawLine();
        //Object 1
        if(viewObj1){
            getObj1();
            Log.d("ContourArea1", Integer.toString(theArea));
            Log.d("Coordinate1", "x: " + Integer.toString(xCent) + " y: " + Integer.toString(yCent));

            x1 = Integer.toString(xCent);
            y1 = Integer.toString(yCent);
            area1 = Integer.toString(theArea);
            if (sendXYa) {

                StringBuilder xOne = new StringBuilder();
                StringBuilder yOne = new StringBuilder();
                StringBuilder aOne = new StringBuilder();

                xOne.append('x').append(x1).append(':');
                yOne.append('y').append(y1).append(':');
                aOne.append('a').append(area1).append(':');

                String lastX = xOne.toString();
                String lastY = yOne.toString();
                String lastA = aOne.toString();
                Log.d("SENDXYA1: ", lastX + " : " + lastY + " : " + lastA);
                if (bluetoothActivity.connectedThread != null) {
                    bluetoothActivity.connectedThread.write(lastX);
                    bluetoothActivity.connectedThread.write(lastY);
                    bluetoothActivity.connectedThread.write(lastA);
                }
            } else {
                regionSet = offsetEdit1.getText().toString();
                areaSet = editArea1.getText().toString();

                if (regionSet.matches("")) regionS = 0;
                else regionS = Integer.parseInt(regionSet);

                if (areaSet.matches("")) areaS = 0;
                else areaS = Integer.parseInt(areaSet);
                objectMovementObj1(xCent, yCent, theArea, regionS, areaS);
            }
        }

        if(viewObj2) {
            //Object 2
            getObj2();
            Log.d("ContourArea2", Integer.toString(theArea));
            Log.d("Coordinate2", "x: " + Integer.toString(xCent) + " y: " + Integer.toString(yCent));

            x2 = Integer.toString(xCent);
            y2 = Integer.toString(yCent);
            area2 = Integer.toString(theArea);

            if (sendXYa) {
                StringBuilder xTwo = new StringBuilder();
                StringBuilder yTwo = new StringBuilder();
                StringBuilder aTwo = new StringBuilder();

                xTwo.append('X').append(x2).append(':');
                yTwo.append('Y').append(y2).append(':');
                aTwo.append('A').append(area2).append(':');

                String lastX = xTwo.toString();
                String lastY = yTwo.toString();
                String lastA = aTwo.toString();

                Log.d("SENDXYA2: ", lastX + " : " + lastY + " : " + lastA);
                if (bluetoothActivity.connectedThread != null) {
                    bluetoothActivity.connectedThread.write(lastX);
                    bluetoothActivity.connectedThread.write(lastY);
                    bluetoothActivity.connectedThread.write(lastA);
                }
            } else {
                regionSet = offsetEdit2.getText().toString();
                areaSet = editArea2.getText().toString();

                if (regionSet.matches("")) regionS = 0;
                else regionS = Integer.parseInt(regionSet);

                if (areaSet.matches("")) areaS = 0;
                else areaS = Integer.parseInt(areaSet);
                objectMovementObj2(xCent, yCent, theArea, regionS, areaS);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(viewObj1) {
                    xObj1.setText(x1);
                    yObj1.setText(y1);
                    areaObj1.setText(area1);
                }
                if(viewObj2) {
                    xObj2.setText(x2);
                    yObj2.setText(y2);
                    areaObj2.setText(area2);
                }
            }
        });
        return rGba;
    }

    public void getObj1(){
        theDetectorObj1.process(rGba);
        List<MatOfPoint> contours1 = theDetectorObj1.getContours();
        int maxAreaIdx1 = 0;
        double maxContourArea1 = 0;
        for (int idx = 0; idx < contours1.size(); idx++) {
            MatOfPoint contour1 = contours1.get(idx);
            double contourarea1 = Imgproc.contourArea(contour1);
            if (contourarea1 > maxContourArea1) {
                maxContourArea1 = contourarea1;
                maxAreaIdx1 = idx;
            }
        }

        if (contours1.isEmpty()) {
            //Biarkanlah...
        } else {
            Moments p = Imgproc.moments(contours1.get(maxAreaIdx1));
            Point center = new Point();
            center.x = (p.get_m10() / p.get_m00());
            center.y = (p.get_m01() / p.get_m00());
            double xD = center.x;
            double yD = center.y;
            xCent = (int) xD;
            yCent = (int) yD;
            Imgproc.circle(rGba,center,5,centerColor1);
        }

        theArea = (int) maxContourArea1;

        Imgproc.drawContours(rGba, contours1, -1, contourColor1);

    }

    public void getObj2(){
        theDetectorObj2.process(rGba);
        List<MatOfPoint> contours2 = theDetectorObj2.getContours();

        int maxAreaIdx2 = 0;
        double maxContourArea2 = 0;
        for (int idx = 0; idx < contours2.size(); idx++) {
            MatOfPoint contour2 = contours2.get(idx);
            double contourarea2 = Imgproc.contourArea(contour2);
            if (contourarea2 > maxContourArea2) {
                maxContourArea2 = contourarea2;
                maxAreaIdx2 = idx;
            }
        }

        if (contours2.isEmpty()) {
            //Biarkanlah...
        } else {
            Moments p = Imgproc.moments(contours2.get(maxAreaIdx2));
            Point center = new Point();
            center.x = (p.get_m10() / p.get_m00());
            center.y = (p.get_m01() / p.get_m00());
            double xD = center.x;
            double yD = center.y;
            xCent = (int) xD;
            yCent = (int) yD;
            Imgproc.circle(rGba,center,5,centerColor2);
        }

        theArea = (int) maxContourArea2;

        Imgproc.drawContours(rGba, contours2, -1, contourColor2);
    }

    public void objectMovementObj1(int x, int y, int area, int regionS, int areaS){
        int centerScreenX   = cols/2;

        if(x < centerScreenX - regionS){
            Log.d("To Do Obj1 -> ","Turn Left!");
            if (bluetoothActivity.connectedThread != null) bluetoothActivity.connectedThread.write("l");
        }
        else if(x > centerScreenX + regionS){
            Log.d("To Do Obj1 -> ","Turn Right!");
            if (bluetoothActivity.connectedThread != null) bluetoothActivity.connectedThread.write("r");
        }else{
            Log.d("To Do Obj1 -> ","Keep Tracking");
            if (bluetoothActivity.connectedThread != null) bluetoothActivity.connectedThread.write("f");
        }

        if(area > areaS){
            Log.d("To Do Obj1 -> ","Back!");
            if (bluetoothActivity.connectedThread != null) bluetoothActivity.connectedThread.write("b");
        }else{
            Log.d("To Do Obj1 -> ","Forward!");
            if (bluetoothActivity.connectedThread != null) bluetoothActivity.connectedThread.write("f");
        }
    }

    public void objectMovementObj2(int x, int y, int area, int regionS, int areaS){
        int centerScreenX   = cols/2;

        if(x < centerScreenX - regionS){
            Log.d("To Do Obj2 -> ","Turn Left!");
            if (bluetoothActivity.connectedThread != null) bluetoothActivity.connectedThread.write("L");
        }
        else if(x > centerScreenX + regionS){
            Log.d("To Do Obj2 -> ","Turn Right!");
            if (bluetoothActivity.connectedThread != null) bluetoothActivity.connectedThread.write("R");
        }else{
            Log.d("To Do Obj2 -> ","Keep Tracking");
            if (bluetoothActivity.connectedThread != null) bluetoothActivity.connectedThread.write("F");
        }

        if(area > areaS){
            Log.d("To Do Obj2 -> ","Back!");
            if (bluetoothActivity.connectedThread != null) bluetoothActivity.connectedThread.write("B");
        }else{
            Log.d("To Do Obj2 -> ","Forward!");
            if (bluetoothActivity.connectedThread != null) bluetoothActivity.connectedThread.write("F");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bBT:
                startActivity(new Intent("android.intent.action.BT1"));
                break;

            case R.id.bBTDisconnect:
                bluetoothActivity.disconnect();
                Toast.makeText(this, "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
                break;

            case R.id.tbObj1:
                if (tbObj1.isChecked()) {
                    getObj1 = true;
                    Toast.makeText(this, "Get The Ball!", Toast.LENGTH_SHORT).show();
                } else {
                    getObj1 = false;
                    Toast.makeText(this, "Ball Received", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tbObj2:
                if (tbObj2.isChecked()) {
                    getObj2 = true;
                    Toast.makeText(this, "Get The Goal!", Toast.LENGTH_SHORT).show();
                } else {
                    getObj2 = false;
                    Toast.makeText(this, "Goal Received!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.sendMthd:
                if (sendMthd.isChecked()) {
                    sendXYa = true;
                    Toast.makeText(this, "Sending X, Y, A!", Toast.LENGTH_SHORT).show();
                    offsetEdit1.setVisibility(View.GONE); offsetEdit2.setVisibility(View.GONE);
                    editArea1.setVisibility(View.GONE); editArea2.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE); textView2.setVisibility(View.GONE); textView3.setVisibility(View.GONE); textView4.setVisibility(View.GONE);
                } else {
                    sendXYa = false;
                    Toast.makeText(this, "Sending F, B, L, R", Toast.LENGTH_SHORT).show();
                    offsetEdit1.setVisibility(View.VISIBLE); offsetEdit2.setVisibility(View.VISIBLE);
                    editArea1.setVisibility(View.VISIBLE); editArea2.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE); textView2.setVisibility(View.VISIBLE); textView3.setVisibility(View.VISIBLE);textView4.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.tbCheckObj1:
                if (tbCheckObj1.isChecked()) {
                    viewObj1 = true;
                    Toast.makeText(this, "Ball!", Toast.LENGTH_SHORT).show();
                } else {
                    viewObj1 = false;
                    Toast.makeText(this, "No Ball", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tbCheckObj2:
                if (tbCheckObj2.isChecked()) {
                    viewObj2 = true;
                    Toast.makeText(this, "Goal!", Toast.LENGTH_SHORT).show();
                } else {
                    viewObj2 = false;
                    Toast.makeText(this, "No Goal", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
