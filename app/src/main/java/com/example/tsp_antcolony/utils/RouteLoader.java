package com.example.tsp_antcolony.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RouteLoader {

    private static final String TAG = "RouteLoader";

    public enum SpeedCategory {
        SLOW(10),
        NORMAL(30),
        FAST(50);

        public final int value;

        SpeedCategory(int value) {
            this.value = value;
        }

        public static SpeedCategory fromString(String speed) {
            switch (speed.toLowerCase()) {
                case "slow": return SLOW;
                case "fast": return FAST;
                case "normal": return NORMAL;
                default:
                    Log.w(TAG, "Unknown speed category: " + speed + ". Defaulting to NORMAL.");
                    return NORMAL;
            }
        }
    }

    public static class RoutePoint {
        public int idPoint;
        public String idLine;
        public Integer interchangeId;
        public LatLng latLng;
        public SpeedCategory speedCategory;
        public int speedValue;

        public RoutePoint(int idPoint, String idLine, Integer interchangeId, double lat, double lng, String speed) {
            this.idPoint = idPoint;
            this.idLine = idLine;
            this.interchangeId = interchangeId;
            this.latLng = new LatLng(lat, lng);
            this.speedCategory = SpeedCategory.fromString(speed);
            this.speedValue = this.speedCategory.value;
        }
    }

    public static List<RoutePoint> loadRoute(Context context, String filename) {
        List<RoutePoint> route = new ArrayList<>();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(filename);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(jsonString);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String idLine = obj.getString("idline");
                int idPoint = obj.getInt("idpoint");
                Integer idInterchange = obj.isNull("idinterchange") ? null : obj.getInt("idinterchange");
                double lat = obj.getDouble("lat");
                double lng = obj.getDouble("lng");
                String speed = obj.has("speed") ? obj.getString("speed") : "normal";

                route.add(new RoutePoint(idPoint, idLine, idInterchange, lat, lng, speed));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading route file", e);
        }
        return route;
    }

    public static int getColorForSpeed(SpeedCategory category) {
        switch (category) {
            case SLOW:
                return 0xFFFF0000; // Red
            case FAST:
                return 0xFF00FF00; // Green
            case NORMAL:
            default:
                return 0xFF0000FF; // Blue
        }
    }
}