/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kmeanslsh;

/**
 *
 * @author binderchri
 */
public class Hashing {
    
    public boolean isSameBucket(AbstractPoint p1, AbstractPoint p2) {
        /*
        // this is a hard coded AND
        for(int i = 0; i < p1._buckets.length; i++) {
            if(p1._buckets[i] != p2._buckets[i])
                return false;
        }
        
        return true;*/
        
        // this is a hard coded OR
/*        for(int i = 0; i < p1._buckets.length; i++) {
            if(p1._buckets[i] == p2._buckets[i])
                return true;
        }
        
        return false;
  */      
        check c = new check(p1, p2);

        /*return (c.eq(0) || c.eq(1) || c.eq(2) || c.eq(3))
                &&
               (c.eq(4) || c.eq(5) || c.eq(6) || c.eq(7));

        */
        return c.eq(0) && c.eq(1) && c.eq(2) && c.eq(3);
        
        /*return (equals(p1, p2, 0) || equals(p1, p2, 1) || equals(p1, p2, 2) || equals(p1, p2, 3))
                &&
               (equals(p1, p2, 4) || equals(p1, p2, 5) || equals(p1, p2, 6) || equals(p1, p2, 7));
*/
    }
    
    private boolean equals(AbstractPoint p1, AbstractPoint p2, int bucketIndex) {
        return p1._buckets[bucketIndex] == p2._buckets[bucketIndex];
    }
    
    private class check {
        private final AbstractPoint p1;
        private final AbstractPoint p2;
        
        public check(AbstractPoint p1, AbstractPoint p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
        
        public boolean eq(int bucketIndex) {
            return p1._buckets[bucketIndex] == p2._buckets[bucketIndex];
        }
    }
    
    
}
