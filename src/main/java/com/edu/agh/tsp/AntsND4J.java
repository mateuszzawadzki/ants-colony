package com.edu.agh.tsp;

import com.edu.agh.model.Ant;
import com.edu.agh.model.XY;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AntsND4J {

    private static final int XY_FIELDS = 3;

    private List<XY> coordinates;
    private INDArray coordinatesND;
    private INDArray distancesND;
    private INDArray pheromonesND;
    private INDArray bigArray;
    private int citiesNo;
    private Ant[] ants;
    private int antsNo = 30;
    private int a = 3;
    private int b = 5;
    private int Q = 100;
    private double dec = 0.5;

    public AntsND4J(List<XY> coordinates) {
        this.coordinates = coordinates;
    }

    public List<XY> getRoute() {
        transformToMatrix();
        initialize();
        for(int i = 1; i < citiesNo; i++) {
            moveAnts();
            updatePheromone();
        }
        return selectBestRoute();
    }

    private void transformToMatrix() {
        int size = coordinates.size();
        this.citiesNo = size;

        coordinatesND = Nd4j.zeros(citiesNo, XY_FIELDS);
        for(int i = 0; i < citiesNo; i++) {
            coordinatesND.getRow(i).assign(coordinates.get(i).toND());
        }

        distancesND = Nd4j.zeros(size, size);
        for(int i = 0; i < size; i++) {
            for(int j = i; j < size; j++) {
                if(i == j) continue;
                distancesND.putScalar(i, j, calculateDistance(
                        coordinatesND.getRow(i),
                        coordinatesND.getRow(j)));
                distancesND.putScalar(j, i, calculateDistance(
                        coordinatesND.getRow(j),
                        coordinatesND.getRow(i)));
            }
        }
    }

    private double calculateDistance(INDArray xy1, INDArray xy2) {
        return Math.sqrt(Math.pow(xy1.getFloat(0)- xy2.getFloat(0), 2)
                + Math.pow(xy1.getFloat(1)- xy2.getFloat(1), 2));
    }

    private void initialize() {
        pheromonesND = Nd4j.ones(citiesNo, citiesNo);
        calculateBigArray();
        ants = new Ant[antsNo];
        for(int i = 0; i < antsNo; i++) {
            ants[i] = new Ant(citiesNo);
            int randomCity = ThreadLocalRandom.current().nextInt(0, citiesNo);
            ants[i].visitedND.putScalar(randomCity, 0);
            ants[i].visited[randomCity] = true;
            ants[i].currentCity = randomCity;
            ants[i].route.add(randomCity);
        }
    }

    private void calculateBigArray() {
        bigArray = Transforms.pow(pheromonesND, a).muli(Transforms.pow(distancesND, -b));
        for(int i = 0; i < citiesNo; i++) {
            bigArray.putScalar(i,i, 0);
        }
    }

    private void moveAnts() {
        for(Ant ant : ants) {
            moveAnt(ant);
        }
    }

    private void moveAnt(Ant ant) {
        int city = getNextCity(ant);
        ant.visited[city] = true;
        ant.visitedND.putScalar(city, 0);
        ant.route.add(city);
        ant.currentCity = city;
    }

    private int getNextCity(Ant ant) {
        double[] probabilities = getProbabilities(ant);
        double random = Math.random();
        double cumulativeProb = 0.0;
        for(int i = 0; i < citiesNo; i++) {
            cumulativeProb += probabilities[i];
            if(cumulativeProb >= random) {
                return i;
            }
        }
        throw new IllegalStateException();
    }

    private double[] getProbabilities(Ant ant) {
        double[] probabilities = new double[citiesNo];
        for(int i = 0; i < citiesNo; i++) {
            if(ant.visitedND.getInt(i) == 0) {
                probabilities[i] = 0.0;
            } else {
                probabilities[i] = getProbability(ant, i);
            }
        }
        return probabilities;
    }

    private double getProbability(Ant ant, int city) {
        double divisor = ant.visitedND.mmul(bigArray.getRow(ant.currentCity).transpose()).getDouble(0);
        double probability = Math.pow(pheromonesND.getFloat(ant.currentCity, city), a)
                * Math.pow(distancesND.getFloat(ant.currentCity, city), -b);
        if(divisor > 0.0) {
            return probability / divisor;
        }
        return probability;
    }

    private void updatePheromone() {
        pheromonesND.mul(dec);

        for (Ant ant : ants) {
            double contribution = Q / getRouteLength(ant);
            if(ant.route.size() <= 1) break;
            for (int i = 0; i < ant.route.size() - 1; i++) {
                pheromonesND.getRow(ant.route.get(i)).getColumn(ant.route.get(i+1)).add(contribution);
            }
        }
        calculateBigArray();
    }

    private List<XY> selectBestRoute() {
        int shortestPath = Integer.MAX_VALUE;
        List<Integer> bestRoute = null;
        for(Ant ant : ants) {
            if (getRouteLength(ant) < shortestPath) {
                shortestPath = getRouteLength(ant);
                bestRoute = ant.route;
            }
        }
        for(int i = 0; i < citiesNo; i++) {
            coordinates.get(bestRoute.get(i)).order = i;
        }
        return coordinates;
    }

    private int getRouteLength(Ant ant) {
        int path = 0;
        List<Integer> route = ant.route;
        for(int i = 0; i < route.size() - 1; i++) {
            path += distancesND.getFloat(route.get(i), route.get(i+1));
        }
        if(route.size() == citiesNo) {
            path += distancesND.getFloat(route.get(0), route.get(citiesNo-1));
        }
        return path;
    }
}
