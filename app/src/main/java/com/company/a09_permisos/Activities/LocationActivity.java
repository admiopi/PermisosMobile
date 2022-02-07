package com.company.a09_permisos.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.company.a09_permisos.R;
import com.company.a09_permisos.databinding.ActivityLocationBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends AppCompatActivity {

    private final int LOCATION = 4;
    private ActivityLocationBinding binding;

    /**
     * Dos formas:
     * 1. Posicionamiento de las antenas (NETWORK)
     * 2. Posicionamiento por satélite (GPS)
     *
     * - El permiso necesario es el FINE_LOCATION
     * - Permiso COARSE_LOCATION
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    getLocationAction();
                } else {
                    if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getLocationAction();
                    } else {
                        String[] permisos = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
                        requestPermissions(permisos, LOCATION);
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getLocationAction();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocationAction() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location;

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location==null){
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if(location != null){
            binding.lblCoordenadas.setText("Long: "+location.getLongitude()+"\nLat: "+location.getLatitude());
            if(location.getLatitude() != 0 && location.getLongitude() !=0){
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                try {
                    List<Address> direcciones = geocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1);
                    if(!direcciones.isEmpty()){
                        Address direccion = direcciones.get(0);
                        binding.lblDireccion.setText(direccion.getAddressLine(0));
                    }

                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

}