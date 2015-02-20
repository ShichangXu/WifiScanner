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
import android.app.Service;
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
import android.os.IBinder;
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

public class PhoneStateScanService extends Service {

	private AlarmManager alarmManager;
	private WifiManager wifiManager;
	private TelephonyManager telephonyManager;

	private GoogleApiClient googleApiClient;

	private HashMap<String, PendingIntent> pendingIntents;

	private PrintWriter logFile;
	
	private boolean isStarted = false;
	BroadcastReceiver broadcastReceiver = null;
	WifiReceiver receiverWifi = null;
	ConnectivityReceiver conReceiver = null;
	
	BroadcastReceiver UIReceiver = null;
	
	private SimpleUDPEchoClient simpleUDPEchoClient = null;
	private WifiScan wifiScan = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		
	}

	private void startUDPEcho(){
		if(simpleUDPEchoClient == null){
			try {
				simpleUDPEchoClient = new SimpleUDPEchoClient(null);
				simpleUDPEchoClient.start();
			} catch (MeasurementError e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void stopUDPEcho(){
		if (simpleUDPEchoClient!= null){
			simpleUDPEchoClient.terminate();
			simpleUDPEchoClient = null;
		}
	}
	
	private void startWifiMonitor(){
		if (wifiScan == null) return;
		wifiScan = new WifiScan(getApplicationContext());
		wifiScan.start();
	}
	
	private void stopWifiMonitor(){
		if (simpleUDPEchoClient!= null){
		wifiScan.terminate();
		wifiScan = null;
		}
	}
	
	private void startThroughputMonitor(){
		String monitorName="Throu";
		PendingIntent intent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, new Intent(
						"getThroughput"),
						PendingIntent.FLAG_UPDATE_CURRENT);
		pendingIntents.put(monitorName, intent);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), 1 * 1000, intent);
	}
	
	private void stopThroughputMonitor(){
		String monitorName="Throu";
		alarmManager.cancel(pendingIntents.get(monitorName));
		pendingIntents.remove(monitorName);
	}
	
	private void startLocationMonitor(){
		String monitorName = "Loc";
		PendingIntent intent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, new Intent(
						"getLocation"),
						PendingIntent.FLAG_UPDATE_CURRENT);
		pendingIntents.put(monitorName, intent);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), 5 * 1000, intent);
	}
	
	private void stopLocationMonitor(){
		String monitorName="Loc";
		alarmManager.cancel(pendingIntents.get(monitorName));
		pendingIntents.remove(monitorName);
	}
	
	private void startCellMonitor(){
		String monitorName = "Cell";
		PendingIntent intent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, new Intent(
						"getCell"),
						PendingIntent.FLAG_UPDATE_CURRENT);
		pendingIntents.put(monitorName, intent);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), 1 * 1000, intent);
	}
	
	private void stopCellMonitor(){
		String monitorName="Cell";
		alarmManager.cancel(pendingIntents.get(monitorName));
		pendingIntents.remove(monitorName);
	}
	


	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			StringBuilder sb = new StringBuilder();
			WifiManager wifiManager = (WifiManager) c
					.getSystemService(Context.WIFI_SERVICE);
			List<ScanResult> wifiList = wifiManager.getScanResults();
			for (int i = 0; i < wifiList.size(); i++) {
				sb.append((wifiList.get(i)).toString());
				sb.append(";");
			}
			logInfo("wifiScan", sb.toString());
		}
	}

	class ConnectivityReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			StringBuilder sb = new StringBuilder();
			if (intent.getAction().equals(
					WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				NetworkInfo netInfo = (NetworkInfo) intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				sb.append("\t" + netInfo.getDetailedState().name() + "\t"
						+ netInfo.getState().name() + "\t"
						+ netInfo.getExtraInfo());
				if (netInfo.isConnected()) {
					sb.append("\tConnected to"
							+ ((WifiInfo) intent
									.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO))
									.toString());
				} else {
					sb.append("\tNot connected");
				}
			} else if (intent.getAction().equals(
					WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int currentState = intent.getIntExtra(
						WifiManager.EXTRA_WIFI_STATE, -1);
				int previousState = intent.getIntExtra(
						WifiManager.EXTRA_PREVIOUS_WIFI_STATE, -1);
				sb.append("\t" + currentState + "<--" + previousState);
			} else if (intent.getAction().equals(
					WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
				SupplicantState state = (SupplicantState) intent
						.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
				sb.append("\t" + state.name());
			} else if (intent.getAction().equals(
					ConnectivityManager.CONNECTIVITY_ACTION)) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					for (String key : extras.keySet()) {
						sb.append("\t" + key + ":" + extras.get(key));
					}
				}
			} else if (intent.getAction().equals(
					WifiManager.RSSI_CHANGED_ACTION)) {
				int currentRSSI = intent.getIntExtra(
						WifiManager.EXTRA_NEW_RSSI, 0);
				sb.append("\t" + currentRSSI);
			}
			logInfo(intent.getAction(), sb.toString());
		}
	}

	class TelephoneStateListener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			logInfo("cellSignalStrength", signalStrength.toString());
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			String[] stateName = new String[3];
			stateName[0] = "CALL_STATE_IDLE";
			stateName[1] = "CALL_STATE_RINGING";
			stateName[2] = "CALL_STATE_OFFHOOK";
			logInfo("callState", stateName[state]);
		}
	}

	public void logInfo(String type, String detail) {
		if (logFile != null) {
			logFile.println(System.currentTimeMillis() + "\t" + type + ":\t"
					+ detail);
			logFile.flush();
		}
		Log.i("StateScanner", System.currentTimeMillis() + "\t" + type + ":\t"
				+ detail);
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("StateScanner", "Received start id " + startId + ": " + intent);
        if (isStarted) return START_STICKY;
        isStarted = true;
        pendingIntents = new HashMap<String, PendingIntent>();
		alarmManager = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);

		googleApiClient = new GoogleApiClient.Builder(this).addApi(
				LocationServices.API).build();
		googleApiClient.connect();

		try {
			logFile = new PrintWriter(new BufferedWriter(new FileWriter(
					"sdcard/phoneInfo" + System.currentTimeMillis() + ".txt",
					true)));
		} catch (IOException e) {
			Log.i("StateScanner", "Fail to open logFile");
			logFile = null;
		}

		IntentFilter filter = new IntentFilter();
		
		filter.addAction("getThroughput");
		filter.addAction("getLocation");
		filter.addAction("getCell");

		broadcastReceiver = new BroadcastReceiver() {
			@SuppressLint("NewApi")
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals("getThroughput")) {
					long newRecvRxBytes = TrafficStats.getTotalRxBytes();
					long newSendTxBytes = TrafficStats.getTotalTxBytes();
					logInfo("Throughput", "Rx:" + newRecvRxBytes + "\t" + "Tx:"
							+ newSendTxBytes);
				} else if (intent.getAction().equals("getLocation")) {
					if (googleApiClient.isConnected()) {
						Location currentLocation = LocationServices.FusedLocationApi
								.getLastLocation(googleApiClient);
						String locationString = "null";
						if (currentLocation != null) {
							locationString = "alti:"
									+ currentLocation.getAltitude() + "\tlati:"
									+ currentLocation.getLatitude() + "\tlong:"
									+ currentLocation.getLongitude()
									+ "\tspeed:" + currentLocation.getSpeed()
									+ "\taccu:" + currentLocation.getAccuracy();
						}
						logInfo("Location", locationString);
					}
				} else if (intent.getAction().equals("getCell")) {
					String result = "";
					List<CellInfo> cellInfos = telephonyManager
							.getAllCellInfo();
					if (cellInfos == null)
						result += "null";
					else {
						for (CellInfo cellInfo : cellInfos) {
							if (cellInfo instanceof CellInfoLte) {
								CellInfoLte info = (CellInfoLte) cellInfo;
								result += "isConnected:" + info.isRegistered()
										+ "\t";
								result += "CellId:"
										+ info.getCellIdentity().getCi() + "\t";
								result += "PhyCellId:"
										+ info.getCellIdentity().getPci()
										+ "\t";
								result += "RSRP:"
										+ info.getCellSignalStrength().getDbm()
										+ ";";
							}
						}
					}
					logInfo("CellInfo", result);
				}
			}
		};

		this.registerReceiver(broadcastReceiver, filter);

		receiverWifi = new WifiReceiver();
		this.registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		ConnectivityReceiver conReceiver = new ConnectivityReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		this.registerReceiver(conReceiver, intentFilter);
		
		
		intentFilter = new IntentFilter();
		intentFilter.addAction("PhoneStateScan_startWifi");
		intentFilter.addAction("PhoneStateScan_stopWifi");
		intentFilter.addAction("PhoneStateScan_startUDP");
		intentFilter.addAction("PhoneStateScan_stopUDP");
		
		UIReceiver = new BroadcastReceiver(){
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals("PhoneStateScan_startWifi")) {
					startWifiMonitor();
				}else if (intent.getAction().equals("PhoneStateScan_stopWifi")) {
					stopWifiMonitor();
				}else if (intent.getAction().equals("PhoneStateScan_startUDP")) {
					startUDPEcho();
				}else if (intent.getAction().equals("PhoneStateScan_stopUDP")) {
					stopUDPEcho();
				}
			}
		};
		this.registerReceiver(UIReceiver, intentFilter);
		
		
		telephonyManager.listen(new TelephoneStateListener(),
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
				| PhoneStateListener.LISTEN_CALL_STATE);
		
		
		startLocationMonitor();
		startThroughputMonitor();
		startCellMonitor();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
	
	 @Override
	 public void onDestroy() {
		 this.unregisterReceiver(broadcastReceiver);
		 this.unregisterReceiver(receiverWifi);
		 this.unregisterReceiver(conReceiver);
		 this.unregisterReceiver(UIReceiver);
		 isStarted = false;
	 }
	 
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}