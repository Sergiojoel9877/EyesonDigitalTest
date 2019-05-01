/**
 * <p>DemoActivity Class</p>
 * @author zhuzhenlei 2014-7-17
 * @version V1.0  
 * @modificationHistory
 * @modify by user: 
 * @modify by reason:
 */
package com.test.demo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.INT_PTR;
import com.hikvision.netsdk.NET_DVR_COMPRESSIONCFG_V30;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PLAYBACK_INFO;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.NET_DVR_TIME;
import com.hikvision.netsdk.NET_DVR_VOD_PARA;
import com.hikvision.netsdk.PTZCommand;
import com.hikvision.netsdk.PlaybackControlCommand;

import org.MediaPlayer.PlayM4.Player;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * <pre>
 *  ClassName  DemoActivity Class
 * </pre>
 * 
 * @author zhuzhenlei
 * @version V1.0
 * @modificationHistory
 */
public class DemoActivity extends Activity {
    private Button m_oLoginBtn = null;
    private Button m_oPreviewBtn = null;
    private Button m_oPlaybackBtn = null;
    private Button m_oParamCfgBtn = null;
    private Button m_oCaptureBtn = null;
    private Button m_oRecordBtn = null;
    private Button m_oTalkBtn = null;
    private Button m_oPTZBtn = null;
    private Button m_oOtherBtn = null;
    private EditText m_oIPAddr = null;
    private EditText m_oPort = null;
    private EditText m_oUser = null;
    private EditText m_oPsd = null;
    private EditText m_oCam = null;
    private EditText m_oDate = null;
    private EditText m_oTime = null;
    private TimePicker timePicker;
    private DatePicker datePicker;
    private Calendar calendar;

    private Gson gson = new Gson();

    private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = null;

    private int m_iLogID = -1; // return by NET_DVR_Login_v30
    private int m_iPlayID = -1; // return by NET_DVR_RealPlay_V30
    private int m_iPlaybackID = -1; // return by NET_DVR_PlayBackByTime

    private int m_iPort = -1; // play port

    public int getM_iStartChan() {
        return m_iStartChan;
    }

    public void setM_iStartChan(int m_iStartChan) {
        this.m_iStartChan = m_iStartChan;
    }

    private int m_iStartChan = 0; // start channel no
    private int m_iChanNum = 0; // channel number
    private static PlaySurfaceView[] playView = new PlaySurfaceView[4];

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    private int year;
    private int month;
    private int day;

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    private int hour;
    private int minute;

    private int m_lUserID = -1;

    private final String TAG = "DemoActivity";

    private boolean m_bTalkOn = false;
    private boolean m_bPTZL = false;
    private boolean m_bMultiPlay = false;

    private boolean m_bNeedDecode = true;
    private boolean m_bSaveRealData = false;
    private boolean m_bStopPlayback = false;

    public static Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static Context context;

    //Socket
    private BufferedReader m_dDataIn = null;
    private com.github.nkzawa.socketio.client.Socket m_sSocket = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashUtil crashUtil = CrashUtil.getInstance();
        crashUtil.init(this);

        setContentView(R.layout.main);


        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                              .build());

        if (!initeSdk()) {
            this.finish();
            return;
        }

        if (!initeActivity()) {
            this.finish();
            return;
        }
        //TODO Finish Socket implementation
        // m_oIPAddr.setText("10.17.132.49");

        //TODO COMPLETE PENDING CHANGES.

        m_oIPAddr.setText("50.78.124.78");
        m_oPort.setText("8001");
        m_oUser.setText("eyeson");
        m_oPsd.setText("2004eyeson");
        m_oCam.setText("1");

        setM_iStartChan(Integer.valueOf(m_oCam.getText().toString()) - 1);

        Client client = new Client(m_oIPAddr.getText().toString(), Integer.valueOf(m_oPort.getText().toString()), m_oTime);
        client.execute();
    }

    private void SetSocket(String IP, int m_iPort)
    {
        new SocketClient().execute(IP, String.valueOf(m_iPort));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("m_iPort", m_iPort);
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        m_iPort = savedInstanceState.getInt("m_iPort");
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");
    }

    /**
     * @fn initeSdk
     * @author zhuzhenlei
     * @brief SDK init
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return true - success;false - fail
     */
    private boolean initeSdk() {
        // init net sdk
        if (!HCNetSDK.getInstance().NET_DVR_Init()) {
            Log.e(TAG, "HCNetSDK init is failed!");
            return false;
        }
        HCNetSDK.getInstance().NET_DVR_SetLogToFile(3, "/mnt/sdcard/sdklog/",
                true);
        return true;
    }

    // GUI init
    private boolean initeActivity() {
        findViews();
        setListeners();

        return true;
    }

    private void ChangeSingleSurFace(boolean bSingle) {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        for (int i = 0; i < 4; i++) {
            if (playView[i] == null) {
                playView[i] = new PlaySurfaceView(this);
                playView[i].setParam(metric.widthPixels);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = playView[i].getM_iHeight() - (i / 2)
                        * playView[i].getM_iHeight();
                params.leftMargin = (i % 2) * playView[i].getM_iWidth();
                params.gravity = Gravity.BOTTOM | Gravity.LEFT;
                addContentView(playView[i], params);
                playView[i].setVisibility(View.INVISIBLE);

            }
        }

        if (bSingle) {
            // ��·ֻ��ʾ����1
            for (int i = 0; i < 4; ++i) {
                playView[i].setVisibility(View.INVISIBLE);
            }
            playView[0].setParam(metric.widthPixels * 2);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = playView[3].getM_iHeight() - (3 / 2) * playView[3].getM_iHeight();
//            params.bottomMargin = 0;
            params.leftMargin = 0;
            // params.
            params.gravity = Gravity.BOTTOM | Gravity.LEFT;
            playView[0].setLayoutParams(params);
            playView[0].setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < 4; ++i) {
                playView[i].setVisibility(View.VISIBLE);
            }

            playView[0].setParam(metric.widthPixels);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = playView[0].getM_iHeight() - (0 / 2)
                    * playView[0].getM_iHeight();
            params.leftMargin = (0 % 2) * playView[0].getM_iWidth();
            params.gravity = Gravity.BOTTOM | Gravity.LEFT;
            playView[0].setLayoutParams(params);
        }

    }
//TODO
    // get controller instance
    private void findViews() {
        m_oLoginBtn = (Button) findViewById(R.id.btn_Login);
        m_oPreviewBtn = (Button) findViewById(R.id.btn_Preview);
        m_oPlaybackBtn = (Button) findViewById(R.id.btn_Playback);
        m_oParamCfgBtn = (Button) findViewById(R.id.btn_ParamCfg);
        m_oCaptureBtn = (Button) findViewById(R.id.btn_Capture);
        m_oRecordBtn = (Button) findViewById(R.id.btn_Record);
        m_oTalkBtn = (Button) findViewById(R.id.btn_Talk);
        m_oPTZBtn = (Button) findViewById(R.id.btn_PTZ);
        m_oOtherBtn = (Button) findViewById(R.id.btn_OTHER);
        m_oIPAddr = (EditText) findViewById(R.id.EDT_IPAddr);
        m_oPort = (EditText) findViewById(R.id.EDT_Port);
        m_oUser = (EditText) findViewById(R.id.EDT_User);
        m_oPsd = (EditText) findViewById(R.id.EDT_Psd);
        m_oCam = (EditText) findViewById(R.id.EDT_Cam);
        m_oDate = (EditText) findViewById(R.id.EDT_Date);
        m_oTime = (EditText) findViewById(R.id.EDT_Hr);
    }

    // listen
    private void setListeners() {
        m_oLoginBtn.setOnClickListener(Login_Listener);
        m_oPreviewBtn.setOnClickListener(Preview_Listener);
        m_oPlaybackBtn.setOnClickListener(Playback_Listener);
        m_oParamCfgBtn.setOnClickListener(ParamCfg_Listener);
        m_oCaptureBtn.setOnClickListener(Capture_Listener);
        m_oRecordBtn.setOnClickListener(Record_Listener);
        m_oTalkBtn.setOnClickListener(Talk_Listener);
        m_oOtherBtn.setOnClickListener(OtherFunc_Listener);
        m_oPTZBtn.setOnTouchListener(PTZ_Listener);
        m_oDate.setOnFocusChangeListener(Dt_Listener);
        m_oTime.setOnFocusChangeListener(T_Listener);
//        m_oTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                // TODO Auto-generated method stub
//                calendar = Calendar.getInstance();
//                int hour = calendar.get(Calendar.HOUR_OF_DAY);
//                int minute = calendar.get(Calendar.MINUTE);
//                TimePickerDialog mTimePicker;
//                mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
//                        m_oTime.setText( selectedHour + ":" + selectedMinute);
//                        setHour(selectedHour);
//                        setMinute(selectedMinute);
//                    }
//                }, hour, minute, false);//Yes 24 hour time
//                mTimePicker.setTitle("Select Time");
//                mTimePicker.show();
//            }
//        });
        m_oCam.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() > 0)
                {
                    setM_iStartChan(Integer.valueOf(s.toString()) - 1);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private EditText.OnFocusChangeListener T_Listener = new
            View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    showDialog(998);
                }
            };

    @SuppressWarnings("deprecation")
    private EditText.OnFocusChangeListener Dt_Listener = new
            View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            showDialog(999);
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        if(id == 998){
            return new TimePickerDialog(this, myTimeListener, hour, minute, false);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                }
            };

    private TimePickerDialog.OnTimeSetListener myTimeListener = new
            TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    showTime(hourOfDay, minute);
                }
            };

    private void showTime(int hour, int minute) {

        setHour(hour);
        setMinute(minute);

        m_oTime.setText(new StringBuilder().append(hour).append(":")
                .append(minute));
    }

    private void showDate(int year, int month, int day) {

        setYear(year);
        setMonth(month);
        setDay(day);

        m_oDate.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }
    // ptz listener
    private Button.OnTouchListener PTZ_Listener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                if (m_iLogID < 0) {
                    Log.e(TAG, "please login on a device first");
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (m_bPTZL == false) {
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                m_iLogID, m_iStartChan, PTZCommand.PAN_LEFT, 0)) {
                            Log.e(TAG,
                                    "start PAN_LEFT failed with error code: "
                                            + HCNetSDK.getInstance()
                                                    .NET_DVR_GetLastError());
                        } else {
                            Log.i(TAG, "start PAN_LEFT succ");
                        }
                    } else {
                        if (!HCNetSDK.getInstance()
                                .NET_DVR_PTZControl_Other(m_iLogID,
                                        m_iStartChan, PTZCommand.PAN_RIGHT, 0)) {
                            Log.e(TAG,
                                    "start PAN_RIGHT failed with error code: "
                                            + HCNetSDK.getInstance()
                                                    .NET_DVR_GetLastError());
                        } else {
                            Log.i(TAG, "start PAN_RIGHT succ");
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (m_bPTZL == false) {
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                m_iLogID, m_iStartChan, PTZCommand.PAN_LEFT, 1)) {
                            Log.e(TAG, "stop PAN_LEFT failed with error code: "
                                    + HCNetSDK.getInstance()
                                            .NET_DVR_GetLastError());
                        } else {
                            Log.i(TAG, "stop PAN_LEFT succ");
                        }
                        m_bPTZL = true;
                        m_oPTZBtn.setText("PTZ(R)");
                    } else {
                        if (!HCNetSDK.getInstance()
                                .NET_DVR_PTZControl_Other(m_iLogID,
                                        m_iStartChan, PTZCommand.PAN_RIGHT, 1)) {
                            Log.e(TAG,
                                    "stop PAN_RIGHT failed with error code: "
                                            + HCNetSDK.getInstance()
                                                    .NET_DVR_GetLastError());
                        } else {
                            Log.i(TAG, "stop PAN_RIGHT succ");
                        }
                        m_bPTZL = false;
                        m_oPTZBtn.setText("PTZ(L)");
                    }
                }
                return true;
            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
                return false;
            }
        }
    };
    // preset listener
    private Button.OnClickListener OtherFunc_Listener = new OnClickListener() {
        public void onClick(View v) {
            // PTZTest.TEST_PTZ(m_iPlayID, m_iLogID, m_iStartChan);
            // ConfigTest.Test_ScreenConfig(m_iLogID, m_iStartChan);
            // PTZTest.TEST_PTZ(m_iPlayID, m_iLogID, m_iStartChan);

            /*
             * try { //PictureTest.PicUpload(m_iLogID); } catch
             * (InterruptedException e) { // TODO Auto-generated catch block
             * e.printStackTrace(); }
             */

            // PictureTest.BaseMap(m_iLogID);
            // DecodeTest.PicPreview(m_iLogID);
            // ManageTest.TEST_Manage(m_iLogID);
            // AlarmTest.Test_SetupAlarm(m_iLogID);
            // OtherFunction.TEST_OtherFunc(m_iPlayID, m_iLogID, m_iStartChan);
            // JNATest.TEST_Config(m_iPlayID, m_iLogID, m_iStartChan);
            ConfigTest.TEST_Config(m_iPlayID, m_iLogID, m_iStartChan);
            // HttpTest.Test_HTTP();
             ScreenTest.TEST_Screen(m_iLogID);
        }
    };
    // Talk listener
    private Button.OnClickListener Talk_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            try {
                if (m_bTalkOn == false) {
                    if (VoiceTalk.startVoiceTalk(m_iLogID) >= 0) {
                        m_bTalkOn = true;
                        m_oTalkBtn.setText("Stop");
                    }
                } else {
                    if (VoiceTalk.stopVoiceTalk()) {
                        m_bTalkOn = false;
                        m_oTalkBtn.setText("Talk");
                    }
                }
            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
            }
        }
    };
    // record listener
    private Button.OnClickListener Record_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            if (!m_bSaveRealData) {
                if (!HCNetSDK.getInstance().NET_DVR_SaveRealData(m_iPlayID,
                        "/sdcard/test.mp4")) {
                    System.out.println("NET_DVR_SaveRealData failed! error: "
                            + HCNetSDK.getInstance().NET_DVR_GetLastError());
                    return;
                } else {
                    System.out.println("NET_DVR_SaveRealData succ!");
                }
                m_bSaveRealData = true;
            } else {
                if (!HCNetSDK.getInstance().NET_DVR_StopSaveRealData(m_iPlayID)) {
                    System.out
                            .println("NET_DVR_StopSaveRealData failed! error: "
                                    + HCNetSDK.getInstance()
                                            .NET_DVR_GetLastError());
                } else {
                    System.out.println("NET_DVR_StopSaveRealData succ!");
                }
                m_bSaveRealData = false;
            }
        }
    };
    // capture listener
    private Button.OnClickListener Capture_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            try {
                if (m_iPort < 0) {
                    Log.e(TAG, "please start preview first");
                    return;
                }
                Player.MPInteger stWidth = new Player.MPInteger();
                Player.MPInteger stHeight = new Player.MPInteger();
                if (!Player.getInstance().getPictureSize(m_iPort, stWidth,
                        stHeight)) {
                    Log.e(TAG, "getPictureSize failed with error code:"
                            + Player.getInstance().getLastError(m_iPort));
                    return;
                }
                int nSize = 5 * stWidth.value * stHeight.value;
                byte[] picBuf = new byte[nSize];
                Player.MPInteger stSize = new Player.MPInteger();
                if (!Player.getInstance()
                        .getBMP(m_iPort, picBuf, nSize, stSize)) {
                    Log.e(TAG, "getBMP failed with error code:"
                            + Player.getInstance().getLastError(m_iPort));
                    return;
                }

                SimpleDateFormat sDateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd-hh:mm:ss");
                String date = sDateFormat.format(new java.util.Date());
                FileOutputStream file = new FileOutputStream("/mnt/sdcard/"
                        + date + ".bmp");
                file.write(picBuf, 0, stSize.value);
                file.close();
            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
            }
        }
    };

//     playback listener
    private Button.OnClickListener Playback_Listener = new
     Button.OnClickListener() {

     public void onClick(View v) {
         try {
             if (m_iLogID < 0) {
                 Log.e(TAG, "please login on a device first");
                 Toast.makeText(getApplicationContext(), "please login on a device first" ,Toast.LENGTH_LONG).show();
                 return;
             }
         if (m_iPlaybackID < 0) {
             if (m_iPlayID >= 0) {
                Log.i(TAG, "Please stop preview first");
                Toast.makeText(getApplicationContext(), "Please stop preview first" ,Toast.LENGTH_LONG).show();
             return;
         }

         ChangeSingleSurFace(true);

         NET_DVR_TIME struBegin = new NET_DVR_TIME();
         NET_DVR_TIME struEnd = new NET_DVR_TIME();

         struBegin.dwYear = getYear();
         struBegin.dwMonth = getMonth();
         struBegin.dwDay = getDay();
         struBegin.dwHour = getHour();
         struBegin.dwMinute= getMinute();
         struBegin.dwSecond = 00;

         struEnd.dwYear = 2019;
         struEnd.dwMonth = 4;
         struEnd.dwDay = 26;
         struEnd.dwHour = 10;
         struEnd.dwMinute= 48;
         struEnd.dwSecond = 20;

         NET_DVR_VOD_PARA struVod = new NET_DVR_VOD_PARA();
         struVod.struBeginTime = struBegin;
         struVod.struEndTime = struEnd;
         struVod.byStreamType = 0;
         struVod.struIDInfo.dwChannel = getM_iStartChan() == 0 ? Integer.valueOf(m_oCam.getText().toString()) - 1: Integer.valueOf(m_oCam.getText().toString()) - 1;// getM_iStartChan(); //m_iStartChan;
         struVod.hWnd = playView[0].getHolder().getSurface();

         m_iPlaybackID =
         HCNetSDK.getInstance().NET_DVR_PlayBackByTime_V40(m_iLogID, struVod);

         // m_iPlaybackID = HCNetSDK.getInstance()
         // .NET_DVR_PlayBackByTime(m_iLogID, m_iStartChan,
         // struBegin, struEnd);
         if (m_iPlaybackID >= 0) {
             NET_DVR_PLAYBACK_INFO struPlaybackInfo = null;
         if (!HCNetSDK
                 .getInstance()
                 .NET_DVR_PlayBackControl_V40(
                                            m_iPlaybackID, PlaybackControlCommand.NET_DVR_PLAYSTART,null, 0, struPlaybackInfo)) {
                    Log.e(TAG, "net sdk playback start failed!");
                return;
         }
         m_bStopPlayback = false;
         m_oPlaybackBtn.setText("Stop");

         Thread thread = new Thread() {
         public void run() {
            int nProgress = -1;
                while (true) {
                         nProgress = HCNetSDK.getInstance()
                         .NET_DVR_GetPlayBackPos(
                         m_iPlaybackID);
                         System.out.println("NET_DVR_GetPlayBackPos:" + nProgress);
                         if (nProgress < 0 || nProgress >= 100) {
                            break;
                         }

                         try {
                             Thread.sleep(1000);
                         } catch (InterruptedException e) {
                         // TODO Auto-generated catch block
                            e.printStackTrace();
                         }
                }
         }
         };
         thread.start();
         } else {
             Log.i(TAG,
                "NET_DVR_PlayBackByTime failed, error code: "
                     + HCNetSDK.getInstance()
                     .NET_DVR_GetLastError());
         }
         } else {
     m_bStopPlayback = true;
     if (!HCNetSDK.getInstance().NET_DVR_StopPlayBack(
        m_iPlaybackID)) {
        Log.e(TAG, "net sdk stop playback failed");
     }
         m_oPlaybackBtn.setText("Playback");
         m_iPlaybackID = -1;

     ChangeSingleSurFace(false);
     }
     } catch (Exception err) {
     Log.e(TAG, "error: " + err.toString());
     }
     }
     };

//    private Button.OnClickListener Playback_Listener = new Button.OnClickListener() {
//
//        public void onClick(View v) {
//            try {
//                if (m_iLogID < 0) {
//                    Log.e(TAG, "please login on a device first");
//                    return;
//                }
//                if (m_iPlaybackID < 0) {
//                    if (m_iPlayID >= 0) {
//                        Log.i(TAG, "Please stop preview first");
//                        return;
//                    }
//
//                    NET_DVR_TIME struBegin = new NET_DVR_TIME();
//                    NET_DVR_TIME struEnd = new NET_DVR_TIME();
//
//                    struBegin.dwYear = 2019;
//                    struBegin.dwMonth = 4;
//                    struBegin.dwDay = 26;
//                    struBegin.dwHour = 9;
//                    struBegin.dwMinute= 41;
//                    struBegin.dwSecond = 30;
//
//                    struEnd.dwYear = 2019;
//                    struEnd.dwMonth = 4;
//                    struEnd.dwDay = 26;
//                    struEnd.dwHour = 10;
//                    struEnd.dwMinute= 48;
//                    struEnd.dwSecond = 20;
//
//                    ChangeSingleSurFace(true);
//                    m_iPlaybackID = HCNetSDK.getInstance().NET_DVR_PlayBackByTime(m_lUserID, Integer.valueOf(m_oCam.getText().toString()), struBegin, struEnd);
//                            /*.NET_DVR_PlayBackByName(m_iLogID,
//                                    new String("ch0002_00010000459000200"), playView[0].getHolder().getSurface());*/
//                    if (m_iPlaybackID >= 0) {
//                        NET_DVR_PLAYBACK_INFO struPlaybackInfo = null;
//                        if (!HCNetSDK
//                                .getInstance()
//                                .NET_DVR_PlayBackControl_V40(
//                                        m_iPlaybackID,
//                                        PlaybackControlCommand.NET_DVR_PLAYSTART,
//                                        null, 0, struPlaybackInfo)) {
//                            Log.e(TAG, "net sdk playback start failed!");
//                            return;
//                        }
//                        m_bStopPlayback = false;
//                        m_oPlaybackBtn.setText("Stop");
//
//                        Thread thread = new Thread() {
//                            public void run() {
//                                int nProgress = -1;
//                                while (true) {
//                                    nProgress = HCNetSDK.getInstance()
//                                            .NET_DVR_GetPlayBackPos(
//                                                    m_iPlaybackID);
//                                    System.out
//                                            .println("NET_DVR_GetPlayBackPos:"
//                                                    + nProgress);
//                                    if (nProgress < 0 || nProgress >= 100) {
//                                        break;
//                                    }
//                                    try {
//                                        Thread.sleep(1000);
//                                    } catch (InterruptedException e) { // TODO
//                                                                       // Auto-generated
//                                                                       // catch
//                                                                       // block
//                                        e.printStackTrace();
//                                    }
//
//                                }
//                            }
//                        };
//                        thread.start();
//                    } else {
//                        Log.i(TAG,
//                                "NET_DVR_PlayBackByName failed, error code: "
//                                        + HCNetSDK.getInstance()
//                                                .NET_DVR_GetLastError());
//                    }
//                } else {
//                    m_bStopPlayback = true;
//                    if (!HCNetSDK.getInstance().NET_DVR_StopPlayBack(
//                            m_iPlaybackID)) {
//                        Log.e(TAG, "net sdk stop playback failed");
//                    } // player stop play
//                    m_oPlaybackBtn.setText("Playback");
//                    m_iPlaybackID = -1;
//
//                    ChangeSingleSurFace(false);
//                }
//            } catch (Exception err) {
//                Log.e(TAG, "error: " + err.toString());
//            }
//        }
//    };

    // login listener
    private Button.OnClickListener Login_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            try {
                if (m_iLogID < 0) {
                    // login on the device
                    m_iLogID = loginDevice();
                    if (m_iLogID < 0) {
                        Log.e(TAG, "This device logins failed!");
                        return;
                    } else {
                        System.out.println("m_iLogID=" + m_iLogID);
                    }
                    // get instance of exception callback and set
                    ExceptionCallBack oexceptionCbf = getExceptiongCbf();
                    if (oexceptionCbf == null) {
                        Log.e(TAG, "ExceptionCallBack object is failed!");
                        return;
                    }

                    if (!HCNetSDK.getInstance().NET_DVR_SetExceptionCallBack(
                            oexceptionCbf)) {
                        Log.e(TAG, "NET_DVR_SetExceptionCallBack is failed!");
                        return;
                    }

                    m_oLoginBtn.setText("Logout");
                    Log.i(TAG,
                            "Login sucess ****************************1***************************");
                } else {
                    // whether we have logout
                    if (!HCNetSDK.getInstance().NET_DVR_Logout_V30(m_iLogID)) {
                        Log.e(TAG, " NET_DVR_Logout is failed!");
                        return;
                    }
                    m_oLoginBtn.setText("Login");
                    m_iLogID = -1;
                }
            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
            }
        }
    };
    // Preview listener
    private Button.OnClickListener Preview_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            try {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(DemoActivity.this
                                .getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                if (m_iLogID < 0) {
                    Log.e(TAG, "please login on device first");
                    return;
                }

                if (m_iPlaybackID >= 0) {
                    Log.i(TAG, "Please stop palyback first");
                    return;
                }

                if (m_bNeedDecode) {
                    if (m_iChanNum > 1)// preview more than a channel
                    {
                        //CAMERA INDEX

                        if (!m_bMultiPlay) {
                            startMultiPreview(4);
                            // startMultiPreview();
                            m_bMultiPlay = true;
                            m_oPreviewBtn.setText("Stop");
                        } else {
                            stopMultiPreview();
                            m_bMultiPlay = false;
                            m_oPreviewBtn.setText("Preview");
                        }
                    } else // preivew a channel
                    {
                        if (m_iPlayID < 0) {
                            startSinglePreview();
                        } else {
                            stopSinglePreview();
                            m_oPreviewBtn.setText("Preview");
                        }
                    }
                } else {

                }
            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
            }
        }
    };
    // configuration listener
    private Button.OnClickListener ParamCfg_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            try {
                paramCfg(m_iLogID);
            } catch (Exception err) {
                Log.e(TAG, "error: " + err.toString());
            }
        }
    };

    private void startSinglePreview() {
        if (m_iPlaybackID >= 0) {
            Log.i(TAG, "Please stop palyback first");
            return;
        }

        Log.i(TAG, "m_iStartChan:" + getM_iStartChan());

        NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
        previewInfo.lChannel = getM_iStartChan();
        previewInfo.dwStreamType = 0; // substream
        previewInfo.bBlocked = 1;
        previewInfo.hHwnd = playView[0].getHolder();
        // HCNetSDK start preview
        m_iPlayID = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(m_iLogID,
                previewInfo, null);
        if (m_iPlayID < 0) {
            Log.e(TAG, "NET_DVR_RealPlay is failed!Err:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }

        Log.i(TAG,
                "NetSdk Play sucess ***********************3***************************");
        m_oPreviewBtn.setText("Stop");
    }

    private void startMultiPreview(int index) {

        for (int i = 0; i < index; i++) {
            playView[i].startPreview(m_iLogID, m_iStartChan + i);
        }

        // new Thread(new Runnable() {
        //
        // @Override
        // public void run() {
        // // TODO Auto-generated method stub
        // for (int i = 0; i < 4; i++) {
        // while (!playView[i].bCreate) {
        // try {
        // Thread.sleep(100);
        // Log.i(TAG, "wait for surface create");
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        //
        // NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
        // previewInfo.lChannel = m_iStartChan + i;
        // previewInfo.dwStreamType = 0; // substream
        // previewInfo.bBlocked = 1;
        // previewInfo.hHwnd = playView[i].getHolder();
        //
        // playView[i].m_iPreviewHandle =
        // HCNetSDK.getInstance().NET_DVR_RealPlay_V40(
        // m_iLogID, previewInfo, null);
        // if (playView[i].m_iPreviewHandle < 0) {
        // Log.e(TAG, "NET_DVR_RealPlay is failed!Err:"
        // + HCNetSDK.getInstance().NET_DVR_GetLastError());
        // }
        // }
        // }
        // }).start();

        m_iPlayID = playView[0].m_iPreviewHandle;
    }

    private void stopMultiPreview() {
        int i = 0;
        for (i = 0; i < 4; i++) {
            playView[i].stopPreview();
        }
        m_iPlayID = -1;
    }

    /**
     * @fn stopSinglePreview
     * @author zhuzhenlei
     * @brief stop preview
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return NULL
     */
    private void stopSinglePreview() {
        if (m_iPlayID < 0) {
            Log.e(TAG, "m_iPlayID < 0");
            return;
        }

        // net sdk stop preview
        if (!HCNetSDK.getInstance().NET_DVR_StopRealPlay(m_iPlayID)) {
            Log.e(TAG, "StopRealPlay is failed!Err:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }

        m_iPlayID = -1;
    }

    /**
     * @fn loginNormalDevice
     * @author zhuzhenlei
     * @brief login on device
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return login ID
     */
    private int loginNormalDevice() {
        // get instance
        m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        if (null == m_oNetDvrDeviceInfoV30) {
            Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
            return -1;
        }
        String strIP = m_oIPAddr.getText().toString();
        int nPort = Integer.parseInt(m_oPort.getText().toString());
        String strUser = m_oUser.getText().toString();
        String strPsd = m_oPsd.getText().toString();
        // call NET_DVR_Login_v30 to login on, port 8000 as default
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort,
                strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            Log.e(TAG, "NET_DVR_Login is failed!Err:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return -1;
        }
        if (m_oNetDvrDeviceInfoV30.byChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byChanNum;
        } else if (m_oNetDvrDeviceInfoV30.byIPChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartDChan;
            m_iChanNum = 1/*m_oNetDvrDeviceInfoV30.byIPChanNum
                    + m_oNetDvrDeviceInfoV30.byHighDChanNum * 256*/;
        }

        if (m_iChanNum > 1) {
            ChangeSingleSurFace(false);
        } else {
            ChangeSingleSurFace(true);
        }
        Log.i(TAG, "NET_DVR_Login is Successful!");

        return iLogID;
    }

    public static void Test_XMLAbility(int iUserID) {
        byte[] arrayOutBuf = new byte[64 * 1024];
        INT_PTR intPtr = new INT_PTR();
        String strInput = new String(
                "<AlarmHostAbility version=\"2.0\"></AlarmHostAbility>");
        byte[] arrayInBuf = new byte[8 * 1024];
        arrayInBuf = strInput.getBytes();
        if (!HCNetSDK.getInstance().NET_DVR_GetXMLAbility(iUserID,
                HCNetSDK.DEVICE_ABILITY_INFO, arrayInBuf, strInput.length(),
                arrayOutBuf, 64 * 1024, intPtr)) {
            System.out.println("get DEVICE_ABILITY_INFO faild!" + " err: "
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            System.out.println("get DEVICE_ABILITY_INFO succ!");
        }
    }

    /**
     * @fn loginEzvizDevice
     * @author liuyu6
     * @brief login on ezviz device
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return login ID
     */
    private int loginEzvizDevice() {
        return -1;
        /*
         * NET_DVR_OPEN_EZVIZ_USER_LOGIN_INFO struLoginInfo = new
         * NET_DVR_OPEN_EZVIZ_USER_LOGIN_INFO(); NET_DVR_DEVICEINFO_V30
         * struDeviceInfo = new NET_DVR_DEVICEINFO_V30();
         * 
         * //String strInput = new String("pbsgp.p2papi.ezviz7.com"); String
         * strInput = new String("open.ys7.com"); //String strInput = new
         * String("pbdev.ys7.com"); //String strInput = new
         * String("183.136.184.67"); byte[] byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sEzvizServerAddress, 0,
         * byInput.length);
         * 
         * struLoginInfo.wPort = 443;
         * 
         * strInput = new
         * String("at.43anfq0q9k8zt06vd0ppalfhc4bj177p-3k4ovrh4vu-105zgp6-jgt8edqst"
         * ); byInput = strInput.getBytes(); System.arraycopy(byInput, 0,
         * struLoginInfo.sAccessToken, 0, byInput.length);
         * 
         * //strInput = new String("67a7daedd4654dc5be329f2289914859");
         * //byInput = strInput.getBytes(); //System.arraycopy(byInput, 0,
         * struLoginInfo.sSessionID, 0, byInput.length);
         * 
         * //strInput = new String("ae1b9af9dcac4caeb88da6dbbf2dd8d5"); strInput
         * = new String("com.hik.visualintercom"); byInput =
         * strInput.getBytes(); System.arraycopy(byInput, 0,
         * struLoginInfo.sAppID, 0, byInput.length);
         * 
         * //strInput = new String("78313dadecd92bd11623638d57aa5128"); strInput
         * = new String("226f102a99ad0e078504d380b9ddf760"); byInput =
         * strInput.getBytes(); System.arraycopy(byInput, 0,
         * struLoginInfo.sFeatureCode, 0, byInput.length);
         * 
         * //strInput = new
         * String("https://pbopen.ys7.com:443/api/device/transmission");
         * strInput = new String("/api/device/transmission"); byInput =
         * strInput.getBytes(); System.arraycopy(byInput, 0, struLoginInfo.sUrl,
         * 0, byInput.length);
         * 
         * strInput = new String("520247131"); byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sDeviceID, 0,
         * byInput.length);
         * 
         * strInput = new String("2"); byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sClientType, 0,
         * byInput.length);
         * 
         * strInput = new String("UNKNOWN"); byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sNetType, 0,
         * byInput.length);
         * 
         * strInput = new String("5.0.1"); byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sOsVersion, 0,
         * byInput.length);
         * 
         * strInput = new String("v.5.1.5.30"); byInput = strInput.getBytes();
         * System.arraycopy(byInput, 0, struLoginInfo.sSdkVersion, 0,
         * byInput.length);
         * 
         * int iUserID = -1;
         * 
         * iUserID =
         * HCNetSDK.getInstance().NET_DVR_CreateOpenEzvizUser(struLoginInfo,
         * struDeviceInfo);
         * 
         * if (-1 == iUserID) { System.out.println("NET_DVR_CreateOpenEzvizUser"
         * + " err: " + HCNetSDK.getInstance().NET_DVR_GetLastError()); return
         * -1; } else {
         * System.out.println("NET_DVR_CreateOpenEzvizUser success"); }
         * 
         * Test_XMLAbility(iUserID); Test_XMLAbility(iUserID);
         * Test_XMLAbility(iUserID);
         * 
         * return iUserID;
         */

    }

    /**
     * @fn loginDevice
     * @author zhangqing
     * @brief login on device
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return login ID
     */
    private int loginDevice() {
        int iLogID = -1;

        iLogID = loginNormalDevice();

        // iLogID = JNATest.TEST_EzvizLogin();
        // iLogID = loginEzvizDevice();

        return iLogID;
    }

    /**
     * @fn paramCfg
     * @author zhuzhenlei
     * @brief configuration
     * @param iUserID
     *            - login ID [in]
     * @param NULL
     *            [out]
     * @return NULL
     */
    private void paramCfg(final int iUserID) {
        // whether have logined on
        if (iUserID < 0) {
            Log.e(TAG, "iUserID < 0");
            return;
        }

        NET_DVR_COMPRESSIONCFG_V30 struCompress = new NET_DVR_COMPRESSIONCFG_V30();
        if (!HCNetSDK.getInstance().NET_DVR_GetDVRConfig(iUserID,
                HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30, m_iStartChan,
                struCompress)) {
            Log.e(TAG, "NET_DVR_GET_COMPRESSCFG_V30 failed with error code:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Log.i(TAG, "NET_DVR_GET_COMPRESSCFG_V30 succ");
        }
        // set substream resolution to cif
        struCompress.struNetPara.byResolution = 1;
        if (!HCNetSDK.getInstance().NET_DVR_SetDVRConfig(iUserID,
                HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30, m_iStartChan,
                struCompress)) {
            Log.e(TAG, "NET_DVR_SET_COMPRESSCFG_V30 failed with error code:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Log.i(TAG, "NET_DVR_SET_COMPRESSCFG_V30 succ");
        }
    }

    /**
     * @fn getExceptiongCbf
     * @author zhuzhenlei
     * @brief process exception
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return exception instance
     */
    private ExceptionCallBack getExceptiongCbf() {
        ExceptionCallBack oExceptionCbf = new ExceptionCallBack() {
            public void fExceptionCallBack(int iType, int iUserID, int iHandle) {
                System.out.println("recv exception, type:" + iType);
            }
        };
        return oExceptionCbf;
    }

    /**
     * @fn Cleanup
     * @author zhuzhenlei
     * @brief cleanup
     * @param NULL
     *            [in]
     * @param NULL
     *            [out]
     * @return NULL
     */
    public void Cleanup() {
        // release net SDK resource
        HCNetSDK.getInstance().NET_DVR_Cleanup();
    }
    public String executeThroughSocket(int portNo, String portAddress) throws IOException {

        StringBuilder responseString = new StringBuilder();

        BufferedReader bufferedReader = null;
        Socket clientSocket = null;

        try {
            clientSocket = new Socket(portAddress, portNo);
            if (!clientSocket.isConnected())
                throw new SocketException("Could not connect to Socket");

            clientSocket.setKeepAlive(true);

            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                responseString.append(str);
            }

        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
            if (clientSocket != null)
                clientSocket.close();
        }
        return responseString.toString();
    }
    public class SocketClient extends AsyncTask<String, Void, BufferedReader> {

        private Socket socket = null;
        private BufferedReader dataInComing = null;

        StringBuilder responseString = new StringBuilder();

        public BufferedReader SetConnection(String ip, int port){

            try {

                socket = new Socket(ip, port);

                if(!socket.isConnected())
                    throw  new SocketException("Could not connect to Socket");

                dataInComing = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                return dataInComing;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected BufferedReader doInBackground(String... strings) {
            SetConnection(strings[0], Integer.valueOf(strings[1]));
            return dataInComing;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(BufferedReader dataInComing) {
            super.onPostExecute(dataInComing);
            if (dataInComing == null) {
                return;
            }
            m_dDataIn = dataInComing;

            try {
//                String data = m_dDataIn.readLine();
//                JSONObject json = new JSONObject(m_dDataIn.readUTF());
                String str;
                while ((str = m_dDataIn.readLine()) != null) {
                    responseString.append(str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (m_dDataIn != null) {
                    try {
                        m_dDataIn.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
