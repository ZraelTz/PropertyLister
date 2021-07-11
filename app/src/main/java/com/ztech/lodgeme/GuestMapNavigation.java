package com.ztech.lodgeme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GuestMapNavigation extends AppCompatActivity {

    private GoogleMap mMap;
    private Context mcontext;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_map_navigation);
        Tools.setSystemBarColor(this, R.color.blue_grey_50);
        getWindow().setNavigationBarColor(getColor(R.color.blue_grey_50));
        Tools.setSystemBarLight(this);
        mcontext = this.getApplicationContext();
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.bt_menu).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(mcontext, GuestProfileMenu.class);
                startActivity(mIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Intent mIntent = new Intent(mcontext, LoginActivity.class);
            startActivity(mIntent);
            finish();
        }
    }
    //    private void initMapFragment() {
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(new OnMapReadyCallback() {
//            @Override
//            public void onMapReady(GoogleMap googleMap) {
//                mMap = Tools.configActivityMaps(googleMap);
//                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(37.7610237, -122.4217785));
//                mMap.addMarker(markerOptions);
//                mMap.moveCamera(zoomingLocation());
//                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//                    @Override
//                    public boolean onMarkerClick(Marker marker) {
//                        try {
//                            mMap.animateCamera(zoomingLocation());
//                        } catch (Exception e) {
//                        }
//                        return true;
//                    }
//                });
//            }
//        });
//    }

    private CameraUpdate zoomingLocation() {
        return CameraUpdateFactory.newLatLngZoom(new LatLng(37.76496792, -122.42206407), 13);
    }


    public void clickAction(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.saved_button:
                Toast.makeText(getApplicationContext(), "Saved Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.list_button:
                Toast.makeText(getApplicationContext(), "List Clicked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.near_button:
                Toast.makeText(getApplicationContext(), "Nearby Clicked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.recents_button:
                Toast.makeText(getApplicationContext(), "Recents Clicked", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
