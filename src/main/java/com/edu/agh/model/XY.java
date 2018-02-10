package com.edu.agh.model;


import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class XY implements Comparable<XY> {
    public int x;
    public int y;
    public int k;
    public int order;

    public XY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public XY(int x, int y, int k) {
        this.x = x;
        this.y = y;
        this.k = k;
    }

    @Override
    public String toString() {
        return "x: " + x + "y: " + y + " " + k + "\n";
    }

    @Override
    public int compareTo(XY o) {
        return Integer.compare(this.order, o.order);
    }

    public INDArray toND() {
        float[] array = new float[3];
        array[0] = x;
        array[1] = y;
        array[2] = order;
        return Nd4j.create(array, new int[]{1,3});
    }

    public float[] toNDCluster() {
        float[] array = new float[3];
        array[0] = x;
        array[1] = y;
        array[2] = k;
        return array;
    }
}
