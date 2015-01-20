package com.androidproject.parkassist;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ParkingInformation extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_parking_information);

        TextView textView = (TextView) findViewById(R.id.parkingNameTextView);
        TextView addressTextView = (TextView) findViewById(R.id.addressTextView);
        TextView rateTextView = (TextView) findViewById(R.id.rateTextView);
        TextView capacityTextView = (TextView) findViewById(R.id.capacityTextView);

        String destName = getIntent().getStringExtra("destinationName");
        ImageButton button = (ImageButton)findViewById(R.id.directionButton);
        String source =  getIntent().getStringExtra("sourceCoordinates");
        String destination = getIntent().getStringExtra("destinationCoordinates");

        final String[] sourceArr = source.split("=");
        final String[] destArr = destination.split("=");

        String ratesInfo= null;
        String rateHighest= getIntent().getStringExtra("rateHighest");
        String rateLowest= getIntent().getStringExtra("rateLowest");
        String rateDescription= getIntent().getStringExtra("rateDescription");
        String capacity= getIntent().getStringExtra("capacity");

        if(rateHighest==null && rateLowest==null)
            ratesInfo=rateDescription;
        if(rateHighest!=null && rateLowest!=null) {

            ratesInfo = "$"+rateLowest + " - " + "$"+rateHighest;
            if(rateDescription!=null)
                ratesInfo=ratesInfo+"/n"+rateDescription;
        }
        // gets the geographical address of parking location clicked by the user
        Geocoder geocoder;
        List<Address> addresses = new ArrayList<Address>();
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(destArr[0]), Double.parseDouble(destArr[1]), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0)+"\n"+ addresses.get(0).getAddressLine(1)+"\n"+
                addresses.get(0).getAddressLine(2);

        // Button click listener for directions button to show navigation from current location to a particular parking
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // sends parameters - source and destination co-ordinates to the google maps application
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                 Uri.parse("http://maps.google.com/maps?saddr=" + sourceArr[0] + "," + sourceArr[1] + "&daddr=" + destArr[0] + "," + destArr[1]));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });
       // sets the values recieveed from the intent
       textView.setText(destName);
       addressTextView.setText(address);
       rateTextView.setText(ratesInfo);
        if(capacity!=null)
            capacityTextView.setText(capacity);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_parking_information, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
