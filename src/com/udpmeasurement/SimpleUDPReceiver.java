package com.udpmeasurement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SimpleUDPReceiver extends Thread{

	public DatagramSocket socket;
	private DatagramPacket receivedPacket;
	private byte[] receivedBuffer;
	private long latestReceiveTime;
	private PrintWriter logFile;
	private boolean isRun;
	private Handler handler;
	private ClientIdentifier serverId;
	
	  public SimpleUDPReceiver(Handler handler) throws MeasurementError, IOException {
	    try {
	      socket = new DatagramSocket();
	    } catch (SocketException e) {
	      throw new MeasurementError("Failed opening and binding socket!");
	    }
	    this.handler =handler;
	    receivedBuffer = new byte[Config.BUFSIZE];
	    receivedPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
	    latestReceiveTime = 0;
	    
	    logFile = new PrintWriter(new BufferedWriter(new FileWriter("sdcard/receiveLog.txt", true)));
	    serverId = new ClientIdentifier(InetAddress.getByName("xsc.eecs.umich.edu"),59012);
	  }
	  
	  public synchronized void updateLatestReceiveTime(){
		  latestReceiveTime = System.currentTimeMillis();
	  }
	  
	  public synchronized long getLatestReceiveTime(){
		  return latestReceiveTime;
	  }
	  
	  public void run() {
		  isRun = true;
		    System.out.println("Receiver thread is running...");
		    
		    //open port in NAT
		    int sendPacketNum = 0;
		    while ( isRun && sendPacketNum < 3) {
		    	SimpleMeasurementPacket dataPacket = new SimpleMeasurementPacket(serverId);
				dataPacket.dir = 0;
			    dataPacket.seq = 1;
			    
			    byte[] sendBuffer= null;
				try {
					sendBuffer = dataPacket.getByteArray();
				    DatagramPacket sendPacket = new DatagramPacket(
				      sendBuffer, sendBuffer.length, serverId.addr, serverId.port); 
				    socket.send(sendPacket);
				} catch (MeasurementError e2) {
					e2.printStackTrace();
				} catch (IOException e) {
			       e.printStackTrace();
				}
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	sendPacketNum ++;
		    }
		    int receivePacketNum = 0;
		    while (isRun){
		      try {
		        // get client's request
		        socket.setSoTimeout(10000);
		        socket.receive(receivedPacket);
		        ClientIdentifier clientId = new ClientIdentifier(
		          receivedPacket.getAddress(), receivedPacket.getPort()); 
		        Config.logmsg("Received message from " + clientId.toString());

		        // processing message
		        try {
		          SimpleMeasurementPacket packet = new SimpleMeasurementPacket(
		              clientId, receivedPacket.getData());
		          System.out.println("Receive Packet: "+packet);
		          receivePacketNum++;
		          if(receivePacketNum%1000==0){
		        	  if (handler!=null){
		        		  Message msg = handler.obtainMessage();
			    			Bundle bundle = new Bundle();
			    			bundle.putString("type", "receivePacket");
			    			bundle.putString("data","Receive "+receivePacketNum+" packet.");
			    			msg.setData(bundle);
			    			handler.sendMessage(msg);  
		        	  }
		          }
		          //updateLatestReceiveTime();
		          logFile.println(packet);
		          logFile.flush();
		          
		        } catch (MeasurementError e) {
		          Config.logmsg("Error processing message: " + e.getMessage());
		        }

		      } catch (IOException e) {
		      }

		    }
		  }
	  public void terminate(){
			isRun = false;
		}
}
