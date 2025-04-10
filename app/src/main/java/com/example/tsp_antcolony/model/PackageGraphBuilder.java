package com.example.tsp_antcolony.model;

import com.example.tsp_antcolony.utils.PackageLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageGraphBuilder {

    public static Map<String, Node> buildGraph(List<PackageLoader.PackagePoint> packages) {
        Map<String, Node> graph = new HashMap<>();

        for (PackageLoader.PackagePoint p : packages) {
            Node node = new Node(String.valueOf(p.id), p.latLng);
            graph.put(node.id, node);
        }

        for (Node from : graph.values()) {
            for (Node to : graph.values()) {
                if (!from.id.equals(to.id)) {
                    double dist = haversine(from.position.latitude, from.position.longitude,
                            to.position.latitude, to.position.longitude);
                    from.addNeighbor(new Edge(from, to, dist));
                }
            }
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
        return R * c;
    }
}
