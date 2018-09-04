package com.example.shivamgupta.firebaseapp;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class FetchAddressIntentService extends IntentService {
    protected ResultReceiver mReceiver;
    public FetchAddressIntentService(){
        super("FetchAddress IntentService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        if(intent == null){
            return;
        }
        String errorMessage = "";
        //Get the location passed to this service throudh an extra
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
        }catch(IOException ioException) {
            errorMessage = getString(R.string.service_not_available);
            Log.e("Error", errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e("Error", errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e("Error", errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i("Error", getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }



    }

    private void deliverResultToReceiver(int failureResult, String errorMessage) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY,errorMessage);
        mReceiver.send(failureResult,bundle);
    }
}
