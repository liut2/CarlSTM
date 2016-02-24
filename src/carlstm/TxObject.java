package carlstm;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;
/**
 * A TxObject is a special kind of object that can be read and written as part
 * of a transaction.
 * 
 * @param <T> type of the value stored in this TxObject
 */


public final class TxObject<T> {
	T value;
	final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	final Lock r = readWriteLock.readLock();
	final Lock w = readWriteLock.writeLock();
	public TxObject(T value) {
		this.value = value;
	}
	/* when we call the read() method, first we try to acquire the read lock; if we fail to acquire the read lock, then we
	call abort(). And if it's our first time to call read(), we read from the TxObject.
	* */
	public T read() throws NoActiveTransactionException,
			TransactionAbortedException {
		TxInfo txInfo = ThreadLocalExample.get();
		if (r.tryLock()) {
			if (txInfo.hashtable.get(this) == null) {
				Pair pair = new Pair((Object) this.value, (Object) this.value);
				txInfo.hashtable.put(this, pair);
				r.unlock();
				return value;
			}
			r.unlock();
			return (T) txInfo.hashtable.get(this).curValue;
		} else {
			txInfo.abort();
			return null;
		}
	}

	public void write(T value) throws NoActiveTransactionException,
			TransactionAbortedException {
		TxInfo txInfo = ThreadLocalExample.get();
		txInfo.hashtable.get(this).curValue = value;
	}
}
