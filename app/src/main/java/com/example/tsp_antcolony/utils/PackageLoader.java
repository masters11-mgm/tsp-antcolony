package com.example.tsp_antcolony.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PackageLoader {

    private static final String TAG = "PackageLoader";

//    List<PackageLoader.PackagePoint> packages = PackageLoader.loadPackagesByFlow(context, "data-package.json", "Flow 1");


    public static class PackagePoint {
        public int id;
        public String name;
        public LatLng latLng;

        public PackagePoint(int id, String name, double lat, double lng) {
            this.id = id;
            this.name = name;
            this.latLng = new LatLng(lat, lng);
        }
    }

    public static List<PackagePoint> loadPackagesByFlow(Context context, String filename, String flowName) {
        List<PackagePoint> packages = new ArrayList<>();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(filename);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonString);

            JSONArray flowArray = root.getJSONArray(flowName);

            for (int i = 0; i < flowArray.length(); i++) {
                JSONObject obj = flowArray.getJSONObject(i);
                int id = obj.getInt("id");
                String name = obj.getString("package");
                double lat = obj.getDouble("lat");
                double lng = obj.getDouble("long");
                packages.add(new PackagePoint(id, name, lat, lng));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading packages for flow: " + flowName, e);
        }
        return packages;
    }
}
