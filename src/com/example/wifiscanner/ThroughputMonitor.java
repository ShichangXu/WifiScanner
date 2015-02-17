package com.example.wifiscanner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ThroughputMonitor extends Thread{
	private boolean isRun;
	private Handler handler;
	private PrintWriter logFile;
	private long recvRxBytes, sendTxBytes, lastTime;
	private double uplinkSpeed, downlinkSpeed;
	public ThroughputMonitor(Handler handler, Context context) throws IOException{
		this.handler = handler;
		isRun = false;
		logFile = new PrintWriter(new BufferedWriter(new FileWriter("sdcard/throughput.txt", true)));
		recvRxBytes = 0;
		sendTxBytes = 0;
		lastTime = 0;
	}
	
	public void run(){
		isRun = true;
		int count = 0;
		while (isRun){
			long newRecvRxBytes = TrafficStats.getTotalRxBytes();
			long newSendTxBytes = TrafficStats.getTotalTxBytes();
			long newTime = System.currentTimeMillis();
			if(newTime -lastTime < 1000){
				uplinkSpeed = (double)(newSendTxBytes-sendTxBytes)/(newTime-lastTime);
				downlinkSpeed = (double)(newRecvRxBytes-recvRxBytes)/(newTime-lastTime);
				String record = newTime +"\t"+uplinkSpeed+"\t"+downlinkSpeed;
				logFile.println(record);
				count ++;
				if(count>100){
					count = 0;
					Message msg = handler.obtainMessage();
	    			Bundle bundle = new Bundle();
	    			bundle.putString("type", "throughputInfo");
	    			bundle.putString("data",record);
	    			msg.setData(bundle);
	    			handler.sendMessage(msg);
	    			Log.i("xsc",record);
				}
			}
			lastTime = newTime;
			recvRxBytes = newRecvRxBytes;
			sendTxBytes = newSendTxBytes;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void terminate(){
		isRun = false;
	}
}
