package kmeanslsh;

import java.util.stream.IntStream;

public class Hashing {

    private final int _comparison;
    public Hashing(int comparison) throws Exception {
        this._comparison = comparison;
        
        if(comparison < 1 || comparison > 3)
            throw new Exception("Comparison type must be either 1,2 or 3");
    }
        
    // initialize arrays only once to gain performance
    
    // for 2x2
    int[] idx_01 = range(0,1);
    int[] idx_23 = range(2,3);
    
    // for 4x3
    int[] idx_012 = range(0,2);
    int[] idx_345 = range(3,5);
    int[] idx_678 = range(6,8);
    int[] idx_91011 = range(9,11);
    
    // for 4x4
    int[] idx_0_3 = range(0,3);
    int[] idx_4_7 = range(4,7);
    int[] idx_8_11 = range(8,11);
    int[] idx_12_15 = range(12,15);
    
    // Take care that the amount of available hashes is defined in:
    //   KmeansLsh.java, variable _hashesCount
    
    private static int[] range(int from, int to) {
        // e.g. range(1,3) creates [1,2,3]
        return IntStream.rangeClosed(from, to).toArray();
    }
    
    public boolean isSameBucket(AbstractPoint p1, AbstractPoint p2) {
        c.set(p1, p2);
        
        // Here you define the hash combination
        switch (_comparison) {
            case 1:
                // 2x2
                return c.and(idx_01) || c.and(idx_23);
            case 2:
                // 4x4
                return c.and(idx_0_3) || c.and(idx_4_7) || c.and(idx_8_11) || c.and(idx_12_15);
            case 3:
                // 8x2
                return (c.and(idx_0_3) && c.and(idx_4_7)) || (c.and(idx_8_11) && c.and(idx_12_15));
            default:
                throw new AssertionError();
        }
    }
    
    Checker c = new Checker();
    
    private boolean equals(AbstractPoint p1, AbstractPoint p2, int bucketIndex) {
        return p1._buckets[bucketIndex] == p2._buckets[bucketIndex];
    }
    
    private class Checker {
        private AbstractPoint p1;
        private AbstractPoint p2;
               
        public void set(AbstractPoint p1, AbstractPoint p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
        
        public boolean eq(int bucketIndex) {
            return p1._buckets[bucketIndex] == p2._buckets[bucketIndex];
        }
        
        public boolean and(int[] bucketIndices) {
            for(int index : bucketIndices)
                if(eq(index) == false)
                    return false;
            return true;
        }
    }
}
