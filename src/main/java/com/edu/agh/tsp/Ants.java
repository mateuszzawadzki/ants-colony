package com.edu.agh.tsp;

import com.edu.agh.model.Ant;
import com.edu.agh.model.XY;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Ants {

    private List<XY> coordinates;
    private double[][] distances;
    private double[][] pheromones;
    private int citiesNo;
    private Ant[] ants;
    private int antsNo = 30;
    private int a = 3;
    private int b = 5;
    private int Q = 100;
    private double dec = 0.5;

    public Ants(List<XY> coordinates) {
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

    private void moveAnts() {
        for(Ant ant : ants) {
            moveAnt(ant);
        }
    }

    private void moveAnt(Ant ant) {
        int city = getNextCity(ant);
        ant.visited[city] = true;
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
        return -1;
    }

    private double[] getProbabilities(Ant ant) {
        double[] probabilities = new double[citiesNo];
        for(int i = 0; i < citiesNo; i++) {
            if(ant.visited[i]) {
                probabilities[i] = 0.0;
            } else {
                probabilities[i] = getProbability(ant, i);
            }
        }
        return probabilities;
    }

    private double getProbability(Ant ant, int city) {
        double divisor = 0.0;
        for(int i = 0; i < citiesNo; i++) {
            if(ant.visited[i]){
                continue;
            }
            divisor += Math.pow(pheromones[ant.currentCity][i], a) * Math.pow(distances[ant.currentCity][i], -b);
        }
        double probability = Math.pow(pheromones[ant.currentCity][city], a) * Math.pow(distances[ant.currentCity][city], -b);
        if(divisor > 0.0) {
            return probability / divisor;
        }
        return probability;
    }

    private void updatePheromone() {
        for (int i = 0; i < citiesNo; i++) {
            for (int j = 0; j < citiesNo; j++) {
                pheromones[i][j] *= dec;
            }
        }

        for (Ant ant : ants) {
            double contribution = Q / getRouteLength(ant);
            if(ant.route.size() <= 1) break;
            for (int i = 0; i < ant.route.size() - 1; i++) {
                pheromones[ant.route.get(i)][ant.route.get(i+1)] += contribution;
            }
            //pheromones[ant.route.get(citiesNo-1)][ant.route.get(0)] += contribution;
        }
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
            path += distances[route.get(i)][route.get(i+1)];
        }
        if(route.size() == citiesNo) {
            path += distances[route.get(0)][route.get(citiesNo-1)];
        }
        return path;
    }

    private void transformToMatrix() {
        int size = coordinates.size();
        this.citiesNo = size;
        distances = new double[size][size];
        for(int i = 0; i < size; i++) {
            for(int j = i; j < size; j++) {
                if(i == j) continue;
                distances[i][j] = distances [j][i] = calculateDistance(coordinates.get(i), coordinates.get(j));
            }
        }
    }

    private double calculateDistance(XY xy1, XY xy2) {
        return Math.sqrt(Math.pow(xy1.x - xy2.x, 2) + Math.pow(xy1.y - xy2.y,2));
    }

    private void initialize() {
        pheromones = new double[citiesNo][citiesNo];
        for(int i = 0; i < citiesNo; i++) {
            for(int j = 0; j < citiesNo; j++) {
                pheromones[i][j] = 0.1;
            }
        }
        ants = new Ant[antsNo];
        for(int i = 0; i < antsNo; i++) {
            ants[i] = new Ant(citiesNo);
            int randomCity = ThreadLocalRandom.current().nextInt(0, citiesNo);
            ants[i].visited[randomCity] = true;
            ants[i].currentCity = randomCity;
            ants[i].route.add(randomCity);
        }
    }
}
