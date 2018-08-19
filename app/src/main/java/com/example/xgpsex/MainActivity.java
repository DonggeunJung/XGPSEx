package com.example.xgpsex;

import android.bluetooth.BluetoothDevice;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.namsung.xgpsmanager.XGPSListener;
import com.namsung.xgpsmanager.XGPSManager;
import com.namsung.xgpsmanager.data.SatellitesInfo;
import com.namsung.xgpsmanager.utils.Constants;
import com.namsung.xgpsmanager.data.LogBulkData;
import com.namsung.xgpsmanager.data.LogData;
import com.namsung.xgpsmanager.utils.DLog;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements XGPSListener {

    protected XGPSManager xgpsManager = null;

    TextView tv_state;
    TextView tv_Latitude;
    TextView tv_Longitude;
    TextView tv_Altitude;
    TextView tv_BatteryLevel;
    TextView tv_Heading;
    TextView tv_Speed;
    TextView tv_UTC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_state = (TextView)findViewById(R.id.tv_state);
        tv_Latitude = (TextView)findViewById(R.id.tv_Latitude);
        tv_Longitude = (TextView)findViewById(R.id.tv_Longitude);
        tv_Altitude = (TextView)findViewById(R.id.tv_Altitude);
        tv_BatteryLevel = (TextView)findViewById(R.id.tv_BatteryLevel);
        tv_Heading = (TextView)findViewById(R.id.tv_Heading);
        tv_Speed = (TextView)findViewById(R.id.tv_Speed);
        tv_UTC = (TextView)findViewById(R.id.tv_UTC);

        connect();
    }

    @Override
    public void connecting(final BluetoothDevice device) {
        tv_state.setText("connecting...");
    }

    // start XGPSListner
    @Override
    public void connected(final boolean isConnect, final int error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isConnect) {
                    tv_state.setText("connect failed");
                } else {
                    tv_state.setText("connected");
                }
            }
        });
    }

    @Override
    public void updateSatellitesInfo() {
    }

    @Override
    public void throwException(Exception e) {
        if (e.getClass() == SecurityException.class) {
            DLog.e(e.getMessage());
        }
    }

    @Override
    public void updateSettings(final boolean positionEnable, final boolean overWrite) {
    }

    @Override
    public void getLogDetailComplete(final ArrayList<LogBulkData> logBulkList) {
    }

    @Override
    public void getLogDetailProgress(final int bulkCount) {
    }

    @Override
    public void getLogListComplete(final ArrayList<LogData> logList) {
    }

    @Override
    public void updateLocationInfo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_Latitude.setText(xgpsManager.getLatitude(XGPSManager.MODE_POSITION_DEGREE));
                tv_Longitude.setText(xgpsManager.getLongitude(XGPSManager.MODE_POSITION_DEGREE));
                tv_Altitude.setText(xgpsManager.getAltitude(Constants.MODE_ALTITUDE_FEET));
                double bl = xgpsManager.getBatteryLevel()*100.f;
                tv_BatteryLevel.setText( (int)bl + "");
                tv_Heading.setText(xgpsManager.getHeadingString());
                tv_Speed.setText(xgpsManager.getSpeed(Constants.MODE_SPEED_KPH));
                tv_UTC.setText(xgpsManager.getUTC());
            }
        });
    }

    @Override
    protected void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    public void connect() {
        xgpsManager = new XGPSManager(this, this);
    }

    public void disconnect() {
        if( xgpsManager == null )
            return;
        Debug.stopMethodTracing();
        xgpsManager.setMockEnable(false);
        xgpsManager.onDestroy();
    }

    public void onBtnReconnect(View v) {
        disconnect();
        connect();
    }

}
