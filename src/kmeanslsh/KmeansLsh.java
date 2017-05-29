/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeanslsh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author binderchri
 */
public class KmeansLsh {
    private int _dimensions = 10;
    private int _clustersCount = 15;
    
    private Random _random = new Random(100);
    
    private DataPoint[] _dataPoints;
    private ClusterCenter[] _clusters;
    
    private int _hashesCount = 10;
    
    private Hashing _hashing = new Hashing();
    private Hasher[] _hashers;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        KmeansLsh app = new KmeansLsh();
        try {        
            app.run();
        } catch (IOException ex) {
            Logger.getLogger(KmeansLsh.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Read data");
    }
    
    private void run1() throws IOException {
        // just to test the orig result
       Path path = Paths.get("/work/out.txt");
        
       int[] one =
               Files
                .lines(path)
                .mapToInt(line -> Integer.parseInt(line))
                .toArray();
       
       
       path = Paths.get("/work/inclasses.txt");
       int[] two =
               Files
                .lines(path)
                .mapToInt(line -> (int)Double.parseDouble(line))
                .toArray();
       
       
       ArrayList<Integer> oneL = new ArrayList<>();
       ArrayList<Integer> twoL = new ArrayList<>();
       
       for(int i : one)
           oneL.add(i);
       
       for(int i : two)
           twoL.add(i);
       
       NmiCalculator.NMI(oneL, twoL);
    }
    
    private void run() throws IOException {
        createHashers();
        
        _dataPoints = readData("/tmp/lsh.csv");
        calculateBuckets(_dataPoints);
        
        _clusters = getInitialClusterCenters(1000);       
        calculateBuckets(_clusters);
        
        printBuckets(_clusters);
        
        boolean centersChanged = true;
        int iterations = 0;
        int maxIterations = 1000;
        
        while(centersChanged && iterations++ < maxIterations) {
            centersChanged = assignPointsToCenters();
            recalculateClusterCenters();
            calculateBuckets(_clusters);
          
            if(iterations % 5 == 0)
                System.out.println("Iteration" + iterations);


//            double nmi = NmiCalculator.calculateNmi(_dataPoints);
  //          System.out.println("Iterations:" + iterations + ", NMI:" + nmi);
            
            //printBuckets(_clusters);
        }
        
        double nmi = NmiCalculator.calculateNmi(_dataPoints);
        System.out.println("Iterations:" + iterations + ", NMI:" + nmi);
        
    }
    
    private void createHashers() {
        Random rnd = getRandom();
        _hashers = IntStream.range(0, _hashesCount).boxed().map(i -> new Hasher(_dimensions, rnd)).toArray(Hasher[]::new);
    }
    
    private void calculateBuckets(AbstractPoint[] data) {
        for(AbstractPoint d : data) 
            d.setBuckets(_hashers);
    }
    
    private DataPoint[] readData(String inputFilename) throws IOException {
        Path path = Paths.get(inputFilename);
        
        return Files
                .lines(path)
                .map(line -> createDataPoint(line))
                .toArray(DataPoint[]::new);
    }
    
    private DataPoint createDataPoint(String line) {
        String delimiter = ",";
        
        String[] parts = line.split(delimiter);
        String classificationPart = parts[parts.length - 1];
        
        String[] dataParts = Arrays.copyOfRange(parts, 0, parts.length - 1);
        
        double[] data = Arrays.stream(dataParts).mapToDouble(Double::parseDouble).toArray();
        int classification = (int)Double.parseDouble(classificationPart.replaceAll("class", ""));
                
        return new DataPoint(data, classification);
    }
    
    private Random getRandom() {
        return _random; // for reproducible results
    }
    
    private ClusterCenter[] getInitialClusterCenters(int pointsCount) {
        // take random subset of size pointsCount of dataPoints
        // take any point as first cluster center
        // find the point most far away and use it as second cluster center
        // find next point, which is most far away from already selected points
        // repeat this step untiel the specified amount of clusters is reached
        
        Random rnd = getRandom();
        
        List<DataPoint> samples = rnd.ints(pointsCount, 0, _dataPoints.length).mapToObj(i -> _dataPoints[i]).collect(Collectors.toList());
        ArrayList<ClusterCenter> chosenPoints = new ArrayList<>(_clustersCount);
        
        DataPoint chosenPoint = _dataPoints[rnd.nextInt(_dataPoints.length)];
        chosenPoints.add(new ClusterCenter(chosenPoint._values.clone()));
        
        while(chosenPoints.size() < _clustersCount) {
            DataPoint max = null;
            double maxDistance = 0;
            
            // select point with max distance to all selected points
            for(DataPoint d : samples) {
                double distance = chosenPoints.stream().mapToDouble(p -> p.getDistance(d)).sum();
                if(distance > maxDistance) {
                    maxDistance = distance;
                    max = d;
                }
            }
            
            samples.remove(max);
            ClusterCenter clusterCenter = new ClusterCenter(max._values.clone());
            clusterCenter._id = chosenPoints.size();
            chosenPoints.add(clusterCenter);
        }
        
        return chosenPoints.toArray(new ClusterCenter[chosenPoints.size()]);
    }
    
    private boolean assignPointsToCenters() {
        //TODO: make this fast
        
        //ArrayList<DataPoint> withoutCenter = new ArrayList<>();
        boolean anyClusterChanged = false;
        int fullDistanceCalculationCount = 0;
        int clustersChanged = 0;
        
        // HASHING
        for(DataPoint dp : _dataPoints) {
            ClusterCenter center = findCenterWithHashing(dp);
            
            if(center == null) {
                center = findCenterWithDistance(dp);
                fullDistanceCalculationCount ++;
            } 
            
            if(dp.setCluster(center))  {
                anyClusterChanged = true;
                clustersChanged ++;
            }
        }
        
        System.out.println("full dist calc:" + fullDistanceCalculationCount + ", total:" + _dataPoints.length + ", cluster changed:" + clustersChanged);
        
        return anyClusterChanged;
    }
    
    private ClusterCenter findCenterWithHashing(DataPoint dp) {
        for(ClusterCenter center : _clusters) {
            if(_hashing.isSameBucket(dp, center))
                return center;
        }
        
        return null;
    }
    
    private ClusterCenter findCenterWithDistance(DataPoint dp) {
        ClusterCenter nearest = null;
        double nearestDistance = 0;
        
        for(ClusterCenter center : _clusters) {
            double distance = center.getDistance(dp);
            
            if(nearest == null || distance < nearestDistance) {
                nearest = center;
                nearestDistance = distance;
            }
        }
        
        return nearest;
    }
    
    private void printBuckets(AbstractPoint[] points) {
        for(AbstractPoint p : points) {
            int id = -1;
            if(p instanceof ClusterCenter)
                id = ((ClusterCenter)p)._id;
            
            System.out.println(id + " ... " + Arrays.toString(p._buckets));
        }
        
    }
    
    private void recalculateClusterCenters() {
        for(ClusterCenter center : _clusters) {
            for(int d = 0; d < _dimensions; d++) {
                final int fd = d; // must be final for lambda expression
                double sum = center._dataPoints.stream().mapToDouble(dp -> dp.getValue(fd)).sum();
                double avg = sum / center._dataPoints.size();
                center.setValue(d, avg);
            }
        }
        
        
    }
    
    
    
}
