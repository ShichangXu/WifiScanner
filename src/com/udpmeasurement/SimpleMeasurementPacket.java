package com.udpmeasurement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SimpleMeasurementPacket {

	public ClientIdentifier clientId;
	public int token; //token=231412;
	public int dir; //dir=0: server-->client dir=1:client-->server
	public long sendTimestamp;
	public long recTimestamp;
	public int seq;
	
	public SimpleMeasurementPacket(ClientIdentifier cliId) {
	    this.clientId = cliId;
	    this.token = 231412;
	    this.sendTimestamp = System.currentTimeMillis();
	  }
	
	public SimpleMeasurementPacket(ClientIdentifier cliId, byte[] rawdata)
		      throws MeasurementError{
		    this.clientId = cliId;

		    ByteArrayInputStream byteIn = new ByteArrayInputStream(rawdata);
		    DataInputStream dataIn = new DataInputStream(byteIn);
		    
		    try {
		      token = dataIn.readInt();
		      dir = dataIn.readInt();
		      sendTimestamp = dataIn.readLong();
		      recTimestamp = System.currentTimeMillis();
		      seq = dataIn.readInt();
		    } catch (IOException e) {
		      throw new MeasurementError("Fetch payload failed! " + e.getMessage());
		    }
		    
		    if (token!=231412)
		    	throw new MeasurementError("Wrong token!");
		    try {
		      byteIn.close();
		    } catch (IOException e) {
		      throw new MeasurementError("Error closing inputstream!");
		    }
		  }
		  
		  /**
		   * Pack the structure to the network message
		   * @return the network message in byte[]
		   * @throws MeasurementError stream writer failed
		   */
		  public byte[] getByteArray() throws MeasurementError {

		    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		    DataOutputStream dataOut = new DataOutputStream(byteOut);
		    
		    try {
		      dataOut.writeInt(token);
		      dataOut.writeInt(dir);
		      dataOut.writeLong(sendTimestamp);
		      dataOut.writeInt(seq);
		    } catch (IOException e) {
		      throw new MeasurementError("Create rawpacket failed! " + e.getMessage());
		    }
		    
		    byte[] rawPacket = byteOut.toByteArray();
		    
		    try {
		      byteOut.close();
		    } catch (IOException e) {
		      throw new MeasurementError("Error closing outputstream!");
		    }
		    return rawPacket; 
		  }
		  
		  @Override
		  public String toString(){
			  return "clientid:"+clientId+"\t"+"dir:"+dir+"\t"
					  +"sendTimestamp:"+sendTimestamp+"\t"+"recTimestamp:"+recTimestamp+"\t"+"seq:"+seq;
		  }

}
