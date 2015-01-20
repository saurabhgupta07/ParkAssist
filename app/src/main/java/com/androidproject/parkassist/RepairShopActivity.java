package com.androidproject.parkassist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RepairShopActivity extends FragmentActivity implements android.location.LocationListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    private Location location = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_shop);
        // gets the tab menu buttons
        ImageButton garageButton = (ImageButton) findViewById(R.id.garageButton);
        garageButton.setBackgroundColor(getResources().getColor(R.color.selected_color));// shows garage button is selected
        ImageButton walkDirectionsButton = (ImageButton) findViewById(R.id.walkDirectionsButton);
        ImageButton parkMeButton = (ImageButton) findViewById(R.id.parkMeButton);
        ImageButton homeButton = (ImageButton) findViewById(R.id.homeButton);


        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepairShopActivity. this,MapScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

            }
        });
        parkMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepairShopActivity.this, ParkMe.class);
                if(location!=null)
                {
                    intent.putExtra("latitude",location.getLatitude());
                    intent.putExtra("longitude",location.getLongitude());
                    intent.putExtra("zoomLevel",mMap.getCameraPosition().zoom);

                }

                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        walkDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParkingLocationStoreDB locationStoreDB =  new ParkingLocationStoreDB(getApplicationContext());
                Cursor cursor = locationStoreDB.getAllLocations();
                if(cursor.getCount()>0)
                {
                    Intent intent = new Intent(RepairShopActivity.this,WalkingDirections.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);

                }
                else{
                    Toast.makeText(getApplicationContext(), "Please park the Car", Toast.LENGTH_LONG).show();
                }
            }
        });


        setUpMapIfNeeded();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, true);
        location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,100,this);

        // calls google map to show navigation from current location to a repair shop
        // on clicking information window of the marker

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                LatLng markerPos = marker.getPosition();

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + location.getLatitude() + "," + location.getLongitude() + "&daddr=" + markerPos.latitude + "," + markerPos.longitude));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);

            }
        });
            if(location!=null){

                showMarker();
                showRepairShops();

            }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    // shows current location marker
    public void showMarker(){

        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }
    // gets the repair shops by calling Google Places API
    public void showRepairShops(){
        CallPlacesApi callPlacesApi = new CallPlacesApi();
        callPlacesApi.execute(location);
        List<RepairShop> repairShops = null;
        try {
            repairShops = callPlacesApi.get();
        } catch (InterruptedException e) {


        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(repairShops.size()>0){
            for(int i=0;i<repairShops.size();i++){
                RepairShop repairShop = repairShops.get(i);
                mMap.addMarker(new MarkerOptions().position(new LatLng(repairShop.getLatitude(), repairShop.getLongitude() )).icon(BitmapDescriptorFactory.fromResource(R.drawable.repair_shop)).title("Directions to : "+repairShop.getName()));

            }

        }
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
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onLocationChanged(Location newLocation) {


              mMap.clear();
              showMarker();
              showRepairShops();



    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // Async Task to get repair shops using Google Places API
    class CallPlacesApi extends AsyncTask<Location,String,List<RepairShop>> {
        ProgressDialog progressDialog;
        @Override
        protected List<RepairShop> doInBackground(Location... params) {

            String urlString = getResources().getString(R.string.places_api);
            List<RepairShop> repairShopList = new ArrayList<RepairShop>();
            String key =  getResources().getString(R.string.places_api_key);
            urlString=urlString+""+"location="+location.getLatitude()+","+location.getLongitude()+"&radius=2000&types=car_repair&key="+key;
            InputStream inputStream = null;
            try {

                HttpPost httpPost = new HttpPost(urlString);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                String resultData = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject =  new JSONObject(resultData);
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for(int i=0;i<jsonArray.length();i++){
                    RepairShop repairShop = new RepairShop();
                    JSONObject repairShopObj =  jsonArray.getJSONObject(i);
                    JSONObject locationObj = repairShopObj.optJSONObject("geometry").optJSONObject("location");
                    repairShop.setLatitude(Double.parseDouble(locationObj.getString("lat")));
                    repairShop.setLongitude(Double.parseDouble(locationObj.getString("lng")));
                    repairShop.setName(repairShopObj.getString("name"));
                    repairShop.setRepairShopId(repairShopObj.getString("id"));
                    repairShopList.add(repairShop);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return repairShopList;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog = ProgressDialog.show(RepairShopActivity.this, "Wait", "Loading....");
        }

        @Override
        protected void onPostExecute(List<RepairShop> repairShops) {
             super.onPostExecute(repairShops);
             progressDialog.dismiss();
        }
    }
}
