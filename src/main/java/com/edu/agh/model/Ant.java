package com.edu.agh.model;


import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.LinkedList;
import java.util.List;

public class Ant {
    public int currentCity;
    public boolean[] visited;
    public List<Integer> route;
    public INDArray visitedND;

    public Ant(int size) {
        visited = new boolean[size];
        visitedND = Nd4j.ones(size);
        route = new LinkedList<>();
    }
}
