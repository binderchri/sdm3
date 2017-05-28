package kmeanslsh;

import java.util.Arrays;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author binderchri
 */
public abstract class AbstractPoint {
    protected double[] _values;
    protected int[] _buckets;
    
    public AbstractPoint(double[] values) {
        _values = values;
    }
    
    public AbstractPoint(AbstractPoint other) {
        _values = other._values;
        _buckets = other._buckets;
    }
    
    public void setBuckets(Hasher[] hashers) {
        _buckets = Arrays.stream(hashers).map(h -> h.hash(_values)).mapToInt(o -> (int)(double)o % 100).toArray();
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
}
