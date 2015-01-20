package com.androidproject.parkassist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MapScreen extends FragmentActivity implements LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    //Paramters for getting current user location
    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    private Location location = null;
    private static List<String> myLocations = new ArrayList<String>();
    private static List<ParkingDestination>parkings= new ArrayList<ParkingDestination>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // getting buttons present in menu tab
        final ImageButton parkMeButton = (ImageButton) findViewById(R.id.parkMeButton);
        final ImageButton homeButton = (ImageButton) findViewById(R.id.homeButton);
        final ImageButton walkDirectionsButton = (ImageButton) findViewById(R.id.walkDirectionsButton);
        final ImageButton garageButton = (ImageButton) findViewById(R.id.garageButton);
        homeButton.setBackgroundColor(getResources().getColor(R.color.selected_color));// shows Home screen is slelected

        // button click event of ParkMe button in the tab(second button)
        parkMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapScreen.this, ParkMe.class);
                if(location!=null)
                {
                    // sends current position and zoom level in the map
                    intent.putExtra("latitude",location.getLatitude());
                    intent.putExtra("longitude",location.getLongitude());
                    intent.putExtra("zoomLevel",mMap.getCameraPosition().zoom);

                }

                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
        //button click event for third button ie Walking Direction from current location to saved parking spot
        walkDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Checks if parked location is stored or not from app database
                ParkingLocationStoreDB locationStoreDB =  new ParkingLocationStoreDB(getApplicationContext());
                Cursor cursor = locationStoreDB.getAllLocations();
                if(cursor.getCount()>0)
                {
                    // if location present calls the WalkingDirection activity
                    Intent intent = new Intent(MapScreen.this,WalkingDirections.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);

                }
                else{
                    //message shown to the user
                 Toast.makeText(getApplicationContext(),"Please park the Car",Toast.LENGTH_LONG).show();
                }
            }
        });
        //Button Click event to show the repair shops around current location
        garageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MapScreen.this,RepairShopActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });


        String url = getApplicationContext().getResources().getString(R.string.url);
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS)
        {
            // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else
        {
            //gets the cached location from application
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            provider = locationManager.getBestProvider(criteria, true);
            setUpMapIfNeeded();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,100,this);
            location = locationManager.getLastKnownLocation(provider);
            // if location is null then disable other buttons
             if(location==null)
              {
                    Toast.makeText(getApplicationContext(), "Location is Null", Toast.LENGTH_LONG).show();
                    parkMeButton.setClickable(false);
                    walkDirectionsButton.setClickable(false);
                    garageButton.setClickable(false);
                }
            //enable buttons if location is present and draw car marker at the current location
            if (location != null)
            {


                drawMarker(location);
                parkMeButton.setClickable(true);
                walkDirectionsButton.setClickable(true);
                garageButton.setClickable(true);
            }
            // gets Parking location XML provided by Streetline Availability API
            new CallAPI().execute(url);

            // Sets on click listener for the information window dispalyed on position marker
            // displays Parking Information activity
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    // checks which marker has been clicked
                    ParkingDestination destination = new ParkingDestination();
                    LatLng markerPos = marker.getPosition();
                    for(int i=0;i<parkings.size();i++){

                        destination = parkings.get(i);
                        LatLng listPos = new LatLng(destination.getLatitude(),destination.getLongitude());
                        if(listPos.equals(markerPos)){
                             break;
                        }
                    }
                    Intent intent = new Intent(MapScreen.this,ParkingInformation.class);
                    // adds parking information to the intent
                    intent.putExtra("destinationName",destination.getDestinationName());
                    intent.putExtra("destinationCoordinates",new String(destination.getLatitude()+"="+destination.getLongitude()));
                    intent.putExtra("sourceCoordinates",new String(location.getLatitude()+"="+location.getLongitude()));
                    intent.putExtra("rateHighest",destination.getRateHighest()==null?null:destination.getRateHighest().toString());
                    intent.putExtra("rateLowest",destination.getRateLowest()==null?null:destination.getRateLowest().toString());
                    intent.putExtra("rateDescription",destination.getRateDescription());
                    intent.putExtra("capacity",destination.getSpaceCapacityTotal()==null?null:Integer.toString(destination.getSpaceCapacityTotal()));
                    startActivity(intent);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                }
            });
        }
    }

    @Override
    public void onLocationChanged(Location newLocation) {

           drawMarker(newLocation);
           displayLocations();

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        Toast.makeText(getApplicationContext(), "Disabled", Toast.LENGTH_SHORT).show();
        showGPSDisableDialog();
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        Toast.makeText(getApplicationContext(), "Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    // draws car marker for the current location
    private void drawMarker(Location location) {
        mMap.clear();
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(currentPosition)

                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))

                .flat(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    // sets up the map fragment
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisableDialog();
        } else {
            Toast.makeText(getApplicationContext(), "Waiting for location", Toast.LENGTH_SHORT).show();
            mMap.setMyLocationEnabled(false);
            //location = mMap.getMyLocation();

        }
    }

    // dialog box shown to the user when GPS is disabled and needs to be turned on
    private void showGPSDisableDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Services Disabled");
        builder.setMessage("Application needs access to your location. Please turn on location access.");
        builder.setCancelable(false);
        builder.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                System.exit(0);
            }
        });
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.show();

    }
    // Checks the parking locations around 1 mile radius of current location
    private void displayLocations() {
        Double latitude = null, longitude = null;
        if (location != null) {
            String tempArray[] = new String[2];
            Location destination = new Location(" ");
            for (int i = 0; i < parkings.size(); i++) {
                 ParkingDestination parkingDestination=new ParkingDestination();
                parkingDestination=parkings.get(i);
                latitude =parkingDestination.getLatitude();
                longitude = parkingDestination.getLongitude();
                destination.setLatitude(latitude);
                destination.setLongitude(longitude);
                double inMeters = location.distanceTo(destination);
                int radius = getApplicationContext().getResources().getInteger(R.integer.radius);
                double radInDouble = (double) radius;
                double inKms = inMeters / 1000;
                double inMiles = inKms * 0.621371;
                if (inMiles <= 1)
                {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.parking)).title(parkingDestination.getDestinationName()));
                }
            }

        }

    }
    // Async Taks class that gets the XML data
    private class CallAPI extends AsyncTask<String, String, String> {
        Long time = Long.valueOf(0);
        InputStream parkingLocations;
            ProgressDialog progressDialog;
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            String result = "";
            InputStream inputStream = null;
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                parkingLocations = new BufferedInputStream(urlConnection.getInputStream());
                showCoordinates(parkingLocations);

            } catch (Exception e) {
                e.printStackTrace();

            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MapScreen.this,"Wait","Loading....");
            time = System.currentTimeMillis();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            System.out.println("=============!!!!@@@@!!@!@!@!@!@!@!@!}}|}|}|}|}|}|}|}|}|}" + ((System.currentTimeMillis() - time) * 0.001));
            displayLocations();
            System.out.println("=============!!!!@@@@!!@!@!@!@!@!@!@!}}|}|}|}|}|}|}|}|}|}" + ((System.currentTimeMillis() - time) * 0.001));
        }
        // parses XML data using DOM parser and stores data into a List of ParkingDestination class
        private void showCoordinates(InputStream inputStream) {
            double latitude = 0.0, longitude,RateHighest=0.0,RateLowest=0.0;
            int spaceTotal=0;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = null;
            try {
                documentBuilder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null;
            try {
                doc = documentBuilder.parse(parkingLocations);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    NodeList childNodes = node.getChildNodes();
                    ParkingDestination park=new ParkingDestination();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node cNode = childNodes.item(j);
                        if (cNode instanceof Element) {
                            String content = cNode.getTextContent();
                            if (cNode.getNodeName().equals("Latitude")) {
                                latitude = Double.parseDouble(content);
                                park.setLatitude(latitude);
                            }
                            if (cNode.getNodeName().equals("Longitude")) {
                                longitude = Double.parseDouble(content);
                                park.setLongitude(longitude);
                                myLocations.add(String.valueOf(latitude) + "," + String.valueOf(longitude));
                            }

                            if (cNode.getNodeName().equals("DestinationID")) {
                                //latitude = Double.parseDouble(content);
                                park.setDestinationId(content);
                            }
                            if (cNode.getNodeName().equals("DestinationName")) {
                                //latitude = Double.parseDouble(content);
                                park.setDestinationName(content);
                            }
                            if (cNode.getNodeName().equals("TimeZone")) {
                                //latitude = Double.parseDouble(content);
                                park.setTimeZone(content);
                            }
                            if (cNode.getNodeName().equals("CurrencySymbol")) {
                                //latitude = Double.parseDouble(content);
                                park.setCurrencySymbol(content);
                            }

                            if (cNode.getNodeName().equals("RateDescription")) {
                                //latitude = Double.parseDouble(content);
                                park.setRateDescription(content);
                            }
                            if (cNode.getNodeName().equals("RateHighest")) {
                                RateHighest = Double.parseDouble(content);
                                park.setLatitude(RateHighest);
                            }
                            if (cNode.getNodeName().equals("RateLowest")) {
                                RateLowest = Double.parseDouble(content);
                                park.setLatitude(RateLowest);
                            }

                            if (cNode.getNodeName().equals("SpaceCapacityTotal")) {
                                spaceTotal = Integer.parseInt(content);
                                park.setSpaceCapacityTotal(spaceTotal);
                            }


                        }
                    }
                    parkings.add(park);
                }

            }


        }


    }
}
