package com.example.wifiscanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class WifiScan extends Thread{
	WifiManager wifiManager;
    //WifiReceiver receiverWifi;
    //List<ScanResult> wifiList;
    Handler handler;
    boolean  isRun;
	//private PrintWriter logFile;
    Context context;
    
//    class WifiReceiver extends BroadcastReceiver {
//        public void onReceive(Context c, Intent intent) {
//        	StringBuilder sb = new StringBuilder();
//            wifiList = wifiManager.getScanResults();
//            for(int i = 0; i < wifiList.size(); i++){
//                sb.append(new Integer(i+1).toString() + ".");
//                sb.append((wifiList.get(i)).toString());
//                sb.append("\\n");
//            }
//            Message msg = handler.obtainMessage();
//			Bundle bundle = new Bundle();
//			bundle.putString("type", "wifiInfo");
//			bundle.putString("data",sb.toString());
//			msg.setData(bundle);
//			handler.sendMessage(msg);
//            if (isRun){
//            	wifiManager.startScan();
//            }
//        }
//    }
    
    public WifiScan(Context context){
		isRun = false;
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		//logFile = new PrintWriter(new BufferedWriter(new FileWriter("sdcard/wifiScanInfo.txt", true)));
		this.context = context;
    }
    
    @Override
    public void run(){
    	if (!isRun){
    		isRun = true;
    		while (isRun){
	    		wifiManager.startScan();
	    		try {
					sleep(5*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    }
    
    public void terminate(){
    	if (isRun){
    		isRun = false;
    	}
    }
}
