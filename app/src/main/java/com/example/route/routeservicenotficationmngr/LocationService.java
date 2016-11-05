package com.example.route.routeservicenotficationmngr;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;


import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.tv.TvView;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Mohammed on 11/2/2016.
 */

public class LocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public LocationService() {
        super("Route-Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Toast.makeText(this, "INTENT TRIGGERED", Toast.LENGTH_SHORT).show();


        //SENDING AND RECEIVING
     /*   Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
        // Extract the receiver passed into the service
        ResultReceiver rec = intent.getParcelableExtra("receiver");
        // Extract additional values from the bundle
        String val = intent.getStringExtra("foo");
        Toast.makeText(this, val, Toast.LENGTH_SHORT).show();
        // To send a message to the Activity, create a pass a Bundle
        Bundle bundle = new Bundle();
        bundle.putString("resultValue", "My Result Value. Passed in: " + val);
        // Here we call send passing a resultCode and the bundle of extras
        rec.send(Activity.RESULT_OK, bundle);*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Destroying Service!", Toast.LENGTH_SHORT).show();

        removeFromServer(currentBus);
        deleteNotificationBar();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        super.onDestroy();
    }

    //TEST GIT

    private String getIdFromSessionFile() {
        String filename= "user_session";
        int c;
        String temp="";
        try {
            FileInputStream fin = openFileInput(filename);

            while( (c = fin.read()) != -1){
                temp = temp + Character.toString((char)c);
            }

//string temp contains all the data of the file.
            fin.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        finally{
            return temp;
        }
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Creating Service!", Toast.LENGTH_SHORT).show();

        currentBus = new Bus();
        requestQueue = Volley.newRequestQueue(LocationService.this);
        currentBus.setId(getIdFromSessionFile());


        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
            createLocationOnStatusBar();
        }
        else
            Toast.makeText(this, "GOOGLE PLAY ISN'T AVAILABLE ON THIS DEVICE ,PLEASE CONTACT YOUR IT IMPLEMENTER!", Toast.LENGTH_SHORT).show();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();

        }

        super.onCreate();
    }


    //_________________ ANYTHING UNDER MUST BE CHANGED


    public RequestQueue requestQueue;
    public Bus currentBus;


    private static final String TAG = MainActivity.class.getSimpleName();


    boolean firstLocationListen=true; //if user's location changes for the first time then add to server else update his location
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    private boolean mRequestLocationUpdates = true; //change this to false if u don't want to receive updates!

    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 200;
    private static int FATEST_INTERVAL = 200;
    private static int DISPLACEMENT = 0;

    NotificationManager notificationManager;
    NotificationCompat.Builder noti;



    private void displayLocation() { //DONE MIGRATING

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Please Check App Permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longtitude = mLastLocation.getLongitude();

            setNotificationBarText(latitude + ", " + longtitude);

            currentBus.setLng(longtitude);
            currentBus.setLat(latitude);
            updateLocServer(currentBus);

            Toast.makeText(this, (latitude + ", " + longtitude), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Couldn't get the location. Make sure location is enabled on the device", Toast.LENGTH_SHORT).show();

        }
    }


    protected synchronized void buildGoogleApiClient() { //DONE MIGRATING
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() { //DONE MIGRATING
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }



    private boolean checkPlayServices() { //DONE MIGRATING
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            return false;
        }
        return true;
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Please Check App Permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) { //DONE MIGRATING


        displayLocation();

        if(mRequestLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) { //DONE MIGRATING
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        Toast.makeText(this, "Location changed!", Toast.LENGTH_SHORT).show();

        displayLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: " + connectionResult.getErrorCode());
    }

    private void deleteNotificationBar(){
        notificationManager.cancel(1234);
    }

    private void setNotificationBarText(String text){


        noti.setContentTitle("Your Location");
        noti.setContentText(text);
        noti.setSmallIcon(R.mipmap.ic_launcher);
        noti.setOngoing(true);


        notificationManager.notify(1234, noti.build());

    }


private void createLocationOnStatusBar() {

 notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

   noti = new NotificationCompat.Builder(this);
        noti.setContentTitle("Your Location");
        noti.setContentText("NO SIGNAL");
        noti.setSmallIcon(R.mipmap.ic_launcher);
        noti.setOngoing(true);

        notificationManager.notify(1234, noti.build());



}
    public void addToServer(final Bus localBus){

        String insertUrl = "http://45.33.73.36/Route/addToServer.php?busId="+localBus.getId()+"&busLng="+localBus.getLng()+"&busLat="+localBus.getLat();

        StringRequest request = new StringRequest(Request.Method.GET, insertUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                System.out.println("SERVER RESPONSE ADDING TO SERVER: "+response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("SERVER ERROR RESPONSE ADDING TO SERVER: "+error.getMessage());
            }
        }) {
/*
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parameters  = new HashMap<String, String>();
                parameters.put("busId",localBus.getId());
                parameters.put("busLng",Double.toString(localBus.getLng()));
                parameters.put("busLat",Double.toString(localBus.getLat()));

                return parameters;
            }*/
        };
        requestQueue.add(request);


    }           //WEB SERVICES FOR DATA COMMUNICATION!
    public void updateLocServer(final Bus localBus) {

        String updateUrl = "http://45.33.73.36/Route/updateBusLocation.php?busId="+localBus.getId()+"&busLng="+localBus.getLng()+"&busLat="+localBus.getLat();

        StringRequest request = new StringRequest(Request.Method.GET, updateUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println("SERVER RESPONSE UPDATE TO SERVER: "+response);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
/* POST REQUEST ->
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                System.out.println(localBus.getId()+" "+localBus.getLng()+","+localBus.getLat());
                Map<String,String> parameters  = new HashMap<String, String>();
                parameters.put("busId",localBus.getId());
                parameters.put("busLng",Double.toString(localBus.getLng()));
                parameters.put("busLat",Double.toString(localBus.getLat()));

                return parameters;
            }*/
        };
        requestQueue.add(request);

    }
    public void removeFromServer(final Bus localBus) {
        String deleteUrl = "http://45.33.73.36/Route/removeFromServer.php?busId="+localBus.getId();
        StringRequest request = new StringRequest(Request.Method.GET, deleteUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                System.out.println("SERVER RESPONSE REMOVE TO SERVER: "+response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
/* POST METHOD!
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parameters  = new HashMap<String, String>();
                parameters.put("busId",localBus.getId());

                return parameters;
            }*/
        };
        requestQueue.add(request);
    }

}
