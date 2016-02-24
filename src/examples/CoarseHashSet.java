package examples;
import java.util.*;

/**
 *
 */
public class CoarseHashSet<T> implements Set<T> {

    /**
     * Helper class - basically is a linked list of items that happen to map to
     * the same hash code.
     */
    private static class Bucket {
        /**
         * The item stored at this entry. This is morally of type T, but Java
         * generics do not play well with arrays, so we have to use Object
         * instead.
         */
        Object item;

        /**
         * Next item in the list.
         */
        Bucket next;

        /**
         * Create a new bucket.
         *
         * @param item item to be stored
         * @param next next item in the list
         */
        public Bucket(Object item, Bucket next) {
            this.item = item;
            this.next = next;
        }
    }

    /**
     * Our array of items. Each location in the array stores a linked list items
     * that hash to that locations.
     */
    private Bucket[] table;

    /**
     * Capacity of the array. Since we do not support resizing, this is a
     * constant.
     */
    private static final int CAPACITY = 1024;

    /**
     * Create a new HashSet.
     */
    public CoarseHashSet() {
        this.table = new Bucket[CAPACITY];
    }

    /**
     * A helper method to see if an item is stored at a given bucket.
     *
     * @param bucket bucket to be searched
     * @param item item to be searched for
     * @return true if the item is in the bucket
     */
    private boolean contains(Bucket bucket, T item) {
        while (bucket != null) {
            if (item.equals(bucket.item)) {
                return true;
            }
            bucket = bucket.next;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see examples.Set#add(java.lang.Object)
     */
    @Override
    public synchronized boolean add(T item) {
        // Java returns a negative number for the hash; this is just converting
        // the negative number to a location in the array.
        int hash = (item.hashCode() % CAPACITY + CAPACITY) % CAPACITY;
        Bucket bucket = table[hash];
        if (contains(bucket, item)) {
            return false;
        }
        table[hash] = new Bucket(item, bucket);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see examples.Set#contains(java.lang.Object)
     */
    @Override
    public synchronized boolean contains(T item) {
        int hash = (item.hashCode() % CAPACITY + CAPACITY) % CAPACITY;
        Bucket bucket = table[hash];
        return contains(bucket, item);
    }
    static class MyThread extends Thread {
        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
        CoarseHashSet<Integer> hashSet;

        public MyThread(CoarseHashSet<Integer> hashSet){
            this.hashSet = hashSet;
        }
        @Override
        public void run() {
            for (int i = 0; i < 100; i++){
                Integer newInt = i;
                //System.out.println(Thread.currentThread().getName() + " add " +hashSet.add((newInt)) + " " + newInt);
                boolean result = hashSet.add((newInt));
            }
            for (int i = 0; i < 100; i++){
                Integer newInt = i;
                //System.out.println(Thread.currentThread().getName()+ " contains "+hashSet.contains((newInt)) + " " + newInt);
                boolean result2 = hashSet.contains((newInt));
            }
        }
    }
    public static void main(String[] args)throws InterruptedException{
        MyThread[] array = new MyThread[1000];
        CoarseHashSet<Integer> hashSet = new CoarseHashSet<Integer>();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < array.length; i++){
            array[i] = new MyThread(hashSet);
            array[i].start();
        }
        for (int i = 0; i < array.length; i++){
            array[i].join();
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
    }
}



