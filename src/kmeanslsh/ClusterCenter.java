package kmeanslsh;

import java.util.ArrayList;

public class ClusterCenter extends AbstractPoint {
    int _id;
    static int id_counter = 0;
    
    ArrayList<DataPoint> _dataPoints = new ArrayList<>();
    
    public ClusterCenter(double[] values) {
        super(values);    
        _id = id_counter ++;
    }
    
    public void assignPointToCenter(DataPoint dp) {
        _dataPoints.add(dp);
    }
    
    public void removePointFromCenter(DataPoint dp) {
        _dataPoints.remove(dp);
    }
}
