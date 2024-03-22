package com.example.googlemap;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.List;


public class SearchMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap myMap;
    TextView addressField;
    Button btn;
    SearchView mapSearhView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_map);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);



        addressField = findViewById(R.id.addressId);
        mapSearhView = findViewById(R.id.searchId);
        btn = findViewById(R.id.btnId);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapSearhView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = mapSearhView.getQuery().toString();
                List<Address> addressList = null;

                if(location != null)
                {
                    Geocoder geocoder = new Geocoder(SearchMapActivity.this);
                    try{
                        addressList = geocoder.getFromLocationName(location, 1);
                    }
                    catch (IOException e){
                        Toast.makeText(SearchMapActivity.this, "Please Enter correct location"+e, Toast.LENGTH_SHORT).show();
                    }
                    Address address = addressList.get(0);
                    String knownName = address.getFeatureName();

                    addressField.setText("Your current Location is: " +knownName);

                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    myMap.addMarker(new MarkerOptions().position(latLng).title(location));

                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(SearchMapActivity.this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);

        myMap.getUiSettings().setZoomGesturesEnabled(true);
        myMap.getUiSettings().setScrollGesturesEnabled(true);

//        myMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(SearchMapActivity.this,R.raw.map_style));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_type, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.mapNone){
            myMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
        if(id == R.id.mapNormal){
            myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        if(id == R.id.mapSatellite){
            myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        if(id == R.id.mapHybrid){
            myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        if(id == R.id.mapTerrain){
            myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
        return true;
    }
}
