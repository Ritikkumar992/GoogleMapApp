package com.example.googlemap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.codebyashish.googledirectionapi.AbstractRouting;
import com.codebyashish.googledirectionapi.ErrorHandling;
import com.codebyashish.googledirectionapi.RouteDrawing;
import com.codebyashish.googledirectionapi.RouteInfoModel;
import com.codebyashish.googledirectionapi.RouteListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class MapRoute extends AppCompatActivity implements OnMapReadyCallback, RouteListener {
    FusedLocationProviderClient fusedLocationProviderClient;
    GoogleMap myMap;
    double userLat, userLong;
    private LatLng destinationLocation, userLocation;
    ArrayList<Polyline> polylines = new ArrayList<>();
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_route);

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.routeMap);
        if (fragment != null) {
            fragment.getMapAsync(this);
        }

        dialog = new ProgressDialog(MapRoute.this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myMap.setMyLocationEnabled(true);
        myMap.getUiSettings().setZoomControlsEnabled(true);

        // function to add map icon whenever we click on google map.
        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                dialog.setMessage("Route is generating, Please Wait");
                dialog.show();
                myMap.clear();
                destinationLocation = latLng;
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.icon(setIcon(MapRoute.this,R.drawable.destination));
                myMap.addMarker(markerOptions);

                getRoutePoints(userLocation, destinationLocation);
            }
        });
        fetchMyLocation(); // function to automatically fetch my current location.
    }
    private void getRoutePoints(LatLng userLocation, LatLng destinationLocation) {
        RouteDrawing routeDrawing = new RouteDrawing.Builder()
                .context(MapRoute.this)
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this).alternativeRoutes(true)
                .waypoints(userLocation, destinationLocation)
                .build();
        routeDrawing.execute();
    }

    private void fetchMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                userLat = location.getLatitude();
                userLong = location.getLongitude();

                userLocation = new LatLng(userLat, userLong);

                LatLng latLng = new LatLng(userLat, userLong);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(12)
                        .build();
                myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                myMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(setIcon(MapRoute.this,R.drawable.location)));
            }
        });
    }
    public BitmapDescriptor setIcon(Activity context, int drawableId){
        Drawable drawable = ActivityCompat.getDrawable(context, drawableId);
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onRouteFailure(ErrorHandling e) {
        Log.i("ERROR", e.toString());
        Toast.makeText(this, "Route Failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRouteStart() {
        Toast.makeText(this, "Route Start", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRouteSuccess(ArrayList<RouteInfoModel> list, int indexing) {
        Toast.makeText(this, "Route Success", Toast.LENGTH_LONG).show();

        if (polylines != null) {
            polylines.clear();
        }

        PolylineOptions polylineOptions = new PolylineOptions();

        for (int i = 0; i < list.size(); i++) {
            if (i == indexing) {
                Log.e("TAG", "onRoutingSuccess: routeIndexing" + indexing);
                polylineOptions.color(Color.BLACK);
                polylineOptions.width(12);
                polylineOptions.addAll(list.get(indexing).getPoints());
                polylineOptions.startCap(new RoundCap());
                polylineOptions.endCap(new RoundCap());
                Polyline polyline = myMap.addPolyline(polylineOptions);
                polylines.add(polyline);
            }
        }
        dialog.dismiss();
    }

    @Override
    public void onRouteCancelled() {
        Toast.makeText(this, "Route Cancelled!", Toast.LENGTH_LONG).show();
    }
}