package com.vivifiedexistence.floatjot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.tasks.OnSuccessListener;

import actions.ActionWaitForAccuracy;
import commands.Command;
import geo.GeoUtils;
import gl.Color;
import gl.GL1Renderer;
import gl.GLFactory;
import gl.scenegraph.MeshComponent;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import system.ArActivity;
import system.DefaultARSetup;
import util.Log;
import util.Vec;
import worldData.Obj;
import worldData.World;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    public FusedLocationProviderClient currLocClient;
    public Location deviceLocation;
    Button viewAR,addJot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Button refresh = (Button)findViewById(R.id.refresh);
        addJot = (Button)findViewById(R.id.addNote);
        addJot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArActivity.startWithSetup(MapsActivity.this, new DefaultARSetup() {
                    @Override
                    public void addObjectsTo(GL1Renderer renderer, World world, GLFactory objectFactory) {
                        final World myWorld = world;


                        getGuiSetup().addButtonToTopView(new Command() {
                            @Override
                            public boolean execute() {
                                final Vec vector = myWorld.getMyCamera().getPositionOnGroundWhereTheCameraIsLookingAt();
                                Color c = Color.getRandomRGBColor();

                                c.alpha = 0.7f;
                                MeshComponent arrow = GLFactory.getInstance().newDiamond(c);
                                arrow.setPosition(vector);
                                myWorld.add(arrow);
                                arrow.setOnClickCommand(new Command() {
                                    @Override
                                    public boolean execute() {
                                        AppGlobalData.currentVec = vector;
                                        Intent i = new Intent(MapsActivity.this,CreateAndViewActivity.class);
                                        startActivity(i);
                                        return false;
                                    }
                                });
                                return false;
                            }
                        },"Add A JotNote");
                    }
                });
            }
        });
        viewAR = (Button) findViewById(R.id.viewJot);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new JSONASync(MapsActivity.this).execute();


            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                currLocClient = LocationServices.getFusedLocationProviderClient(this);
                currLocClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!=null){
                            deviceLocation = location;
                            AppGlobalData.currentLocation = location;
                            Log.d("LATLNG", String.valueOf(deviceLocation.getLatitude()));
                            LatLng temp = new LatLng(deviceLocation.getLatitude(), deviceLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp,15));
                            new JSONASync(MapsActivity.this).execute();

                        }
                    }
            });
            }
        }
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //LatLng temp = new LatLng(deviceLocation.getLatitude(), deviceLocation.getLongitude());
       // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15));

    }

    public void dispSnackbar(){

        Toast.makeText(this, "Jot nearby, click button to continue", Toast.LENGTH_SHORT).show();
        Log.d("DIST", "kappa<1000");
        viewAR.setVisibility(View.VISIBLE);

        viewAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArActivity.startWithSetup(MapsActivity.this, new DefaultARSetup() {
                    @Override
                    public void addObjectsTo(GL1Renderer renderer, World world, GLFactory objectFactory) {
//                        final World myWorld = world;
//                        getGuiSetup().addButtonToTopView(new Command() {
//                            @Override
//                            public boolean execute() {
//                                final Vec vector = myWorld.getMyCamera().getPositionOnGroundWhereTheCameraIsLookingAt();
//                                Color c = Color.getRandomRGBColor();
//
//                                c.alpha = 0.7f;
//                                MeshComponent arrow = GLFactory.getInstance().newDiamond(c);
//                                arrow.setPosition(vector);
//                                myWorld.add(arrow);
//                                arrow.setOnClickCommand(new Command() {
//                                    @Override
//                                    public boolean execute() {
//                                        AppGlobalData.currentVec = vector;
//                                        Intent i = new Intent(MapsActivity.this,CreateAndViewActivity.class);
//                                        startActivity(i);
//                                        return false;
//                                    }
//                                });
//                                return false;
//                            }
//                        },"Add A JotNote");

                        for(int i=0;i<AppGlobalData.nearbyJots.size();i++)
                        {
                            final int val = i;
                            Color c = Color.getRandomRGBColor();
                            c.alpha = 0.7f;
                            MeshComponent arrow = GLFactory.getInstance().newDiamond(c);
                            Vec vector = new Vec();
                            vector.x = Float.parseFloat(AppGlobalData.nearbyJots.get(i).vx);
                            vector.y = Float.parseFloat(AppGlobalData.nearbyJots.get(i).vy);
                            vector.z = Float.parseFloat(AppGlobalData.nearbyJots.get(i).vz);
                            arrow.setPosition(vector);

                            Vec labelVec = new Vec();
                            labelVec.x = vector.x;
                            labelVec.y = vector.y;
                            labelVec.z = vector.z + 2.5f;

                            Obj label = GLFactory.getInstance().newTextObject(AppGlobalData.nearbyJots.get(i).title,
                                    labelVec, getApplicationContext(), getCamera());

                            world.add(arrow);
                            world.add(label);

                            arrow.setOnClickCommand(new Command() {
                                @Override
                                public boolean execute() {
                                    AppGlobalData.currentFloatJot = AppGlobalData.nearbyJots.get(val);
                                    Intent in = new Intent(MapsActivity.this,ViewJot.class);
                                    startActivity(in);
                                    return false;
                                }
                            });
                        }





                    }
                });
            }
        });
    }
}
