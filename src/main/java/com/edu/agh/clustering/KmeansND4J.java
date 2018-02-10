package com.edu.agh.clustering;

import com.edu.agh.model.XY;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.NDArrayFactory;
import org.nd4j.linalg.factory.Nd4j;


import java.util.*;

public class KmeansND4J {

    private final int width;
    private final int height;
    private final int pad;
    private INDArray centroidsND;
    private int kNo;
    private boolean[] anomalies;

    public KmeansND4J(int width, int height, int pad) {
        this.width = width;
        this.height = height;
        this.pad = pad;
    }

    public XY[] getCentroids() {
        XY[] centroids = new XY[kNo];
        for (int i = 0; i < centroidsND.rows(); i++) {
            centroids[i] = rowToXY(centroidsND.getRow(i));
        }
        return centroids;
    }

    private XY rowToXY(INDArray row) {
        return new XY(row.getInt(0, 0), row.getInt(0, 1));
    }

    public Map<Integer, List<XY>> cluster(XY[] array, int kNo) {
        this.kNo = kNo;
        int iterations = 20;
        INDArray centroidsND = Nd4j.zeros(kNo, 2);
        INDArray arrayND = new NDArray(array.length, 3);
        for (int i = 0; i < array.length; i++) {
            arrayND.putRow(i, Nd4j.create(array[i].toNDCluster(), new int[]{1, 3}));
        }
        Random random = new Random();
        for (int i = 0; i < kNo; i++) {
            centroidsND.putScalar(i, 0, random.nextInt(width - pad + 1) + pad);
            centroidsND.putScalar(i, 1, random.nextInt(height - pad + 1) + pad);
        }
        findAnomalies(arrayND);
        Map<Integer, List<INDArray>> clustersND = null;
        for (int i = 0; i < iterations; i++) {
            clustersND = new HashMap<>();
            for (int c = 0; c < kNo; c++) {
                clustersND.put(c, new ArrayList<>());
            }
            for (int j = 1; j < array.length; j++) {
                if(anomalies[j]){
                    continue;
                }
                int min = Integer.MAX_VALUE;
                for (int l = 0; l < kNo; l++) {
                    if (distanceND(centroidsND.getRow(l), arrayND.getRow(j)) < min) {
                        min = (int) distanceND(centroidsND.getRow(l), arrayND.getRow(j));
                        arrayND.put(j, 2, l);
                        clustersND.get(l).add(arrayND.getRow(j));
                        for (Map.Entry<Integer, List<INDArray>> cluster : clustersND.entrySet()) {
                            if (cluster.getKey() != l && cluster.getValue().contains(arrayND.getRow(j))) {
                                cluster.getValue().remove(arrayND.getRow(j));
                            }
                        }
                    }
                }
            }
            for (Map.Entry<Integer, List<INDArray>> cluster : clustersND.entrySet()) {
                meanCoordinates(cluster.getValue(), centroidsND.getRow(cluster.getKey()));
            }
        }
        assignAnomalies(arrayND, centroidsND, clustersND);
        this.centroidsND = centroidsND;
        return toXYMap(clustersND);
    }

    private Map<Integer, List<XY>> toXYMap(Map<Integer, List<INDArray>> mapND) {
        Map<Integer, List<XY>> clusters = new HashMap<>();
        for (Map.Entry<Integer, List<INDArray>> cluster : mapND.entrySet()) {
            clusters.put(cluster.getKey(), toXYList(cluster.getValue()));
        }
        return clusters;
    }

    private List<XY> toXYList(List<INDArray> listND) {
        List<XY> list = new ArrayList<>();
        for (int i = 0; i < listND.size(); i++) {
            list.add(new XY(listND.get(i).getInt(0, 0), listND.get(i).getInt(0, 1), listND.get(i).getInt(0, 2)));
        }
        return list;
    }

    private void meanCoordinates(List<INDArray> coordinates, INDArray centroid) {
        int size = coordinates.size();
        if (size == 0) {
            return;
        }
        NDArray coordinatesND = new NDArray(coordinates.size(), 3);
        for (int i = 0; i < size; i++) {
            coordinatesND.putRow(i, coordinates.get(i));
        }

        double x = (Double) coordinatesND.getColumn(0).sumNumber();
        double y = (Double) coordinatesND.getColumn(1).sumNumber();

        centroid.putScalar(0, 0, x / size);
        centroid.putScalar(0, 1, y / size);
    }

    private double distanceND(INDArray cluster, INDArray point) {
        double x = cluster.getDouble(0, 0) - point.getFloat(0, 0);
        double y = cluster.getDouble(0, 1) - point.getFloat(0, 1);
        return Math.sqrt(x * x + y * y);
    }

    private void findAnomalies(INDArray arrayND) {
        int avgx = arrayND.getColumn(0).meanNumber().intValue();
        int avgy = arrayND.getColumn(1).meanNumber().intValue();

        int rows = arrayND.rows();
        INDArray means = Nd4j.zeros(rows, 1);
        INDArray mean = Nd4j.zeros(1, 2);

        mean.putScalar(0, 0, avgx);
        mean.putScalar(0, 1, avgy);

        for (int i = 0; i < rows; i++) {
            means.putScalar(i, distanceND(arrayND.getRow(i), mean));
        }

        int avgDistance = means.meanNumber().intValue();

        anomalies = new boolean[rows];
        for (int i = 0; i < rows; i++) {
            if (3 * avgDistance < means.getInt(i)) {
                anomalies[i] = true;
                System.out.println("Znaleziono anomalie");
            }
        }
    }

    private void assignAnomalies(INDArray arrayND, INDArray centroidsND, Map<Integer, List<INDArray>> clustersND) {
        for(int i = 0; i < anomalies.length; i++) {
            if(anomalies[i]) {
                int min = Integer.MAX_VALUE;
                for (int l = 0; l < kNo; l++) {
                    if (distanceND(centroidsND.getRow(l), arrayND.getRow(i)) < min) {
                        min = (int) distanceND(centroidsND.getRow(l), arrayND.getRow(i));
                        arrayND.put(i, 2, l);
                    }
                }
                clustersND.get(arrayND.getInt(i,2)).add(arrayND.getRow(i));
            }
        }
    }
}
