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

/**
 *
 * @author binderchri
 */
public class KmeansLsh {
    private int _dimensions = 10;
    private int _clustersCount = 15;
    
    private Random _random = new Random(100);
    
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
    
    private void run() throws IOException {
        DataPoint[] data = readData("/tmp/lsh.csv");
        createHashes(data);
        
        getInitialClusterCenters(data, 1000);
    }
    
    private void createHashes(DataPoint[] data) {
        Random rnd = getRandom();
        
        Hasher[] hashers = {
          new Hasher(_dimensions, rnd),
          new Hasher(_dimensions, rnd),
          new Hasher(_dimensions, rnd)    
        };
        
        for(DataPoint d : data) 
            d.setBuckets(hashers);
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
    
    private ClusterCenter[] getInitialClusterCenters(DataPoint[] dataPoints, int pointsCount) {
        // take random subset of size pointsCount of dataPoints
        // take any point as first cluster center
        // find the point most far away and use it as second cluster center
        // find next point, which is most far away from already selected points
        // repeat this step untiel the specified amount of clusters is reached
        
        Random rnd = getRandom();
        
        List<DataPoint> samples = rnd.ints(pointsCount, 0, dataPoints.length).mapToObj(i -> dataPoints[i]).collect(Collectors.toList());
        ArrayList<ClusterCenter> chosenPoints = new ArrayList<ClusterCenter>(_clustersCount);
        
        DataPoint chosenPoint = dataPoints[rnd.nextInt(dataPoints.length)];
        chosenPoints.add(new ClusterCenter(chosenPoint));
        
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
            chosenPoints.add(new ClusterCenter(max));
        }
        
        return chosenPoints.toArray(new ClusterCenter[chosenPoints.size()]);
    }
    
    
}
