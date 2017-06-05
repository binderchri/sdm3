package kmeanslsh;

public class DataPoint extends AbstractPoint {
    int _trueClassification;
    private ClusterCenter _cluster;    
    
    public DataPoint(double[] points, int trueClassification) {
        super(points);
        _trueClassification = trueClassification;
    }
    
    /**
     * 
     * @param cluster
     * @return true if the cluster has changed
     */
    public boolean setCluster(ClusterCenter cluster) {
        if(_cluster == cluster)
            return false;
        
        if(_cluster != null)
            _cluster.removePointFromCenter(this);
        
        if(cluster != null)
            cluster.assignPointToCenter(this);
        
        _cluster = cluster;
        return true;
    }
    
    public ClusterCenter getCluster() {
        return _cluster;
    }
}
