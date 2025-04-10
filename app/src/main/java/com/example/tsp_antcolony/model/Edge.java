package com.example.tsp_antcolony.model;

public class Edge {
    public Node from;
    public Node to;
    public double cost;
    public double pheromone = 1.0;

    public Edge(Node from, Node to, double cost) {
        this.from = from;
        this.to = to;
        this.cost = cost;
    }
}
