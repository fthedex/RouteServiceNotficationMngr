package com.example.route.routeservicenotficationmngr;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public MyTestReceiver receiverForTest;
    Button loginBtn,logoutBtn;
    TextView username,password;
    public RequestQueue requestQueue;
    boolean loggedIn = false;
    Bus currentBus;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is not supported", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        currentBus = new Bus();

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        loginBtn = (Button) findViewById(R.id.loginBtn);
        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        username = (TextView) findViewById(R.id.driverUsername);
        password = (TextView) findViewById(R.id.driverPassword);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.length()<3){
                    Toast.makeText(MainActivity.this, "Please Enter A Username First!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length()<3){
                    Toast.makeText(MainActivity.this, "Please Enter A Password First!", Toast.LENGTH_SHORT).show();
                    return;

                }
                isValidUser(username.getText().toString(),password.getText().toString());
            }
        });


        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loggedIn=false;
                loginBtn.setVisibility(View.VISIBLE);
                logoutBtn.setVisibility(View.INVISIBLE);

                username.setEnabled(true);
                password.setEnabled(true);
                stopService();
                Toast.makeText(MainActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void isValidUser(final String  user, final String pass) {
        String insertUrl = "http://45.33.73.36/Route/getUserInfoIfValid.php";
        StringRequest request = new StringRequest(Request.Method.POST, insertUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println(response);

                if(response.equals("TRUE")){
                    logUserIn();
                }
                else{
                    Toast.makeText(MainActivity.this, "Invalid Info Please Try Again !", Toast.LENGTH_SHORT).show();
                }

            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("SERVER ERROR RESPONSE ADDING TO SERVER: "+error.getMessage());
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parameters  = new HashMap<String, String>();
                parameters.put("username",user);
                parameters.put("password",pass);

                return parameters;
            }
        };
        requestQueue.add(request);


    }

    private void logUserIn() {

        loggedIn=true;

        loginBtn.setVisibility(View.INVISIBLE);
        logoutBtn.setVisibility(View.VISIBLE);
        username.setEnabled(false);
        password.setEnabled(false);

        currentBus.setId(username.getText().toString());
        createWriteFile(username.getText().toString());
        addToServer(currentBus);

        onStartService();
        Toast.makeText(this, "Successfully Logged In!", Toast.LENGTH_SHORT).show();
    }




    @Override
    protected void onStart(){
        super.onStart();
        if(!checkPlayServices()){
            Toast.makeText(this, "Google play isn't available on this device , please enable it first!", Toast.LENGTH_SHORT).show();
        }
    }

    private void createWriteFile(String id) {
        String string = id;
        String filename = "user_session";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();


    }


    // Starts the IntentService
    public void onStartService() {
        Intent i = new Intent(this, LocationService.class);
      /*  i.putExtra("foo", "bar"); //COMMUNICATION
        i.putExtra("receiver", receiverForTest);*/
        startService(i);
    }

    public void stopService(){
        Intent i = new Intent(this, LocationService.class);
      /*  i.putExtra("foo", "bar"); //COMMUNICATION
        i.putExtra("receiver", receiverForTest);*/
        stopService(i);
    }

    // Setup the callback for when data is received from the service
    public void setupServiceReceiver() {
        receiverForTest = new MyTestReceiver(new Handler());
        // This is where we specify what happens when data is received from the service
        receiverForTest.setReceiver(new MyTestReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_OK) {
                    String resultValue = resultData.getString("resultValue");
                    Toast.makeText(MainActivity.this, resultValue, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//TEST GITadsasdsadas asdasdsad asdasdas

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


}
