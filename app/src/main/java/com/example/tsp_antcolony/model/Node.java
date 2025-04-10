package com.example.tsp_antcolony.model;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public String id;
    public LatLng position;
    public List<Edge> neighbors;

    public double g = Double.MAX_VALUE;
    public double h = 0;
    public double f = 0;
    public Node parent;

    public Node(String id, LatLng position) {
        this.id = id;
        this.position = position;
        this.neighbors = new ArrayList<>();
    }

    public void addNeighbor(Edge edge) {
        neighbors.add(edge);
    }
}
