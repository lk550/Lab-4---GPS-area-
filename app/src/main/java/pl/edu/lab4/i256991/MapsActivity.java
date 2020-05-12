package pl.edu.lab4.i256991;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback, LocationListener {


    public static final int REQUEST_LOCATION = 97;
    private GoogleMap mMap;
    private LocationManager mLocationMan;
    private String provider;
    private Location myLocation;
    private Location areaCenter;
    private static final int RADIUS = 150;
    private boolean layerActive = false;



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        mLocationMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert mLocationMan != null;
        boolean gpsEnabled = mLocationMan.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if GPS is enabled and if not send user to the GSP settings
        if (!gpsEnabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // system OS > marshmallow
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //requesting permission
                String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
                //show popup to request permissions
                requestPermissions(permission, REQUEST_LOCATION);
            } else {
                //permission already granted
                getCurrentLocation();
                mLocationMan.requestLocationUpdates(provider, 400, 1, this);
            }
        }
        else {
            //system OS < marshmallow
            //permission already granted
            getCurrentLocation();
            mLocationMan.requestLocationUpdates(provider, 400, 1, this);
        }

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
                setArea();
                //disable button to prevent adding more areas since the app only requested the detection of a single area
                button.setEnabled(false);
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

    }

    public void setLocationLayer() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        layerActive=true;
    }

    public void setArea() {
        double latitude = myLocation.getLatitude();
        double longitude = myLocation.getLongitude();

        //sets center of area in myLocation
        areaCenter = myLocation;

        CircleOptions areaCircle = new CircleOptions()
                .center( new LatLng(latitude, longitude) )
                .radius( RADIUS )
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2);
        mMap.addCircle(areaCircle);

        Toast.makeText(this, "Area created with center at: \n (" + areaCenter.getLatitude() + " , " + areaCenter.getLongitude() + ")", Toast.LENGTH_LONG).show();

    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onMyLocationButtonClick() {
       // Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        //gets and saves my current location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // system OS > marshmallow
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //requesting permission
                String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
                //show popup to request permissions
                requestPermissions(permission, REQUEST_LOCATION);
            } else {
                //permission already granted
             getCurrentLocation();
            }
        } else {
            //system OS < marshmallow
            //permission already granted
           getCurrentLocation();
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getCurrentLocation(){

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                provider = mLocationMan.getBestProvider(criteria, true);
                myLocation = mLocationMan.getLastKnownLocation(provider);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setLocationLayer();
            } else {
                Toast.makeText(this, "Please allow the permission...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        if(layerActive) {
            mMap.setMyLocationEnabled(true);
            mLocationMan.requestLocationUpdates(provider, 400, 1, this);
        }
    }

    @Override
    public void onPause() {
        if(layerActive) {
            mMap.setMyLocationEnabled(false);
            mLocationMan.removeUpdates(this);
        }
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onLocationChanged(Location location) {
        getCurrentLocation();
        if(areaCenter!=null) {
            float distance = myLocation.distanceTo(areaCenter);
            if (distance > RADIUS) {
                AlertDialog outside_area = new AlertDialog.Builder(this)
                        .setTitle("You left the area!")
                        .setMessage("Your location is currently outside the radius of the set area")
                        .setPositiveButton("OK",null)
                        .setCancelable(false)
                        .show();
            }
        }
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
}


