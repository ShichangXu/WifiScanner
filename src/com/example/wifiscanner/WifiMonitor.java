package com.example.wifiscanner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;

public class WifiMonitor extends Thread{
	private boolean isRun;
	private Handler handler;
	private PrintWriter logFile;
	private WifiManager wifiManager;
	
	public WifiMonitor(Handler handler, Context context) throws IOException{
		this.handler = handler;
		isRun = false;
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		logFile = new PrintWriter(new BufferedWriter(new FileWriter("sdcard/wifiInfo.txt", true)));
	}
	
	public void run(){
		isRun = true;
		logFile.println(System.currentTimeMillis()+"startRecording");
		logFile.flush();
		long count = 0;
		while (isRun){
			String wifiInfoS = getCurrentWifiInfo();
			Log.i("xsc",wifiInfoS);
			logFile.println(wifiInfoS);
			logFile.flush();
			try {
				sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			count ++;
			if(count>100){
				count = 0;
				Message msg = handler.obtainMessage();
    			Bundle bundle = new Bundle();
    			bundle.putString("type", "wifiInfo");
    			bundle.putString("data",wifiInfoS);
    			msg.setData(bundle);
    			handler.sendMessage(msg);
			}
		}
		//logFile.println(System.currentTimeMillis()+"endRecording");
		logFile.flush();
	}

	private String getCurrentWifiInfo() {
		String result = System.currentTimeMillis()+"\t";
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo!=null){
			result += "SSID:"+wifiInfo.getSSID()+"\t";
			result += "BSSID:"+wifiInfo.getBSSID()+"\t";
			result += "RSSI:"+wifiInfo.getRssi()+"\t";
			result += "IP:"+Formatter.formatIpAddress(wifiInfo.getIpAddress())+"\t";
		}
		return result;
	}
	
	public void terminate()
	{
		isRun = false;
	}
}
