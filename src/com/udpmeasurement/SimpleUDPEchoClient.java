package com.udpmeasurement;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SimpleUDPEchoClient extends Thread{
	private DatagramSocket socket;
	private PrintWriter logFile;
	private boolean isRun;
	private int seq;
	private Handler handler;
	private ClientIdentifier serverId;
	
	public SimpleUDPEchoClient(Handler handler) throws MeasurementError, IOException{
		try {
		      socket = new DatagramSocket();
		    } catch (SocketException e) {
		      throw new MeasurementError("SimpleUDPSender Failed opening and binding socket!");
		    }
		this.handler =handler;
		    logFile = new PrintWriter(new BufferedWriter(new FileWriter("sdcard/echoLog.txt", true)));
		    seq = 0;

		    serverId = new ClientIdentifier(InetAddress.getByName("xsc.eecs.umich.edu"),31341);
	}
	
	public void run(){
		isRun = true;
		int sendPacketNum = 0;
		long nextSendTime = 0;
		while (isRun) {
			sendPacketNum ++;
			SimpleMeasurementPacket dataPacket = new SimpleMeasurementPacket(serverId);
			dataPacket.dir = 1;
		    dataPacket.seq = seq;
		    seq ++;
		    
		    byte[] sendBuffer= null;
			try {
				nextSendTime = System.currentTimeMillis()+20;
				sendBuffer = dataPacket.getByteArray();
			    DatagramPacket sendPacket = new DatagramPacket(
			      sendBuffer, sendBuffer.length, serverId.addr, serverId.port); 
			    socket.send(sendPacket);
			    Config.logmsg("Send message to " + serverId.toString());
			    logFile.println(dataPacket);
			    sendPacketNum++;
			    socket.setSoTimeout(5);
			    try{
			    	socket.receive(sendPacket);
			    	dataPacket = new SimpleMeasurementPacket(new ClientIdentifier(sendPacket.getAddress(), sendPacket.getPort()), sendPacket.getData());
			    	dataPacket.dir = 2;
			    	dataPacket.recTimestamp = System.currentTimeMillis();
			    	logFile.println(dataPacket);
			    	Config.logmsg("Receive message from " + serverId.toString());
			    } catch(IOException e){
			    	
			    }
//		          if(sendPacketNum%1000==0){
//		        	  if (handler!=null){
//		        	  Message msg = handler.obtainMessage();
//		    			Bundle bundle = new Bundle();
//		    			bundle.putString("type", "sendPacket");
//		    			bundle.putString("data","send "+sendPacketNum+" packet.");
//		    			msg.setData(bundle);
//		    			handler.sendMessage(msg);}
//		          }
			} catch (MeasurementError e2) {
				e2.printStackTrace();
			} catch (IOException e) {
		       e.printStackTrace();
			}
			try {
				if (nextSendTime > System.currentTimeMillis())
				sleep(nextSendTime-System.currentTimeMillis());
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
