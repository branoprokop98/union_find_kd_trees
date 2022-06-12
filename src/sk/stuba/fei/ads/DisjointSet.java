package sk.stuba.fei.ads;

import net.sf.javaml.core.kdtree.KDTree;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

//https://github.com/mission-peace/interview/blob/master/src/com/interview/graph/DisjointSet.java
//http://java-ml.sourceforge.net/api/0.1.7/net/sf/javaml/core/kdtree/KDTree.html

public class DisjointSet {

    private Map<Point, Node> map = new HashMap<>();

    static class Point {
        double x;
        double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Point() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    class Node {
        Point data;
        Node parent;
        int rank;
    }

    static class UnionPoints {
        private Point p1;
        private Point p2;
        private double distance;

        public Point getP1() {
            return p1;
        }

        public void setP1(Point p1) {
            this.p1 = p1;
        }

        public Point getP2() {
            return p2;
        }

        public void setP2(Point p2) {
            this.p2 = p2;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        @Override
        public String toString() {
            return "UnionPoints{" +
                    "p1=" + p1 +
                    ", p2=" + p2 +
                    ", distance=" + distance +
                    "}";
        }
    }

    /**
     * Create a set with only one element.
     */
    public void makeSet(Point data) {
        Node node = new Node();
        node.data = data;
        node.parent = node;
        node.rank = 0;
        map.put(data, node);
    }

    /**
     * Combines two sets together to one.
     * Does union by rank
     *
     * @return true if data1 and data2 are in different set before union else false.
     */
    public boolean union(Point data1, Point data2) {
        Node node1 = map.get(data1);
        Node node2 = map.get(data2);
        Node parent1 = findSet(node1);
        Node parent2 = findSet(node2);
        //if they are part of same set do nothing
        if (parent1.data == parent2.data) {
            return false;
        }
        //else whoever's rank is higher becomes parent of other
        if (parent1.rank >= parent2.rank) {
            //increment rank only if both sets have same rank
            parent1.rank = (parent1.rank == parent2.rank) ? parent1.rank + 1 : parent1.rank;
            parent2.parent = parent1;
        } else {
            parent1.parent = parent2;
        }
        return true;
    }

    /**
     * Finds the representative of this set
     */
    public Point findSet(Point data) {
        return findSet(map.get(data)).data;
    }

    /**
     * Find the representative recursively and does path
     * compression as well.
     */
    private Node findSet(Node node) {
        Node parent = node.parent;
        if (parent == node) {
            return parent;
        }
        node.parent = findSet(node.parent);
        return node.parent;
    }

    public static List<KDTree> insert(Map<Point, Set<Point>> components) {
        List<KDTree> trees = new ArrayList<>();
        for (Set<Point> comps : components.values()) {
            KDTree kdTree = new KDTree(2);
            for (Point p : comps) {
                kdTree.insert(new double[]{p.x, p.y}, new double[]{p.x, p.y});
            }
            trees.add(kdTree);
        }
        return trees;
    }

    public static UnionPoints findNearest(KDTree tree, double[] node) {
        double[] nearest = (double[]) tree.nearest(new double[]{node[0], node[1]});
        double dist = Math.sqrt((node[1] - nearest[1]) * (node[1] - nearest[1]) +
                (node[0] - nearest[0]) * (node[0] - nearest[0]));
        UnionPoints unionPoints = new UnionPoints();
        unionPoints.setP1(new Point(node[0], node[1]));
        unionPoints.setP2(new Point(nearest[0], nearest[1]));
        unionPoints.setDistance(dist);
        return unionPoints;
    }

    public static void writeToFile(List<UnionPoints> nearest) {
        try {
            FileWriter myWriter = new FileWriter("grafy/out_18.txt");
            for (UnionPoints p : nearest) {
                int x1 = (int) p.p1.x;
                int y1 = (int) p.p1.y;

                int x2 = (int) p.p2.x;
                int y2 = (int) p.p2.y;

                myWriter.write("[" + x1 + "," + y1 + "] [" + x2 + "," + y2 + "]\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        DisjointSet ds = new DisjointSet();
        List<UnionPoints> points = new ArrayList<>();
        Set<Point> parentPoints = new HashSet<Point>();

        try {
            List<String> allLines = Files.readAllLines(Paths.get("grafy/ZAD4_graph_18.txt"));
            for (String line : allLines) {
                String[] words = line.split("\\s+");
                for (String word : words) {
                    String[] numbers = word.split(",");
                    int x = Integer.parseInt(numbers[0].substring(1));
                    int y = Integer.parseInt(numbers[1].substring(0, numbers[1].length() - 1));
                    ds.makeSet(new Point(x, y));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            List<String> allLines = Files.readAllLines(Paths.get("grafy/ZAD4_graph_18.txt"));
            for (String line : allLines) {
                String[] words = line.split("\\s+");
                Point p1 = new Point();
                Point p2 = new Point();
                for (int i = 0; i < words.length; i++) {
                    if (i == 0) {
                        String[] numbers = words[0].split(",");
                        p1.x = Integer.parseInt(numbers[0].substring(1));
                        p1.y = Integer.parseInt(numbers[1].substring(0, numbers[1].length() - 1));
                    } else {
                        String[] numbers = words[1].split(",");
                        p2.x = Integer.parseInt(numbers[0].substring(1));
                        p2.y = Integer.parseInt(numbers[1].substring(0, numbers[1].length() - 1));
                    }
                }
                ds.union(p1, p2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        Map<Point, Set<Point>> components = new HashMap<>();

        for (Point p : ds.map.keySet()) {
            Point parent = ds.findSet(p);
            parentPoints.add(parent);

            components.computeIfAbsent(parent, k -> new HashSet<>());
            components.get(parent).add(p);

        }

        List<KDTree> trees = insert(components);


        int size = components.size() - 1;
        List<UnionPoints> nearest = new ArrayList<>();
        long start = System.currentTimeMillis();

        for (int i = 0; i < size; i++) {
            KDTree tree = trees.get(0);
            for (Set<Point> comps : components.values()) {
                for (Point p : comps) {
                    if (tree.search(new double[]{p.x, p.y}) == null) {
                        UnionPoints unionPoints = findNearest(tree, new double[]{p.x, p.y});
                        points.add(unionPoints);
                    }else {
                        break;
                    }
                }
            }
            points.sort(new Comparator<UnionPoints>() {
                @Override
                public int compare(UnionPoints o1, UnionPoints o2) {
                    if (o1.getDistance() > o2.getDistance()) {
                        return 1;
                    } else if (o1.getDistance() < o2.getDistance()) {
                        return -1;
                    }
                    return 0;
                }
            });
            nearest.add(points.get(0));
            System.out.println(i + ": " + points.get(0));

            ds.union(points.get(0).p1, points.get(0).p2);
            components.clear();
            parentPoints.clear();
            for (Point p : ds.map.keySet()) {
                Point parent = ds.findSet(p);
                parentPoints.add(parent);

                components.computeIfAbsent(parent, k -> new HashSet<>());
                components.get(parent).add(p);
            }

            trees = insert(components);
            points.clear();

        }
        long end = System.currentTimeMillis();

        double count = 0;

        for (UnionPoints up : nearest) {
            count += up.distance;
        }
        writeToFile(nearest);

        long minutes = (end - start) / 1000 / 60;
        long seconds = (end - start) / 1000 % 60;

        System.out.println("Time: " + minutes + " min: " + seconds + " sec");
        System.out.println("Distance: " + String.format("%.12f", count));


    }
}
