package com.ghdeoliveira.googlemaps;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.view.View;
import android.widget.RadioButton;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_CODE = 1234;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap nMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private float ZOOM_PADRAO = 16;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLocationPermission();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onMapReady(GoogleMap googleMap) {
        nMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Corrdenadas BC Shopping: -26.992370861036004, -48.64672461124661
        LatLng bcShopping = new LatLng(-26.992370861036004, -48.64672461124661);

        nMap.addMarker(new MarkerOptions()
                .position(bcShopping)
                .title("Balneário Camboriú Shopping")
                .snippet("Av. Santa Catarina, 1 - Estados, Balneário Camboriú/SC")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));

        nMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                Double latitude = latLng.latitude;
                Double longitude = latLng.longitude;

                Toast.makeText(MainActivity.this, "Lat: " + latitude + " | Long: " + longitude, Toast.LENGTH_LONG).show();

                nMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("BC Shopping")
                        .snippet("Av. Santa Catarina, 1 - Estados, Balneário Camboriú/SC")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
            }
        });

        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bcShopping, ZOOM_PADRAO));
    }

    public void onImageViewClicked(View view) {
        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            nMap.setMyLocationEnabled(true);
            nMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        RadioButton tipo_satelite = findViewById(R.id.radio_satelite);
        RadioButton tipo_normal = findViewById(R.id.radio_normal);

        if (tipo_normal.isChecked()) {
            nMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        else if (tipo_satelite.isChecked()){
            nMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, ZOOM_PADRAO));

                            Double latitude = currentLatLng.latitude;
                            Double longitude = currentLatLng.longitude;
                            String local = ("Sua Localização: Lat: " + latitude + " | Long: " + longitude).toString();
                            Toast.makeText(MainActivity.this, local, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void getLocationPermission() {
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE: {
                if(grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                }
            }
        }

    }

}