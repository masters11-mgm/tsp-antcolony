package com.example.tsp_antcolony.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tsp_antcolony.R;
import com.example.tsp_antcolony.model.PackageRouteSolver;
import com.example.tsp_antcolony.model.PathResult;
import com.example.tsp_antcolony.utils.PackageLoader;
import com.example.tsp_antcolony.utils.RouteLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


import java.util.List;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<PackageLoader.PackagePoint> selectedPackages;
    private final LatLng startPoint = new LatLng(-8.0199653, 112.6191625);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button buttonSend = findViewById(R.id.button1);
        Button buttonMethod = findViewById(R.id.button2);
        Button resetButton = findViewById(R.id.resetButton);

        buttonSend.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, buttonSend);
            popup.getMenuInflater().inflate(R.menu.menu_data, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                handleButtonSelectionData(item);
                return true;
            });
            popup.show();
        });

        buttonMethod.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, buttonMethod);
            popup.getMenuInflater().inflate(R.menu.menu_action, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                handleButtonSelectionAction(item);
                return true;
            });
            popup.show();
        });

        resetButton.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.clear();
            }
            drawBaseRoute();
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void handleButtonSelectionData(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnflow1:
                selectedPackages = PackageLoader.loadPackagesByFlow(this, "data-package.json", "Flow 1");
                break;
            case R.id.btnflow2:
                selectedPackages = PackageLoader.loadPackagesByFlow(this, "data-package.json", "Flow 2");
                break;
            case R.id.btnflow3:
                selectedPackages = PackageLoader.loadPackagesByFlow(this, "data-package.json", "Flow 3");
                break;
        }

        if (mMap != null && selectedPackages != null) {
            for (PackageLoader.PackagePoint pkg : selectedPackages) {
                mMap.addMarker(new MarkerOptions().position(pkg.latLng).title(pkg.name));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 14));
            Toast.makeText(this, "Data paket ditampilkan.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void handleButtonSelectionAction(MenuItem item) {
        if (selectedPackages == null || selectedPackages.isEmpty()) {
            Toast.makeText(this, "Please select a package flow first!", Toast.LENGTH_SHORT).show();
            return;
        }

        PathResult result = null;
        switch (item.getItemId()) {
            case R.id.BtnAntColony:
                result = PackageRouteSolver.solve(selectedPackages, PackageRouteSolver.AlgorithmType.ANT_COLONY, 100, 20);
                Toast.makeText(this, "Ant Colony executed", Toast.LENGTH_SHORT).show();
                break;
            case R.id.BtnBeeColony:
                result = PackageRouteSolver.solve(selectedPackages, PackageRouteSolver.AlgorithmType.BEE_COLONY, 100, 20);
                Toast.makeText(this, "Bee Colony executed", Toast.LENGTH_SHORT).show();
                break;
        }

        if (result != null && mMap != null) {
            mMap.clear();
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.add(startPoint);
            mMap.addMarker(new MarkerOptions().position(startPoint).title("Start Point"));

            for (int i = 0; i < result.path.size(); i++) {
                LatLng point = result.path.get(i);
                polylineOptions.add(point);
                mMap.addMarker(new MarkerOptions().position(point).title("Paket ke-" + (i + 1)));
            }
            mMap.addPolyline(polylineOptions);

            int roundedCost = (int) Math.round(result.totalCost);
            Toast.makeText(this, "Total Jarak: " + roundedCost + " km", Toast.LENGTH_LONG).show();
        }
    }
    private void drawBaseRoute() {
        List<RouteLoader.RoutePoint> routePoints = RouteLoader.loadRoute(this, "map-route.json");
        if (routePoints == null || routePoints.size() < 2) return;

        PolylineOptions baseLine = new PolylineOptions();
        for (RouteLoader.RoutePoint point : routePoints) {
            baseLine.add(point.latLng);
        }
        mMap.addPolyline(baseLine);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        drawBaseRoute();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 14));
    }
}