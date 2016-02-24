package carlstm;
import java.util.*;


/**
 * This class holds transactional state for a single thread. You should use
 * {@link java.lang.ThreadLocal} to allocate a TxInfo to each Java thread. This
 * class is only used within the STM implementation, so it and its members are
 * set to package (default) visibility.
 */

class Pair{
	Object oldValue;
	Object curValue;
	public Pair(Object oldValue, Object curValue){
		this.oldValue = oldValue;
		this.curValue = curValue;
	}
}
class TxInfo {
	boolean activeTransaction;
	Hashtable<TxObject, Pair> hashtable;
	/**
	 * Start a transaction by initializing any necessary state. This method
	 * should throw {@link TransactionAlreadyActiveException} if a transaction
	 * is already being executed.
	 */
	void start(){
		// TODO implement me
		if (!activeTransaction){
			activeTransaction = true;
			hashtable = new Hashtable<TxObject, Pair>();
		}else{
			throw new TransactionAlreadyActiveException();
		}
	}

	/**
	 * Try to commit a completed transaction. This method should update any
	 * written TxObjects, acquiring locks on those objects as needed.
	 * 
	 * @return true if the commit succeeds, false if the transaction aborted
	 */
	boolean commit() throws TransactionAbortedException{
		// TODO implement me
		for (TxObject txObject : hashtable.keySet()){
			Pair pair = hashtable.get(txObject);
			if(txObject.r.tryLock()) {
				if (pair.oldValue.equals(txObject.value)) {
					txObject.r.unlock();
					if(txObject.w.tryLock()){
						txObject.value = pair.curValue;
						txObject.w.unlock();
					} else {
						throw new TransactionAbortedException();
					}
				} else {
					txObject.r.unlock();
					return false;
				}
			} else {
				throw new TransactionAbortedException();
			}
		}
		return true;
	}

	/**
	 * This method cleans up any transactional state if a transaction aborts.
	 */
	void abort() {
		activeTransaction = false;
		hashtable = null;
	}
}
