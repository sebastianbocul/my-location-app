package com.example.mylocationapp;

import androidx.annotation.NonNull;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;

import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import java.util.List;

import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    Button btLocation;
    TextView textView1,textView2,textView3,textView4,textView5;
    FusedLocationProviderClient fusedLocationProviderClient;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private double myLatitude = 0 ,myLongitude = 0;
    private GoogleMap myGoogleMap;
    private EditText userNameEditText;
    private static final int REQUEST_CODE = 44;
    public List<Address> addresses;

    private TextWatcher nameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String nameInput = userNameEditText.getText().toString().trim();

            btLocation.setEnabled(!nameInput.isEmpty());


        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(MainActivity.this,"Firebase connection Success", Toast.LENGTH_LONG).show();


        userNameEditText = (EditText) findViewById(R.id.name);
        btLocation = (Button) findViewById(R.id.bt_location);
        textView1 =(TextView) findViewById(R.id.text_view1);
        textView2 =(TextView) findViewById(R.id.text_view2);
        textView3 =(TextView) findViewById(R.id.text_view3);
        textView4 =(TextView) findViewById(R.id.text_view4);
        textView5 =(TextView) findViewById(R.id.text_view5);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        userNameEditText.addTextChangedListener(nameTextWatcher);



        btLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checking permission
                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    getLocation(userNameEditText.getText().toString());
                }
                else {
                    ActivityCompat.requestPermissions(MainActivity.this, new  String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
                }
            }
        });

    }

    private void getLocation(final String userName) {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if(location!=null){
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

                        textView1.setText("Lattitude: " + addresses.get(0).getLatitude());
                        textView2.setText("Longitude: " + addresses.get(0).getLongitude());
                        textView3.setText("Country Name: " + addresses.get(0).getCountryName());
                        textView4.setText("Locality: " + addresses.get(0).getLocality());
                        textView5.setText("Address: " + addresses.get(0).getAddressLine(0));
                        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
                        supportMapFragment.getMapAsync(MainActivity.this);


                        database = FirebaseDatabase.getInstance();
                        myRef = database.getReference().child("Users").child(userName);

                        myRef.child("userName").setValue(userName);

                        myRef.child("latitude").setValue(addresses.get(0).getLatitude());
                        myLatitude=addresses.get(0).getLatitude();
                        myRef.child("longitude").setValue(addresses.get(0).getLongitude());
                        myLongitude = addresses.get(0).getLongitude();


                        if(addresses.get(0).getCountryName()!=null){
                            myRef.child("countryName").setValue(addresses.get(0).getCountryName());
                        }else {
                            myRef.child("countryName").setValue("Not found");
                        }

                        if(addresses.get(0).getLocality()!=null){
                            myRef.child("locality").setValue(addresses.get(0).getLocality());
                        }else {
                            myRef.child("locality").setValue("Not found");
                        }

                        if(addresses.get(0).getAddressLine(0)!=null){
                            myRef.child("address").setValue(addresses.get(0).getAddressLine(0));
                        }else {
                            myRef.child("address").setValue("Not found");
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        myGoogleMap = googleMap;
        LatLng latLng = new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));



        myRef = database.getReference();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                myGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                            textView1.setText("Lattitude: " + dataSnapshot.child("Users").child(marker.getTag().toString()).child("latitude").getValue().toString());
                            textView2.setText("Longitude: " +dataSnapshot.child("Users").child(marker.getTag().toString()).child("longitude").getValue().toString());
                            textView3.setText("Country Name: " + dataSnapshot.child("Users").child(marker.getTag().toString()).child("countryName").getValue().toString());
                            textView4.setText("Locality: " + dataSnapshot.child("Users").child(marker.getTag().toString()).child("locality").getValue().toString());
                            textView5.setText("Address: " + dataSnapshot.child("Users").child(marker.getTag().toString()).child("address").getValue().toString());
                        return false;
                    }
                });
                for(DataSnapshot ds : dataSnapshot.child("Users").getChildren()){
                    String userName = ds.child("userName").getValue().toString();
                    double latitude = Double.parseDouble(ds.child("latitude").getValue().toString());
                    double longitude = Double.parseDouble(ds.child("longitude").getValue().toString());
                    double distance = distance(myLatitude,myLongitude,latitude,longitude);
                    LatLng latLngFB = new LatLng(latitude,longitude);
                    MarkerOptions markerOptionsFB;
                    if (userName.equals(userNameEditText.getText().toString())) {
                        markerOptionsFB= new MarkerOptions().position(latLngFB).title("I'm here").icon(BitmapDescriptorFactory.defaultMarker(300));
                    }
                    else {
                        markerOptionsFB= new MarkerOptions().position(latLngFB).title(userName + "\r" + Math.round(distance) + "km");
                    }
                    Marker myMarker = myGoogleMap.addMarker(markerOptionsFB);
                    myMarker.setTag(ds.child("userName").getValue().toString());



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]  == PackageManager.PERMISSION_GRANTED){
                    getLocation(userNameEditText.getText().toString());
                }
                break;


        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515* 1.609344;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
