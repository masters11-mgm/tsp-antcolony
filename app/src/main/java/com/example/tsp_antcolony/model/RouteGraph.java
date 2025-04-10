package com.example.tsp_antcolony.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RouteGraph {
    private final Map<String, Node> nodes = new HashMap<>();

    public void addNode(Node node) {
        nodes.put(node.id, node);
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    public void reset() {
        for (Node node : nodes.values()) {
            node.g = Double.MAX_VALUE;
            node.h = 0;
            node.f = 0;
            node.parent = null;
        }
    }
}
