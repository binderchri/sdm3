package kmeanslsh;

import java.util.Random;
import java.util.stream.IntStream;

public class Hasher {
    private final int _dimensions;
    private double[] _hash;
        
    public Hasher(int dimensions, Random r, int bucketCount) {
        _dimensions = dimensions;
        
        _hash = IntStream.range(0, dimensions)
                .asDoubleStream()
                .map(i -> r.nextGaussian())
                .toArray();
        
        _bucketCountFactor = bucketCount / 2D;
        _useSignForBucket = bucketCount == 2;
    }
    
    public double hash(double[] values) {
        double result = 0;
        
        if(values.length != _dimensions)
            throw new RuntimeException("Dimensions don't fit for hashing");
        
        for(int i = 0; i < _dimensions; i ++) {
            result += _hash[i] * values[i];
        }
        
        return result;
    }
    
    private final double _bucketCountFactor;
    private final boolean _useSignForBucket;
    public int hashAndGetBucket(double[] values) {
        //returns a value from -bucketCountFactor to bucketCountFactor on normalized data
        
        double hashed = hash(values);
        
        int bucket;
        if(_useSignForBucket)
            bucket = hashed >= 0 ? 1 : 0; // used for R+/R- buckets
        else
            bucket = (int)(hashed / _dimensions * _bucketCountFactor); 
        
        return bucket;
    }
}
