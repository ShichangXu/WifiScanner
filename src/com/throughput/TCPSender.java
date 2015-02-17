package com.throughput;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPSender extends Thread{
	boolean isRun;
	Socket sendSocket;
	
	public TCPSender(){
		isRun = false;
		sendSocket = null;
	}
	
	@Override
	public void run(){
		if (!isRun){
			isRun = true;
			while(isRun){
				try {
					sendSocket = new Socket("xsc.eecs.umich.edu", 6789);
					DataOutputStream outToServer = new DataOutputStream(sendSocket.getOutputStream());
					while (isRun){
						String sentense="1234567890";
						outToServer.writeBytes(sentense + '\n');
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void terminate(){
		isRun = false;
	}

}
