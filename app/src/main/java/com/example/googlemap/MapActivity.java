package com.example.googlemap;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final int LOCATION_REQUEST_CODE = 100;
    private GoogleMap myMap;
    private FusedLocationProviderClient fusedLocationProviderClient;

    TextView addressField;
    Handler handler;
    long refreshTime = 5000;
    Runnable runnable;
    private  LatLng destinationLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        addressField = findViewById(R.id.addressId);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // fetching continuous location:
//        handler = new Handler();
//        handler.postDelayed(runnable = new Runnable(){
//            @Override
//            public void run() {
//                handler.postDelayed(runnable,refreshTime);
//                checkLocationPermission();
//            }
//        },refreshTime);
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            requestForPermission();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double lat = location.getLatitude();
                    double longitude = location.getLongitude();

                    Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(lat, longitude, 1);
                        if (!addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            String country = address.getCountryName();
                            String state = address.getAdminArea();
                            String city = address.getLocality();
                            String postalCode = address.getPostalCode();
                            String knownName = address.getFeatureName(); // Address name (if available)

                            addressField.setText("Your current Location is: " + knownName + ","+ city  +","+ state);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    LatLng userLocation = new LatLng(lat, longitude);
                    myMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                    myMap.animateCamera(CameraUpdateFactory.zoomTo(12));

//                    myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                        @Override
//                        public void onMapClick(@NonNull LatLng latLng) {
//                            myMap.clear();
//                            destinationLocation = latLng;
//                            MarkerOptions markerOptions = new MarkerOptions();
//                            markerOptions.position(latLng);
//                            markerOptions.icon(setIcon(MapActivity.this, R.drawable.location));
//                            myMap.addMarker(markerOptions);
//                        }
//                    });

                    if(myMap != null)
                    {
                        myMap.clear();
                        myMap.addMarker(new MarkerOptions().position(userLocation).title("My Location"));
                    }
                    Log.i("XOXO", " "+ lat + " " +longitude);

                } else {
                    Toast.makeText(MapActivity.this, "Location is null.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    public BitmapDescriptor setIcon(Activity context, int drawableId)
//    {
//        Drawable drawable = ActivityCompat.getDrawable(context, drawableId);
//        drawable.setBounds(0, 0,drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        drawable.draw(canvas);
//        return BitmapDescriptorFactory.fromBitmap(bitmap);
//    }

    private void requestForPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Accepted", Toast.LENGTH_SHORT).show();
                getUserLocation();
            } else {
                Toast.makeText(this, "Permission Rejected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
    }
}
