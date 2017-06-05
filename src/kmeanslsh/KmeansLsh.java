package kmeanslsh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class KmeansLsh {
    private final int _dimensions = 10;
    private final int _clustersCount = 15;
    
    private DataPoint[] _dataPoints;
    private ClusterCenter[] _clusters;
    
    private final int _hashesCount = 16;
    private int _bucketsCount = -1;
    
    private Hashing _hashing;
    private int _comparison = 1;
    private Hasher[] _hashers;
    
    public static boolean s_verboseOutput = false;
        
    private String _filename = "/work/lsh.csv";
    
    public static void main(String[] args) {
        KmeansLsh app = new KmeansLsh();
        try {
            if(app.parseArguments(args)) {
                app.run();
            }
        } catch (Exception ex) {
            System.err.println("ERROR:" + ex.toString());
        }
    }
    
    private boolean parseArguments(String[] args) throws Exception {
        if(args.length == 0) {
            printHelp();
            return false;
        }
        
        boolean nextIsFilename = false;
        for(String arg : args) {
            if(nextIsFilename) {
                _filename = arg;
                nextIsFilename = false;
            }
            
            if("-v".equals(arg))
                s_verboseOutput = true;
            else if(arg.startsWith("-b")) {
                // e.g. -b100 for 100 buckets
                _bucketsCount = Integer.parseInt(arg.substring(2));
            } else if(arg.startsWith("-c")) {
                // e.g. -c1
                _comparison = Integer.parseInt(arg.substring(2));
            } else if(arg.startsWith("-f")) {
                nextIsFilename = true;
            } else if(arg.startsWith("-h")) {
                printHelp();
                return false;
            }
        }
        
        if(nextIsFilename)
            throw new Exception("After -f parameter, the filename must follow.");
        
        return true;
    }
    
    private void printHelp() {
        System.out.println("KmeansLsh uses the following parameters:");
        System.out.println("  -b<bucketCount> ... eg. -b100, when bucketCount=2, then R+/R- bucket assignment is used (required parameter)");
        System.out.println("  -c<comparison> ... eg. -c1 ... possible values: 1=2x2, 2=4x4, 3=8x2, default is 1");
        System.out.println("  -f <filename> ... default is /work/lsh.csv");
        System.out.println("  -h ... prints this help");
    }
    
    private void run() throws IOException, Exception {
        _hashing = new Hashing(_comparison);
        
        createHashers();
        
        _dataPoints = readData(_filename);
        normalizeDataPoints(_dataPoints);
        
        if(s_verboseOutput) {
            calculateBuckets(_dataPoints);
            for(int i = 0; i < 20 ; i++)
                System.out.println(_dataPoints[i]);
        }
        
        long startTime = System.nanoTime();
                
        _clusters = getInitialClusterCenters();       
        
        calculateBuckets(_dataPoints);
        calculateBuckets(_clusters);
        
        if(s_verboseOutput)
            printBuckets(_clusters);
        
        boolean centersChanged = true;
        int iterations = 0;
        int maxIterations = 500;
        
        while(centersChanged && ++iterations < maxIterations) {
            centersChanged = assignPointsToCenters();
            
            recalculateClusterCenters(_clusters);
            calculateBuckets(_clusters);
          
            if(s_verboseOutput && iterations % 5 == 0)
                System.out.println("Iteration" + iterations);
        }
        
        long endTime = System.nanoTime();
        double elapsedSeconds = (endTime - startTime) / 1E9;
        
        double nmi = NmiCalculator.calculateNmi(_dataPoints);
        System.out.println(
                "Iterations:" + iterations +
                ";  Converged:" + !centersChanged +
                ";  NMI:" + nmi +
                ";  Elapsed_seconds:" + elapsedSeconds +
                ";  Distance_calculations:" + _lastFullDistanceCalculationCount +
                ";  Datapoint_count:" + _dataPoints.length +
                ";  Buckets_count:" + _bucketsCount
        );
    }
    
    private void createHashers() throws Exception {
        if(_bucketsCount < 0)
            throw new Exception("-b parameter must be used to set the amount of buckets, e.g. -b100");
        
        Random rnd = getRandom();
        _hashers = IntStream.range(0, _hashesCount).boxed().map(i -> new Hasher(_dimensions, rnd, _bucketsCount)).toArray(Hasher[]::new);
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
    
    //Random _random = new Random(100);
    private Random getRandom() {
        //return _random; // for reproducible results
        return new SecureRandom();
    }
    
    private ClusterCenter[] getInitialClusterCenters() {
        // take any point as first cluster center
        // find the point most far away and use it as second cluster center
        // find next point, which is most far away from already selected points
        // repeat this step until the specified amount of clusters is reached
        // the first cluster center will be removed from the list as it is chosen randomly 
        //   and we prefer to find points which are lying on the border of the data
        
        Random rnd = getRandom();
        
        ArrayList<ClusterCenter> clusterCenters = new ArrayList<>(_clustersCount);
        
        DataPoint randomDataPoint = _dataPoints[rnd.nextInt(_dataPoints.length)];
        ClusterCenter firstCluster = new ClusterCenter(randomDataPoint._values.clone());
        clusterCenters.add(firstCluster);
        
        while(clusterCenters.size() < _clustersCount) {
            DataPoint max = null;
            double maxDistance = 0;
            
            // select point with max distance to all selected points
            for(DataPoint d : _dataPoints) {
                double distance = clusterCenters.stream().mapToDouble(p -> p.getDistance(d)).sum();
                if(distance > maxDistance) {
                    maxDistance = distance;
                    max = d;
                }
            }
            
            ClusterCenter clusterCenter = new ClusterCenter(max._values.clone());
            clusterCenter._id = clusterCenters.size();
            clusterCenters.add(clusterCenter);
            
            if(clusterCenters.contains(firstCluster)) {
                clusterCenters.remove(firstCluster);
            }
        }
        
        return clusterCenters.toArray(new ClusterCenter[clusterCenters.size()]);
    }
        
    int _lastFullDistanceCalculationCount = -1;
    private boolean assignPointsToCenters() {
        _lastFullDistanceCalculationCount = 0;
        int clustersChanged = 0;
        
        for(DataPoint dp : _dataPoints) {
            ClusterCenter center = null;
            center = findCenterWithHashing(dp);
            
            if(center == null) {
                center = findCenterWithDistance(dp);
                _lastFullDistanceCalculationCount ++;
            } 
                        
            if(dp.setCluster(center))  {
                clustersChanged ++;
            }
        }
        
        if(s_verboseOutput)
            System.out.println("full dist calc:" + _lastFullDistanceCalculationCount + ", total:" + _dataPoints.length + ", cluster changed:" + clustersChanged);
        
        boolean anyClusterChanged = clustersChanged > 0;
        return anyClusterChanged;
    }
    
    private ClusterCenter findCenterWithHashing(DataPoint dp) {
        // First check currently assigned cluster, maybe it's still in the same bucket
        ClusterCenter currentCluster = dp.getCluster();
        if(currentCluster != null && _hashing.isSameBucket(dp, currentCluster))
            return currentCluster;
        
        for(ClusterCenter cluster : _clusters) {
            if(cluster == currentCluster)
                continue; // This is already checked
            
            if(_hashing.isSameBucket(dp, cluster))
                return cluster;
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
    
    private void recalculateClusterCenters(ClusterCenter[] clusterCenters) {
        for(ClusterCenter center : clusterCenters) {
            for(int d = 0; d < _dimensions; d++) {
                final int fd = d; // must be final for lambda expression
                double sum = center._dataPoints.stream().mapToDouble(dp -> dp.getValue(fd)).sum();
                double avg = sum / center._dataPoints.size();
                center.setValue(d, avg);
            }
        }
    }
    
    private void normalizeDataPoints(DataPoint[] dataPoints) {
        for(int d = 0; d < _dimensions; d++)
            normalizeDataPoints(d, dataPoints);
        
        if(s_verboseOutput) 
            System.out.println("Normalized data");
    }
    
    private void normalizeDataPoints(int dimension, DataPoint[] dataPoints) {
        // https://stats.stackexchange.com/questions/70801/how-to-normalize-data-to-0-1-range
        
        boolean firstRound = true;
        double min = 0, max = 0;
        
        for(DataPoint dp : dataPoints) {
            double value = dp.getValue(dimension);
            if(firstRound) {
                min = max = value;
                firstRound = false;
                continue;
            }
            
            if(value < min)
                min = value;
            
            if(value > max)
                max = value;
        }
        
        for(DataPoint dp : dataPoints) {
            double normalized = (dp.getValue(dimension) - min) / (max-min);
            dp.setValue(dimension, normalized);
        }
    }
}
