package com.example.tsp_antcolony.model;

import com.google.android.gms.maps.model.LatLng;
import com.example.tsp_antcolony.utils.RouteLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphBuilder {

    public static Map<String, Node> buildGraph(List<RouteLoader.RoutePoint> points) {
        Map<String, Node> graph = new HashMap<>();

        for (RouteLoader.RoutePoint point : points) {
            String id = point.idLine + ":" + point.idPoint;
            Node node = new Node(id, point.latLng);
            graph.put(id, node);
        }

        for (int i = 0; i < points.size() - 1; i++) {
            RouteLoader.RoutePoint a = points.get(i);
            RouteLoader.RoutePoint b = points.get(i + 1);

            if (!a.idLine.equals(b.idLine)) continue;

            String idA = a.idLine + ":" + a.idPoint;
            String idB = b.idLine + ":" + b.idPoint;
            Node nodeA = graph.get(idA);
            Node nodeB = graph.get(idB);

            double distance = haversine(a.latLng.latitude, a.latLng.longitude,
                    b.latLng.latitude, b.latLng.longitude);
            double cost = distance / (a.speedValue / 60.0); // time-based cost

            nodeA.addNeighbor(new Edge(nodeA, nodeB, cost));
            // If bidirectional:
            nodeB.addNeighbor(new Edge(nodeB, nodeA, cost));
        }

        return graph;
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // distance in km
    }
}
