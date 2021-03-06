package kmeanslsh;

import java.util.Arrays;

public abstract class AbstractPoint {
    protected double[] _values;
    protected int[] _buckets;
    
    public AbstractPoint(double[] values) {
        _values = values;
    }
    
    public void setBuckets(Hasher[] hashers) {
        _buckets = Arrays.stream(hashers).mapToInt(h -> h.hashAndGetBucket(_values)).toArray();
    }
    
    public double getDistance(AbstractPoint other) {
        if(_values.length != other._values.length)
            throw new RuntimeException("Dimensions don't fit for getDistance().");
        
        double sum = 0;
        for(int i = 0; i < _values.length; i ++) {
            sum += Math.pow(_values[i] - other._values[i], 2);
        }
        
        return Math.sqrt(sum);
    }
    
    public double getValue(int dimension) {
        return _values[dimension];
    }
    
    public void setValue(int dimension, double value) {
        _values[dimension] = value;
    }

    @Override
    public String toString() {
        String result= "datapoint: Values:";
        for(double v : _values) {
            result += (v + ";");
        }
        
        if(_buckets == null) 
            return result;
        
        result += "  -- Buckets: ";
        
        for(int b : _buckets) {
            result += (b + ";");
        }
        
        return result;
    }
}
