package com.edu.agh.clustering;


import com.edu.agh.model.XY;

import java.util.*;

public class Kmeans {

    private final int width;
    private final int height;
    private final int pad;
    private XY[] centroids;

    public Kmeans(int width, int height, int pad) {
        this.width = width;
        this.height = height;
        this.pad = pad;
    }

    public XY[] getCentroids() {
        return this.centroids;
    }

    public Map<Integer, List<XY>> cluster(XY[] array, int kNo) {
        int iterations = 20;
        XY[] centroids = new XY[kNo];
        Random random = new Random();
        for(int i = 0; i < kNo; i++){
            centroids[i] = new XY(random.nextInt(width - pad + 1) + pad, random.nextInt(height - pad + 1) + pad);
        }
        Map<Integer, List<XY>> clusters = null;
        for(int i = 0; i < iterations; i++) {
            clusters = new HashMap<>();
            for(int c = 0; c < kNo; c++) {
                clusters.put(c, new ArrayList<>());
            }
            for(int j = 1; j < array.length; j++) {
                int min = Integer.MAX_VALUE;
                for(int l = 0; l < kNo; l++) {
                    if(distance(centroids[l], array[j]) < min) {
                        min = (int) distance(centroids[l], array[j]);
                        array[j].k = l;
                        clusters.get(l).add(array[j]);
                        for(Map.Entry<Integer, List<XY>> cluster : clusters.entrySet()) {
                            if(cluster.getKey() != l && cluster.getValue().contains(array[j])) {
                                cluster.getValue().remove(array[j]);
                            }
                        }
                    }
                }
            }
            for(Map.Entry<Integer, List<XY>> cluster : clusters.entrySet()) {
                meanCoordinates(cluster.getValue(), centroids[cluster.getKey()]);
            }
        }
        this.centroids = centroids;
        return clusters;
    }

    private void meanCoordinates(List<XY> coordinates, XY centroid) {
        if(coordinates.size() == 0) {
            return;
        }
        int x = 0;
        int y = 0;
        int size = coordinates.size();
        for(XY coordinate : coordinates) {
            x += coordinate.x;
            y += coordinate.y;
        }
        centroid.x = x/size;
        centroid.y = y/size;
    }

    private double distance(XY cluster, XY point) {
        int x = cluster.x-point.x;
        int y = cluster.y-point.y;
        return Math.sqrt(x*x + y*y);
    }
}
