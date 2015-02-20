package com.example.wifiscanner;

import java.io.*;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.throughput.TCPSender;
import com.udpmeasurement.MeasurementError;
import com.udpmeasurement.SimpleUDPEchoClient;
import com.udpmeasurement.SimpleUDPReceiver;
import com.udpmeasurement.SimpleUDPSender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.app.PendingIntent;

public class PhoneStateScanner extends Activity {

	private AlarmManager alarmManager;
	private WifiManager wifiManager;
	private TelephonyManager telephonyManager;

	private GoogleApiClient googleApiClient;

	private HashMap<String, PendingIntent> pendingIntents;

	private PrintWriter logFile;

	private boolean isWifiRunning = false;
	private boolean isUDPRunning = false;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		startService(new Intent(this, PhoneStateScanService.class));
		Button b = (Button) findViewById(R.id.buttonWifi);
		b.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View v) {
				Button b = (Button) v;
				String text = b.getText().toString();
				Intent intent = new Intent();
				if (!isWifiRunning) {
					isWifiRunning = true;
					text = text.replace("start", "stop");
					b.setText(text);
					intent.setAction("PhoneStateScan_startWifi");
					sendBroadcast(intent);
				}else{
					isWifiRunning = false;
					text = text.replace("stop", "start");
					b.setText(text);
					intent.setAction("PhoneStateScan_stopWifi");
					sendBroadcast(intent);
				}
			}});
		

		b = (Button) findViewById(R.id.buttonUDPEcho);
		b.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View v) {
				Button b = (Button) v;
				String text = b.getText().toString();
				Intent intent = new Intent();
				if (!isUDPRunning) {
					isUDPRunning = true;
					text = text.replace("start", "stop");
					b.setText(text);
					intent.setAction("PhoneStateScan_startUDP");
					sendBroadcast(intent);
				}else{
					isUDPRunning = false;
					text = text.replace("stop", "start");
					b.setText(text);
					intent.setAction("PhoneStateScan_stopUDP");
					sendBroadcast(intent);
				}
			}});
		
		
	}


	
	@Override
	protected void onResume(){
		super.onResume();
		Button b = null;
		if (isWifiRunning){
			b = ((Button) findViewById(R.id.buttonWifi));
			b.setText(b.getText().toString().replace("start","stop"));
		}
		if (isUDPRunning){
			b = ((Button) findViewById(R.id.buttonUDPEcho));
			b.setText(b.getText().toString().replace("start","stop"));
		}
	}
	
}