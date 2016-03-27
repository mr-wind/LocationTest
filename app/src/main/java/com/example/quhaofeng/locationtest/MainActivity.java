package com.example.quhaofeng.locationtest;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int SHOW_LOCATION = 0;

    private TextView positionTextView;

    private LocationManager locationManager;

    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        positionTextView = (TextView) findViewById(R.id.position_text_view);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            //当没有可用的位置提供器的时候，弹出Toast提示用户
            Toast.makeText(this, "NO location provider to use", Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if ((locationManager != null) && (checkPermission(LOCATION_SERVICE, 1 , 0) != PackageManager.PERMISSION_GRANTED)){
            //显示当前设备的位置信息
            showLocation(location);
    }

        locationManager.requestLocationUpdates(provider, 5000, 1, locationListener);
    }


    protected void onDestroy(){
        super.onDestroy();

        if ((locationManager != null) && (checkPermission(LOCATION_SERVICE, 1 , 0) != PackageManager.PERMISSION_GRANTED)){
            locationManager.removeUpdates(locationListener);
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //更新当前设备位置信息
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void showLocation(final Location location) {
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    StringBuilder url = new StringBuilder();
                    url.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
                    url.append(location.getLatitude()).append(",");
                    url.append(location.getLongitude());
                    url.append("&region=zh&sensor=false");
                    URL address = new URL(url.toString());
                    HttpURLConnection connection = null;
                    connection = (HttpURLConnection)address.openConnection();
                    connection.addRequestProperty("Accept-Language","zh-CN");//消息头，表示接收中文返回值
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    Log.w("MainActivity", "" + response.toString());
                    parseJSONWithJSONObject(response.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();*/
        String currentPosition = "latitude is " + location.getLatitude() + "\n" + "longitude is " + location.getLongitude();
        positionTextView.setText(currentPosition);
    }


    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SHOW_LOCATION:
                    String currentPosition = (String)msg.obj;
                    positionTextView.setText(currentPosition);
                    break;
                default:
                    break;
            }
        }
    };

    private void parseJSONWithJSONObject(String jsonData){
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray resultArray = jsonObject.getJSONArray("results");
            if (resultArray.length() > 0){
                JSONObject subObject = resultArray.getJSONObject(0);
                String address1 = subObject.getString("formatted_address");
                Message message = new Message();
                message.what = SHOW_LOCATION;
                message.obj = address1;
                handler.sendMessage(message);
                Log.w("MainActivity", "address:" + address1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
