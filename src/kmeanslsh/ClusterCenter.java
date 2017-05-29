package kmeanslsh;

import java.util.ArrayList;

/**
 *
 * @author Gruppe10
 */
public class ClusterCenter extends AbstractPoint {
    int _id;
    static int id_counter = 0;
    
    ArrayList<DataPoint> _dataPoints = new ArrayList<>();
    
    public ClusterCenter(double[] values/*, int id*/) {
        super(values);    
        _id = id_counter ++;
    }
    /*
    public ClusterCenter(AbstractPoint other) {
        super(other);
        
        if(other instanceof ClusterCenter)
            _id = ((ClusterCenter)other)._id;
    }*/
    
    public void assignPointToCenter(DataPoint dp) {
        _dataPoints.add(dp);
    }
    
    public void removePointFromCenter(DataPoint dp) {
        _dataPoints.remove(dp);
    }
}
