package com.example.wifiscanner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ConnectivityMonitor {
	ConnectivityManager conManager;
	Handler handler;
    boolean  isRun;
    Context context;
    PrintWriter logFile;
    ConReceiver conReceiver;
    class ConReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
        	//Log.i("xsc",intent.toString());
        	StringBuilder sb = new StringBuilder();
        	sb.append(intent.getAction());
        	if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
        		NetworkInfo netInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        		sb.append("\t"+netInfo.getDetailedState().name()+"\t"+netInfo.getState().name()+"\t"+netInfo.getExtraInfo());
                if( netInfo.isConnected() )
                {
                	sb.append("\tConnected to"+((WifiInfo)intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO)).toString());
                } else{
                	sb.append("\tNot connected");
                }
        	}else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
        		int currentState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
        		int previousState = intent.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE,-1);
        		sb.append("\t"+currentState+"<--"+previousState);
        	}else if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
        		SupplicantState state = (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
        		sb.append("\t"+state.name());
        	}else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
        		Bundle extras = intent.getExtras();
        		if (extras!=null){
        			for (String key: extras.keySet()){
        				sb.append("\t"+key+":"+extras.get(key));
        			}
        		}
        	}else if(intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)){
        		int currentRSSI = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI,0);
        		sb.append("\t"+currentRSSI);
        	}
        	Log.i("xsc",new Date().getTime()+"\t"+sb.toString());
        	logFile.println(new Date().getTime()+"\t"+sb.toString());
        	logFile.flush();
            Message msg = handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putString("type", "wifiInfo");
			bundle.putString("data",sb.toString());
			msg.setData(bundle);
			handler.sendMessage(msg);
        }
    }
    public ConnectivityMonitor(Handler handler, Context context) throws IOException{
		this.handler = handler;
		isRun = false;
		conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		logFile = new PrintWriter(new BufferedWriter(new FileWriter("sdcard/connectivityInfo.txt", true)));
		this.context = context;
    }
    
    public void start(){
    	if (!isRun){
    		isRun = true;
    		conReceiver = new ConReceiver();
    		IntentFilter intentFilter = new IntentFilter();
    		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    		intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
    		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    		intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
            context.registerReceiver(conReceiver, intentFilter);
    	}
    }
    
    public void terminate(){
    	if (isRun){
    		isRun = false;
    		context.unregisterReceiver(conReceiver);
    	}
    }
}
