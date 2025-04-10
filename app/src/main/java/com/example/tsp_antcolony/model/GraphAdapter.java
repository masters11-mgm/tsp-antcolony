package com.example.tsp_antcolony.model;


import com.example.tsp_antcolony.core.AntColony;
import com.example.tsp_antcolony.core.BeeColony;
import com.google.android.gms.maps.model.LatLng;
import com.example.tsp_antcolony.model.Edge;
import com.example.tsp_antcolony.model.Node;

import java.util.HashMap;
import java.util.Map;

public class GraphAdapter {

    public static Map<String, AntColony.Node> adaptToAntColony(Map<String, Node> original) {
        Map<String, AntColony.Node> converted = new HashMap<>();

        for (Node node : original.values()) {
            AntColony.Node antNode = new AntColony.Node(node.id, node.position);
            converted.put(node.id, antNode);
        }

        for (Node node : original.values()) {
            AntColony.Node from = converted.get(node.id);
            for (Edge edge : node.neighbors) {
                AntColony.Node to = converted.get(edge.to.id);
                from.neighbors.add(new AntColony.Edge(to, edge.cost));
            }
        }

        return converted;
    }

    public static Map<String, BeeColony.Node> adaptToBeeColony(Map<String, Node> original) {
        Map<String, BeeColony.Node> converted = new HashMap<>();

        for (Node node : original.values()) {
            BeeColony.Node beeNode = new BeeColony.Node(node.id, node.position);
            converted.put(node.id, beeNode);
        }

        for (Node node : original.values()) {
            BeeColony.Node from = converted.get(node.id);
            for (Edge edge : node.neighbors) {
                BeeColony.Node to = converted.get(edge.to.id);
                from.neighbors.add(new BeeColony.Edge(to, edge.cost));
            }
        }

        return converted;
    }
}