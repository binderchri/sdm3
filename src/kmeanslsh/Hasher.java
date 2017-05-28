/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeanslsh;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author binderchri
 */
public class Hasher {
    private final int _dimensions;
    private double[] _hash;
    
    
    public Hasher(int dimensions, Random r) {
        _dimensions = dimensions;
        
        _hash = IntStream.range(0, dimensions)
                .asDoubleStream()
                .map(i -> r.nextGaussian())
                .toArray();
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
}
