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
public class DataPoint extends AbstractPoint {
    int _trueClassification;
    int _predictedClassification = -1;
        
    public DataPoint(double[] points, int trueClassification) {
        super(points);
        _trueClassification = trueClassification;
    }
}
