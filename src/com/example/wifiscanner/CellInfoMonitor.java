package com.example.wifiscanner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CellInfoMonitor extends Thread{
	private boolean isRun;
	private Handler handler;
	private PrintWriter logFile;
	TelephonyManager telephonyManager;
	
	public CellInfoMonitor(Handler handler, Context context) throws IOException{
		this.handler = handler;
		isRun = false;
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		logFile = new PrintWriter(new BufferedWriter(new FileWriter("sdcard/cellInfo.txt", true)));
	}
	
	public void run(){
		isRun = true;
		logFile.println(System.currentTimeMillis()+"startRecording");
		logFile.flush();
		long count = 0;
		while (isRun){
			String cellInfoS = getCurrentLTECellInfo();
			Log.i("xsc",cellInfoS);
			logFile.println(cellInfoS);
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
    			bundle.putString("type", "cellInfo");
    			bundle.putString("data",cellInfoS);
    			msg.setData(bundle);
    			handler.sendMessage(msg);
			}
		}
		logFile.println(System.currentTimeMillis()+"endRecording");
		logFile.flush();
	}
	
	@SuppressLint("NewApi")
	private String getCurrentLTECellInfo() {
		String result = System.currentTimeMillis()+"\t";
		List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
		if (cellInfos == null)
			result += "null";
		else{
			for (CellInfo cellInfo : cellInfos) {
				if (cellInfo instanceof CellInfoLte) {
					CellInfoLte info = (CellInfoLte) cellInfo;
					result += "isConnected:" + info.isRegistered() + "\t";
					result += "CellId:" + info.getCellIdentity().getCi() + "\t";
					result += "PhyCellId:" + info.getCellIdentity().getPci() + "\t";
					result += "RSRP:" + info.getCellSignalStrength().getDbm()+";";
				}
			}
		}
		return result;
	}
	
	public void terminate()
	{
		isRun = false;
	}
}