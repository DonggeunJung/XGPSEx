package com.example.xgpsex;

import android.app.FragmentManager;
import android.bluetooth.BluetoothDevice;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.namsung.xgpsmanager.XGPSListener;
import com.namsung.xgpsmanager.XGPSManager;
import com.namsung.xgpsmanager.data.SatellitesInfo;
import com.namsung.xgpsmanager.utils.Constants;
import com.namsung.xgpsmanager.data.LogBulkData;
import com.namsung.xgpsmanager.data.LogData;
import com.namsung.xgpsmanager.utils.DLog;

public class MainActivity extends AppCompatActivity
        implements XGPSListener, OnMapReadyCallback {

    protected XGPSManager xgpsManager = null;

    TextView tv_state;
    TextView tv_Latitude;
    TextView tv_Longitude;
    TextView tv_Altitude;
    TextView tv_BatteryLevel;
    TextView tv_Heading;
    TextView tv_Speed;
    TextView tv_UTC;

    GoogleMap googleMap = null;
    //public static MainActivity mThis = null;
    LatLng newPos = null;
    Marker mMarkerMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mThis = this;

        tv_state = (TextView)findViewById(R.id.tv_state);
        tv_Latitude = (TextView)findViewById(R.id.tv_Latitude);
        tv_Longitude = (TextView)findViewById(R.id.tv_Longitude);
        tv_Altitude = (TextView)findViewById(R.id.tv_Altitude);
        tv_BatteryLevel = (TextView)findViewById(R.id.tv_BatteryLevel);
        tv_Heading = (TextView)findViewById(R.id.tv_Heading);
        tv_Speed = (TextView)findViewById(R.id.tv_Speed);
        tv_UTC = (TextView)findViewById(R.id.tv_UTC);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

    @Override
    public void onMapReady(final GoogleMap map) {
        googleMap = map;
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
    }

    @Override
    public void updateLocationInfo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String strLat = xgpsManager.getLatitude(XGPSManager.MODE_POSITION_DEGREE);
                tv_Latitude.setText(strLat);
                String strLng = xgpsManager.getLongitude(XGPSManager.MODE_POSITION_DEGREE);
                tv_Longitude.setText(strLng);
                tv_Altitude.setText(xgpsManager.getAltitude(Constants.MODE_ALTITUDE_FEET));
                double bl = xgpsManager.getBatteryLevel()*100.f;
                tv_BatteryLevel.setText( (int)bl + "");
                tv_Heading.setText(xgpsManager.getHeadingString());
                tv_Speed.setText(xgpsManager.getSpeed(Constants.MODE_SPEED_KPH));
                tv_UTC.setText(xgpsManager.getUTC());

                boolean bFirst = false;
                if( newPos == null )
                    bFirst = true;
                newPos = getLatLng(strLat, strLng);
                if( bFirst )
                    initMap(newPos);
                else
                    setLatLng_Map(newPos);
            }
        });
    }

    protected LatLng getLatLng(String strLat, String strLng) {
        double lat = 34.0;
        double lng = 84.0;
        int len = strLat.length();
        if( len > 3 ) {
            double minus = 1.0;
            if( strLat.charAt(len-1) != 'N' )
                minus = -1.0;
            strLat = strLat.substring(0, len - 2);
            lat = Double.parseDouble(strLat);
            lat *= minus;
        }
        len = strLng.length();
        if( len > 3 ) {
            double minus = 1.0;
            if( strLng.charAt(len-1) == 'W' )
                minus = -1.0;
            strLng = strLng.substring(0, len - 2);
            lng = Double.parseDouble(strLng);
            lng *= minus;
        }
        return new LatLng(lat, lng);
    }

    protected void setLatLng_Map(LatLng pos) {
        //LatLng pos = new LatLng(lat, lng);
        if( googleMap != null ) {
            mMarkerMan.setPosition(pos);
            // 지도 중심좌표 위치를 갱신
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        }
    }

    private void initMap(LatLng pos) {
        if( googleMap == null )
            return;

        MarkerOptions moStart = new MarkerOptions();
        moStart.position(pos);
        moStart.title("My location");
        mMarkerMan = googleMap.addMarker(moStart);
        mMarkerMan.showInfoWindow();

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }

}
