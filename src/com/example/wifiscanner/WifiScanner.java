package com.example.wifiscanner;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.wifiscanner.ProviderLocationTracker.ProviderType;
import com.throughput.TCPSender;
import com.udpmeasurement.MeasurementError;
import com.udpmeasurement.SimpleUDPReceiver;
import com.udpmeasurement.SimpleUDPSender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class WifiScanner extends Activity {

	// TextView textview;
	ListView list;
	String []listInfo;
	ArrayAdapter<String> adapter;
	Button buttonLTE, buttonUDP, buttonWifi, buttonThput, buttonTCP, buttonLoc;
	String text;
	WifiManager wifiManager;
	TelephonyManager telephonyManager;
	//ProviderLocationTracker locationTracker;
	String[] wifis;
	Writer writer;
	Handler handler = null;
	Runnable runable = null;
	boolean isLTERun = false;
	boolean isUDPRun = false;
	boolean isWifiRun = false;
	boolean isThroughputRun = false;
	boolean isTCPRun = false;
	boolean isLocRun = false;
	CellInfoMonitor cellInfoMonitor = null;
	ConnectivityMonitor conMonitor = null;
	WifiMonitor wifiMonitor = null;
	WifiScan wifiScan = null;
	SimpleUDPReceiver simpleUDPReceiver = null;
	SimpleUDPSender simpleUDPSender = null;
	ThroughputMonitor throuMonitor = null;
	TCPSender tcpSender = null;
	GoogleLocationTracker locationTracker =null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// textview = (TextView)findViewById(R.id.textView);
		list = (ListView) findViewById(R.id.listView1);
		buttonLTE = (Button) findViewById(R.id.buttonLTE);
		buttonLTE.setOnClickListener(onClickLTEListener);
		buttonUDP = (Button) findViewById(R.id.buttonUDP);
		buttonUDP.setOnClickListener(onClickUDPListener);
		buttonWifi = (Button) findViewById(R.id.buttonWifi);
		buttonWifi.setOnClickListener(onClickWifiListener);
		buttonThput = (Button) findViewById(R.id.buttonThroughput);
		buttonThput.setOnClickListener(onClickThroughputListener);
		buttonTCP = (Button) findViewById(R.id.buttonTCP);
		buttonTCP.setOnClickListener(onClickTCPListener);
		buttonLoc = (Button) findViewById(R.id.buttonLoc);
		buttonLoc.setOnClickListener(onClickLocListener);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//		locationTracker = new ProviderLocationTracker(getApplicationContext(),
//				ProviderType.GPS);
//		locationTracker.start();
		text = "";
		
		listInfo = new String[3];
		listInfo[0]="";listInfo[1]="";listInfo[2]="";
		adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, listInfo);
		list.setAdapter(adapter);
		
		handler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				Bundle bundle = msg.getData();
				String type = bundle.getString("type");
				if(type.equals("cellInfo")||type.equals("wifiInfo")){
					listInfo[0]=bundle.getString("data");
					adapter.notifyDataSetChanged();
				}else if(type.equals("receivePacket")){
					listInfo[1]=bundle.getString("data");
					adapter.notifyDataSetChanged();
				}else if(type.equals("sendPacket")){
					listInfo[2]=bundle.getString("data");
					adapter.notifyDataSetChanged();
				}
				
			}
		};
	}

//	private String getCurrentLocation() {
//		Location location = locationTracker.getLocation();
//		if (location == null)
//			return "Location: null\n";
//		return "Location: " + location.getAltitude() + ","
//				+ location.getLatitude() + "," + location.getLongitude() + "\n";
//	}

	
	private OnClickListener onClickThroughputListener = new OnClickListener(){
		@Override
		public void onClick(final View v) {
			if (!isThroughputRun){
				try {
					throuMonitor = new ThroughputMonitor(handler, getApplicationContext());
					throuMonitor.start();
					isThroughputRun = true;
					buttonThput.setText("StopThr");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				throuMonitor.terminate();
				isThroughputRun = false;
				throuMonitor = null;
				buttonThput.setText("StartThr");
			}
		}
	};
	
	private OnClickListener onClickUDPListener = new OnClickListener(){
		@Override
		public void onClick(final View v) {
			if(!isUDPRun){
				isUDPRun = true;
				buttonUDP.setText("StopUDP");
				try {
					simpleUDPReceiver = new SimpleUDPReceiver(handler);
					simpleUDPSender = new SimpleUDPSender(handler);
					simpleUDPSender.start();
					simpleUDPReceiver.start();
				} catch (MeasurementError e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				isUDPRun = false;
				buttonUDP.setText("StartUDP");
				if (simpleUDPReceiver!= null){
					simpleUDPReceiver.terminate();
					simpleUDPReceiver=null;
				}
				if (simpleUDPSender!= null){
					simpleUDPSender.terminate();
					simpleUDPSender=null;
				}
			}
		}
	};
	private OnClickListener onClickTCPListener= new OnClickListener(){
		@Override
		public void onClick(final View v){
			if(!isTCPRun){
				tcpSender = new TCPSender();
				tcpSender.start();
				buttonTCP.setText("StopTCP");
				isTCPRun = true;
			}else{
				tcpSender.terminate();
				buttonTCP.setText("StartTCP");
				isTCPRun = false;
			}
		}
	};
	
	private OnClickListener onClickWifiListener = new OnClickListener(){
		@Override
		public void onClick(final View v){
			if(!isWifiRun){
				isWifiRun = true;
				buttonWifi.setText("StopWifi");
				try {
					conMonitor = new ConnectivityMonitor(handler, getApplicationContext());
					conMonitor.start();
					//wifiScan = new WifiScan(handler, getApplicationContext());
					//wifiScan.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				isWifiRun = false;
				buttonWifi.setText("StartWifi");
				if (conMonitor!=null){
					conMonitor.terminate();
					conMonitor=null;
				}
//				if (wifiScan!= null){
//					wifiScan.terminate();
//					wifiScan=null;
//				}
			}
		}
	};
	
	private OnClickListener onClickLocListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			if (!isLocRun){
				isLocRun = true;
				buttonLoc.setText("StopLoc");
				try {
					locationTracker = new GoogleLocationTracker(handler, getApplicationContext());
					locationTracker.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				isLocRun = false;
				buttonLoc.setText("StartLoc");
				if (locationTracker!=null){
					locationTracker.terminate();
					locationTracker=null;
				}
			}
		}
	};
	
	private OnClickListener onClickLTEListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			if(!isLTERun){
				isLTERun = true;
				buttonLTE.setText("StopLTE");
				try {
					cellInfoMonitor = new CellInfoMonitor(handler, getApplicationContext());
					cellInfoMonitor.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				isLTERun = false;
				buttonLTE.setText("StartLTE");
				if (cellInfoMonitor!= null){
					cellInfoMonitor.terminate();
					cellInfoMonitor=null;
				}
			}
			// try {
			// Method[] methods = android.telephony.SignalStrength.class
			// .getMethods();
			// for (Method mthd : methods) {
			// if (mthd.getName().equals("getLteSignalStrength")
			// || mthd.getName().equals("getLteRsrp")
			// || mthd.getName().equals("getLteRsrq")
			// || mthd.getName().equals("getLteRssnr")
			// || mthd.getName().equals("getLteCqi")) {
			// Log.i(LOG_TAG,
			// "onSignalStrengthsChanged: " + mthd.getName() + " "
			// + mthd.invoke(signalStrength));
			// }
			// }
			// } catch (SecurityException e) {
			// e.printStackTrace();
			// } catch (IllegalArgumentException e) {
			// e.printStackTrace();
			// } catch (IllegalAccessException e) {
			// e.printStackTrace();
			// } catch (InvocationTargetException e) {
			// e.printStackTrace();
			// }
			// Reflection code ends here
		}
		// int type =telephonyManager.getNetworkType();

		// switch(type){
		// case TelephonyManager.NETWORK_TYPE_LTE:
		// wifis[0]="NETWORK_TYPE_LTE";
		// break;
		// case TelephonyManager.NETWORK_TYPE_UMTS:
		// wifis[0]="NETWORK_TYPE_UMTS";
		// break;
		// default:
		// wifis[0]=""+type;
		// }
		// //wifis[0] = " "+wifiManager.getConnectionInfo().getRssi();
		// /*List<ScanResult> wifiScanList = wifiManager.getScanResults();
		// wifis = new String[wifiScanList.size()];
		// text = "";
		// for(int i = 0; i < wifiScanList.size(); i++){
		// ScanResult result = wifiScanList.get(i);
		// wifis[i] ="";
		// wifis[i] += ("SSID: "+result.SSID+", ");
		// wifis[i] += ("BSSID: "+result.BSSID+", ");
		// wifis[i] += ("level: "+result.level+", ");
		// wifis[i] += ("frequency: "+result.frequency+", ");
		// //wifis[i] += ("timestamp: "+result.timestamp);
		// text +=wifis[i];
		// text+="\n";
		// }*/
		// list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
		// android.R.layout.simple_list_item_1,wifis));
		// }
	};
}
