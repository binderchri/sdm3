package kmeanslsh;

/**
 *
 * @author Gruppe10
 */
public class ClusterCenter extends AbstractPoint {
    public ClusterCenter(double[] points) {
        super(points);    
    }
    
    public ClusterCenter(AbstractPoint other) {
        super(other);
    }
}
