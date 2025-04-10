package com.example.tsp_antcolony.model;
import com.example.tsp_antcolony.core.AntColony;
import com.example.tsp_antcolony.core.BeeColony;
import com.example.tsp_antcolony.utils.PackageLoader;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackageRouteSolver {

    public enum AlgorithmType {
        ANT_COLONY,
        BEE_COLONY
    }

    public static PathResult solve(List<PackageLoader.PackagePoint> packages,
                                   AlgorithmType algorithmType,
                                   int iteration, int agents) {

        Map<String, AntColony.Node> antGraph = null;
        Map<String, BeeColony.Node> beeGraph = null;

        List<LatLng> path = new ArrayList<>();

        switch (algorithmType) {
            case ANT_COLONY:
                antGraph = GraphAdapter.adaptToAntColony(PackageGraphBuilder.buildGraph(packages));
                AntColony.Node startAnt = antGraph.get(String.valueOf(packages.get(0).id));
                path = AntColony.findPath(antGraph, startAnt, startAnt,
                        agents, iteration, 1.0, 5.0, 0.5, 100);
                break;

            case BEE_COLONY:
                beeGraph = GraphAdapter.adaptToBeeColony(PackageGraphBuilder.buildGraph(packages));
                BeeColony.Node startBee = beeGraph.get(String.valueOf(packages.get(0).id));
                path = BeeColony.findPath(beeGraph, startBee, startBee,
                        agents, iteration, 3, 5, 10);
                break;
        }

        double totalCost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            LatLng a = path.get(i);
            LatLng b = path.get(i + 1);
            totalCost += haversine(a.latitude, a.longitude, b.latitude, b.longitude);
        }

        return new PathResult(path, totalCost);
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
