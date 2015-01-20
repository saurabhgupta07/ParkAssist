package com.androidproject.parkassist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ParkMe extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ParkingLocationStoreDB locationStoreDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_me);
        setUpMapIfNeeded();
        // gets all the buttons in the menu tab
        final ImageButton parkMeButton = (ImageButton) findViewById(R.id.parkMeButton);
        final ImageButton homeButton = (ImageButton) findViewById(R.id.homeButton);
        final ImageButton walkDirectionsButton = (ImageButton) findViewById(R.id.walkDirectionsButton);
        final ImageButton garageButton = (ImageButton) findViewById(R.id.garageButton);

        parkMeButton.setBackgroundColor(getResources().getColor(R.color.selected_color));// sets parkme button as selected

        final Button parkButton = (Button) findViewById(R.id.parkButton);

        final TextView parkingAtLabelTextView= (TextView)findViewById(R.id.parkingAtLabelTextView);
        final TextView parkingAtTextView=(TextView)findViewById(R.id.parkingAtTextView);
        // gets values from intent
        final Double longitude = getIntent().getDoubleExtra("longitude",0.0);
        final Double latitude = getIntent().getDoubleExtra("latitude",0.0);
        final Float zoomLevel = getIntent().getFloatExtra("zoomLevel",0.0f);

        // database class is initialised
        locationStoreDB = new ParkingLocationStoreDB(this.getApplicationContext());
        // gets the values from the table
        Cursor cursor= locationStoreDB.getAllLocations();
        int length= cursor.getCount();
        //checking if the user has parked his car or not
        if(length==0)
        {
            // car is not parked. Initialize data accordingly
            // sets marker on the current location
            parkingAtLabelTextView.setText(getResources().getString(R.string.confirm_parking));
            parkingAtTextView.setText(getAddress(latitude,longitude));
            parkButton.setText(getResources().getString(R.string.park_me));
            setMarker(latitude,longitude,zoomLevel,false);

        }
        else if(length==1)
        {
            // car is already parked
            //shows marker at location stored in DB
            parkingAtLabelTextView.setText(getResources().getString(R.string.parkedAtLocation));
            // gets the first and only record in the db
            if(cursor.moveToFirst())
            {
                Double parkedLatitude = cursor.getDouble(cursor.getColumnIndex("lat"));
                Double parkedLongitude = cursor.getDouble(cursor.getColumnIndex("lng"));
                String  zoom =  cursor.getString(cursor.getColumnIndex("zom"));
                parkingAtTextView.setText(getAddress(parkedLatitude,parkedLongitude));
                setMarker(parkedLatitude,parkedLongitude,Float.parseFloat(zoom), true);
            }
            parkButton.setText(getResources().getString(R.string.done_parking));
        }
        // button click event for Home Button
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParkMe.this,MapScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

            }
        });
        // button click event for walking directions
        // checks for all conditions discussed in MapScreen activity
        walkDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParkingLocationStoreDB locationStoreDB =  new ParkingLocationStoreDB(getApplicationContext());
                Cursor cursor = locationStoreDB.getAllLocations();
                if(cursor.getCount()>0)
                {
                    Intent intent = new Intent(ParkMe.this,WalkingDirections.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);

                }
                else{
                    Toast.makeText(getApplicationContext(),"Please park the Car",Toast.LENGTH_LONG).show();
                }
            }
        });
        // button click event for Repair shops
        garageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParkMe.this,RepairShopActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });






        // button click event for Park Me button if car is to be parked
        // and the click event for Done Parking button when the car is already parked
        parkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(parkButton.getText().equals(getResources().getString(R.string.park_me))){
                    // parks the car, stores the current location in the database
                    // and sets an updated marker
                    if(latitude!=null&&longitude!=null && zoomLevel!=null){
                        long rowId =   insertToDB(latitude,longitude,zoomLevel);
                        parkingAtLabelTextView.setText(getResources().getString(R.string.parkedAtLocation));
                        parkingAtTextView.setText(getAddress(latitude,longitude));
                        parkButton.setText(getResources().getString(R.string.done_parking));
                        setMarker(latitude,longitude,zoomLevel,true);
                    }

                }
                else
                {
                    // removes the location ie deletes the row, ensures that a single or no record is present at all times
                    // changes marker to the original marker
                    int delRow = deleteFromDB();

                    parkingAtLabelTextView.setText(getResources().getString(R.string.confirm_parking));
                    parkButton.setText(getResources().getString(R.string.park_me));
                    Intent intent =  new Intent(ParkMe.this,MapScreen.class);
                    startActivity(intent);
                    finish();
                }

            }
        });

    }

    // retreives geographical location
    public String getAddress(Double latitude,Double longitude)
    {
        Geocoder geocoder;
        List<Address> addresses = new ArrayList<Address>();
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude,1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0);

        return address;

    }

    // inserts values in the database
     public long insertToDB(Double latitude,Double longitude, Float zoomLevel){
         ContentValues dbContent = new ContentValues();
         dbContent.put(ParkingLocationStoreDB.FIELD_LAT,latitude);
         dbContent.put(ParkingLocationStoreDB.FIELD_LNG,longitude);
         dbContent.put(ParkingLocationStoreDB.FIELD_ZOOM,Float.toString(zoomLevel));
         long rowId = locationStoreDB.insert(dbContent);
         return rowId;
     }
    // deletes record from the database
    public int deleteFromDB(){
        return locationStoreDB.del();
    }
    // sets marker depending upon the conditions
    // false if original marker is to be shown, true if updated marker needs to be shown

    public void setMarker(Double latitude, Double longitude,Float zoomLevel, boolean marked){
        mMap.clear();
        if(marked){
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("Me").icon(BitmapDescriptorFactory.fromResource(R.drawable.carparked)));

        }
        else{
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("Me").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude,longitude)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));

    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
       // mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
}
