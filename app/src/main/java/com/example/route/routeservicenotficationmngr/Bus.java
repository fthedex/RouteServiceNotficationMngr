package com.example.route.routeservicenotficationmngr;


/**
 * Created by Khalil on 10/09/2016.
 */
public class Bus {


    Bus(){
        setId("9");
        setLng(36.2384);
        setLat(30.5852);


    }

    Bus(String busId, Double lng, Double lat){
       setLng(lng);
        setLat(lat);
        setId(busId);
    }


    private String id_;
    private double lng_;
    private double lat_;



    public double getLng(){
        return this.lng_;
    }
    public double getLat(){
        return this.lat_;
    }
    public String getId(){
        return this.id_;
    }

    public void setLng(Double lng){
        lng_=lng;
    }
    public void setLat(Double lat){
        lat_=lat;
    }
    public void setId(String id){
        id_=id;
    }


}