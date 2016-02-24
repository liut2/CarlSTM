package examples;

import carlstm.CarlSTM;
import carlstm.NoActiveTransactionException;
import carlstm.Transaction;
import carlstm.TransactionAbortedException;
import carlstm.TxObject;

import java.util.*;

/**
 *
 */
public class AtomicHashSet<T> implements Set<T> {
    static AtomicHashSet<Integer> hashSet = new AtomicHashSet<Integer>();

    /**
     * Helper class - basically is a linked list of items that happen to map to
     * the same hash code.
     */
    private static class Bucket<T> {
        /**
         * The item stored at this entry. This is morally of type T, but Java
         * generics do not play well with arrays, so we have to use Object
         * instead.
         */
        //here we changed the item to the TxObject which will be responsible for storing the actual value of the hashset.
        TxObject<T> item;

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
        public Bucket(TxObject<T> item, Bucket next) {
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

    @SuppressWarnings("unchecked")
    /**
     * Create a new HashSet.
     */
    public AtomicHashSet() {
        this.table = new Bucket[CAPACITY];

    }

    /**
     * A helper method to see if an item is stored at a given bucket.
     *
     * @param bucket bucket to be searched
     * @param item   item to be searched for
     * @return true if the item is in the bucket
     */
    private boolean contains(Bucket bucket, T item)throws NoActiveTransactionException,
            TransactionAbortedException {
        //we used a temp Bucket to walk through the list so as not to disturb the original head of linkedlist
        Bucket temp = bucket;
        while (temp != null) {
            if (item.equals(temp.item.read())) {
                return true;
            }
            temp = temp.next;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see examples.Set#add(java.lang.Object)
     */
    @Override
    public boolean add(T item) {
        // Java returns a negative number for the hash; this is just converting
        // the negative number to a location in the array.
        //since it's not possible to refer to the item inside the transaction, we used a final type reference to help solve the problem.
        final T itemRef = item;
        Boolean result = CarlSTM.execute(new Transaction<Boolean>() {
            public Boolean run() throws NoActiveTransactionException,
                    TransactionAbortedException {
                int hash = (itemRef.hashCode() % CAPACITY + CAPACITY) % CAPACITY;
                TxObject<T> newTxObject = new TxObject<T>(itemRef);
                //if the head of the linkedlist is null itself, we will just add the item to it and don't bother to verify if it's in the hashset.
                if (table[hash] == null){
                    table[hash] = new Bucket(newTxObject, null);
                    return true;
                }
                Bucket bucket = table[hash];
                //if the hashset contains the item, then we can just return false, since we don't need to add item anyway.
                if (contains(bucket, itemRef)) {
                    return false;
                }
                //if the hashset does not contains the item, it will walk through the linkedlist with the temp bucket and insert it at the end of the list.
                Bucket temp = bucket;
                while(temp.next != null) {
                    temp = temp.next;
                }
                temp.next = new Bucket(newTxObject, null);
                return true;
            }
        });
        return result;
    }

    /*
     * (non-Javadoc)
     * @see examples.Set#contains(java.lang.Object)
     */
    @Override
    //we rewrite our contains method of the transactional hashset version. Here we replace the lock and synchronized code with
    //the transaction we implemented previously. A transanction can be viewed as an atomic block.
    public boolean contains(T item) {
        final T itemRef = item;
        Boolean result = CarlSTM.execute(new Transaction<Boolean>() {
            public Boolean run() throws NoActiveTransactionException,
                    TransactionAbortedException {
                int hash = (itemRef.hashCode() % CAPACITY + CAPACITY) % CAPACITY;
                Bucket bucket = table[hash];
                return contains(bucket, itemRef);
            }
        });
        return result;
    }



    /**
     * A Java Thread that executes a transaction and prints its result.
     *
     */
    static class MyThread extends Thread {
        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
        AtomicHashSet<Integer> hashSet;

        public MyThread(AtomicHashSet<Integer> hashSet){
            this.hashSet = hashSet;
        }
        //Here we defined our thread's task to be adding 100 numbers from 0 to 100 and verify if they are in the hashset afterwards.
        @Override
        public void run() {
           for (int i = 0; i < 100; i++){
               Integer newInt = i;
               //System.out.println(Thread.currentThread().getName() + " add " +hashSet.add((newInt)) + " " + newInt);
               boolean result = hashSet.add((newInt));
           }
            for (int i = 0; i < 100; i++) {
                Integer newInt = i;
                //System.out.println(Thread.currentThread().getName()+ " contains "+hashSet.contains((newInt)) + " " + newInt);
                boolean result2 = hashSet.contains((newInt));
            }
        }
    }

    public static void main(String[] args) throws InterruptedException{
        //here we used 1000 threads for testing purpose.
        MyThread[] array = new MyThread[1000];
        AtomicHashSet<Integer> hashSet = new AtomicHashSet<Integer>();
        //we calculate the run time of these 1000 threads by counting the start and end time.
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




