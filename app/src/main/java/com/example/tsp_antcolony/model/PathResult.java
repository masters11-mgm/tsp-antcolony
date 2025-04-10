package com.example.tsp_antcolony.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class PathResult {
    public List<LatLng> path;
    public double totalCost;

    public PathResult(List<LatLng> path, double totalCost) {
        this.path = path;
        this.totalCost = totalCost;
    }
}
