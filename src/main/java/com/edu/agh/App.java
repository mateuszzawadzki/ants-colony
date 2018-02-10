package com.edu.agh;

import com.edu.agh.clustering.KmeansND4J;
import com.edu.agh.model.XY;
import com.edu.agh.tsp.AntsND4J;

import java.awt.*;
import java.util.*;
import java.awt.geom.*;
import java.io.*;
import java.util.List;
import javax.swing.*;

public class App extends JPanel {
    final static int PAD = 20;
    final static int WIDTH = 384;
    final static int HEIGHT = 361;


    static int MAX;
    static int kNo = 0;
    static XY[] cities;
    static Map<Integer, List<XY>> clusters;
    static Map<Integer, List<XY>> routes;
    static Color[] colors;
    static boolean clusterAction = false;
    static boolean routeAction = false;
    static XY depot;

    static boolean added = false;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        XY[] array = cities;
        MAX = getMax(array);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.draw(new Line2D.Double(PAD, PAD, PAD, HEIGHT-PAD));
        g2.draw(new Line2D.Double(PAD, HEIGHT-PAD, WIDTH-PAD, HEIGHT-PAD));


        g2.setPaint(Color.black);
        XY depot = array[0];
        App.depot = depot;
        g2.fill(new Ellipse2D.Double(depot.x-4, depot.y-4, 8, 8));


        if(!clusterAction && !routeAction) {
            paintPoints(g2, array);
        }
        if(clusterAction) {
            paintCentroids(g2, getClusters(array));
        }
        if(clusterAction || routeAction) {
            paintClusteredPoints(g2);
        }
        if (routeAction) {
            paintRoutes(g2);
        }
        clusterAction = false;
        routeAction = false;
    }

    private XY[] getClusters(XY[] array) {
        added = false;
        KmeansND4J kmeans = new KmeansND4J(WIDTH, HEIGHT, PAD);
        clusters = kmeans.cluster(array, kNo);
        return kmeans.getCentroids();
    }

    private void paintPoints(Graphics2D g2, XY[] array) {
        g2.setPaint(Color.red);
        for (int i = 1; i < array.length; i++) {
            g2.fill(new Ellipse2D.Double(array[i].x, array[i].y, 4, 4));
        }
    }

    private void paintCentroids(Graphics2D g2, XY[] centroids) {
        g2.setPaint(Color.GREEN);
        for (int i = 0; i < centroids.length; i++) {
            g2.fill(new Ellipse2D.Double(centroids[i].x - 2, centroids[i].y - 2, 4, 4));
        }
    }

    private void paintClusteredPoints(Graphics2D g2) {
        for(Map.Entry<Integer, List<XY>> cluster : clusters.entrySet()) {
            g2.setPaint(colors[cluster.getKey()]);
            for(int i = 0; i < cluster.getValue().size(); i++) {
                g2.fill(new Ellipse2D.Double(cluster.getValue().get(i).x - 4, cluster.getValue().get(i).y - 4, 8, 8));
            }

        }
    }

    private void paintRoutes(Graphics2D g2) {
        for(Map.Entry<Integer, List<XY>> route : routes.entrySet()) {
            g2.setPaint(colors[route.getKey()]);
            List<XY> points = route.getValue();
            g2.draw(new Line2D.Double(points.get(0).x, points.get(0).y, points.get(points.size()-1).x, points.get(points.size()-1).y));
            for(int i = 0; i < points.size() - 1; i++) {
                g2.draw(new Line2D.Double(points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y));
            }
        }
    }

    private static void generateColors() {
        colors = new Color[kNo];
        for (int i = 0; i < kNo; i++) {
            colors[i] = new Color((int) (Math.random() * 0x1000000));
        }
    }

    private void scaleCoordinates(XY[] array, int height) {
        MAX = getMax(array);
        double scale = (double)(height - 2*PAD)/MAX;
        for(int i = 0; i < array.length; i++) {
            array[i].x = (int) (PAD + scale*array[i].x);
            array[i].y = (int) (height - PAD - scale*array[i].y);
        }
    }

    private int getMax(XY[] array) {
        int max = -Integer.MAX_VALUE;
        for(int i = 0; i < array.length; i++) {
            if(array[i].x > max)
                max = array[i].x;
            if(array[i].y > max)
                max = array[i].y;
        }
        return max;
    }

    private void readFile(){
        String fileName = "resources//berlin52";
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(fileName));
        } catch (IOException e) {

        }
        int size = scanner.nextInt();
        XY array[] = new XY[size];
        for(int i = 0; i < size; i++) {
            scanner.nextInt();
            array[i] = new XY(scanner.nextInt(), scanner.nextInt());
        }
        scanner.close();
        scaleCoordinates(array, HEIGHT);
        cities = array;
    }

    private static void generateRoute() {
        routes = new HashMap<>();
        for(Map.Entry<Integer, List<XY>> cluster : clusters.entrySet()) {
            List<XY> clusteredXY = cluster.getValue();
            if(!added) {
                clusteredXY.add(0, depot);
            }
            routes.put(cluster.getKey(), new AntsND4J(new LinkedList<>(clusteredXY)).getRoute());
            Collections.sort(routes.get(cluster.getKey()));
        }
        added =true;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        App app = new App();
        app.readFile();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField clusterNumber = new JTextField();
        clusterNumber.setBounds(420,20, 80,20);
        f.add(clusterNumber);

        JButton generateRouteButton = new JButton("Trasa");
        generateRouteButton.setBounds(420,100,80,30);
        generateRouteButton.addActionListener((e) -> {
            generateRoute();
            routeAction = true;
            app.repaint();
            f.revalidate();
        });
        f.add(generateRouteButton);

        JButton generateClusterButton = new JButton("Klastry");
        generateClusterButton.setBounds(420,60,80,30);
        generateClusterButton.addActionListener((e) -> {
            int previousK = kNo;
            kNo = Integer.parseInt(clusterNumber.getText());
            if(previousK != kNo) {
                generateColors();
            }
            clusterAction = true;
            app.repaint();
            f.revalidate();
        } );
        f.add(generateClusterButton);

        f.add(app);
        f.setSize(550,400);
        f.setLocation(200,200);
        f.setVisible(true);
    }
}