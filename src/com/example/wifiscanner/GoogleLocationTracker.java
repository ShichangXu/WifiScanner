package com.example.wifiscanner;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GoogleLocationTracker implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	GoogleApiClient mGoogleApiClient;
	private Handler handler;
	private Location mCurrentLocation;
	private LocationRequest mLocationRequest;
	private String mLastUpdateTime;
	private boolean mIsConnected;
	private boolean mIsConnectionFailed;
	private boolean isRun;
	public GoogleLocationTracker(Handler handler, Context context) throws IOException{
		this.handler = handler;
		mGoogleApiClient = new GoogleApiClient.Builder(context)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
		mIsConnected = false;
		mIsConnectionFailed = false;
		mLocationRequest = null;
		isRun = false;
	}

	public void start(){
		if(!isRun){
			mGoogleApiClient.connect();
			isRun = true;
			if (mIsConnected){
				startLocationUpdates();
			}else if (mIsConnectionFailed){
				isRun = false;
			}
		}
	}
	
	public void terminate(){
		isRun = false;
		if (mIsConnected)
		{stopLocationUpdates();
		mGoogleApiClient.disconnect();}
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		mIsConnectionFailed = false;
		isRun = false;
		Log.i("xsc","Connection Failed");
	}

	@Override
	public void onConnected(Bundle arg0) {
		mIsConnected = true;
		mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);   
		Log.i("xsc","Connection Success!"+mCurrentLocation);
		if (isRun){
			startLocationUpdates();
		}
	}

	protected void createLocationRequest() {
	    mLocationRequest = new LocationRequest();
	    mLocationRequest.setInterval(1000);
	    mLocationRequest.setFastestInterval(500);
	    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}
	
	protected void startLocationUpdates() {
		createLocationRequest();
	    LocationServices.FusedLocationApi.requestLocationUpdates(
	            mGoogleApiClient, mLocationRequest, this);
	}
	
	 @Override
	    public void onLocationChanged(Location location) {
	        mCurrentLocation = location;
	        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            Message msg = handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putString("type", "locationInfo");
			bundle.putString("data",mCurrentLocation.toString()+mLastUpdateTime);
			msg.setData(bundle);
			handler.sendMessage(msg);
			Log.i("xsc",mCurrentLocation.toString()+mLastUpdateTime);
	    }
	 
	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	protected void stopLocationUpdates() {
	    LocationServices.FusedLocationApi.removeLocationUpdates(
	            mGoogleApiClient, this);
	}
	
	

}
