package pl.edu.lab4.gpsareadetector;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {


    public static final int REQUEST_LOCATION = 97;
    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private Geofence area;
    private LocationManager mLocationMan;
    private Location location;
    private double latitude;
    private double longitude;
    private static final int RADIUS = 150;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        geofencingClient = LocationServices.getGeofencingClient(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Pad the map controls to make room for the button - note that the button may not have
// been laid out yet.
        final Button button = findViewById(R.id.checkout_button);
        button.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mMap.setPadding(0, button.getHeight(), 0, 0);
                    }
                }
        );
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                setGeofence();
            }

        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // system OS > marshmallow
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //requesting permission
                String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
                //show popup to request permissions
                requestPermissions(permission, REQUEST_LOCATION);
            } else {
                //permission already granted
                setLocationLayer();
            }
        } else {
            //system OS < marshmallow
            setLocationLayer();
        }
        ;

    }

    public void setLocationLayer() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
    }

    public void setGeofence() {
        area = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("1")
                .setCircularRegion(latitude, longitude, RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        Toast.makeText(this, "Current geofence stats:\n" + latitude + " " + longitude, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMyLocationClick(Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        //gets and saves my current location

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getCurrentLocation(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // system OS > marshmallow
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //requesting permission
                String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
                //show popup to request permissions
                requestPermissions(permission, REQUEST_LOCATION);
            } else {
                //permission already granted
                location = mLocationMan.getLastKnownLocation(mLocationMan.GPS_PROVIDER);
                assert location != null;
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                String provider = mLocationMan.getBestProvider(criteria, true);
                location = mLocationMan.getLastKnownLocation(provider);

                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } else {
            //system OS < marshmallow
            //permission already granted
            Criteria criteria = new Criteria();
            Location location = mLocationMan.getLastKnownLocation(Objects.requireNonNull(mLocationMan.getBestProvider(criteria, false)));
            assert location != null;
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            Toast.makeText(this, "Here's the data..."+ latitude + "/"+ longitude, Toast.LENGTH_SHORT).show();
        }
        ;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setLocationLayer();
                } else {
                    Toast.makeText(this, "Please allow the permission...", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }
}
