package carlstm;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * This class coordinates transaction execution. You can execute a transaction
 * using {@link #execute}. For example:
 * 
 * <pre>
 * class MyTransaction implements Transaction&lt;Integer&gt; {
 * 	TxObject&lt;Integer&gt; x;
 * 
 * 	MyTransaction(TxObject&lt;Integer&gt; x) {
 * 		this.x = x;
 * 	}
 * 
 * 	public Integer run() throws NoActiveTransactionException,
 * 			TransactionAbortedException {
 * 		int value = x.read();
 * 		x.write(value + 1);
 * 		return value;
 * 	}
 * 
 * 	public static void main(String[] args) {
 * 		TxObject&lt;Integer&gt; x = new TxObject&lt;Integer&gt;(0);
 * 		int result = CarlSTM.execute(new MyTransaction(x));
 * 		System.out.println(result);
 * 	}
 * }
 * </pre>
 */
class Double{
	int commitCounter;
	int abortCounter;
	public Double(int commitCounter, int abortCounter){
		this.commitCounter = commitCounter;
		this.abortCounter = abortCounter;
	}
}
class ThreadLocalExample{
	private static final ThreadLocal<TxInfo> myThreadLocal = new ThreadLocal<TxInfo>();
	public static void set(TxInfo txInfo){
		myThreadLocal.set(txInfo);
	}
	public static TxInfo get(){
		return myThreadLocal.get();
	}
}
class ThreadLocalCounter {
	private static final ThreadLocal<Double> myThreadLocal = new ThreadLocal<Double>();
	public static void set(Double pair){
		myThreadLocal.set(pair);
	}
	public static Double get(){
		return myThreadLocal.get();
	}
}
public class CarlSTM {


	/**
	 * Execute a transaction and return its result. This method needs to
	 * repeatedly start, execute, and commit the transaction until it
	 * successfully commits.
	 * 
	 * @param <T> return type of the transaction
	 * @param tx transaction to be executed
	 * @return result of the transaction
	 */

	public static <T> T execute(Transaction<T> tx) {
		// TODO implement me
		TxInfo txInfo = new TxInfo();
		Double dou = new Double(0, 0);
		ThreadLocalExample.set(txInfo);
		ThreadLocalCounter.set(dou);
		boolean successful = false;
		int sleepTime = 1;
		T runResult = null;
		while (!successful){
			try {
				txInfo.start();
				runResult = tx.run();
				boolean tempResult = txInfo.commit();
				if (tempResult){
					successful = true;
					ThreadLocalCounter.get().commitCounter++;
				}else {
					System.out.println("Aborted after check!!!!!!!!!!!!!!!!!!!!!!!!!");
					//txInfo.abort();
					throw new TransactionAbortedException();

				}
			} catch (NoActiveTransactionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (TransactionAbortedException e) {
				// TODO Auto-generated catch block
				try {
					Thread.sleep(sleepTime);
					sleepTime *= 100;
				} catch(InterruptedException e1) {
					e1.printStackTrace();
				} finally {
					e.printStackTrace();
					txInfo.abort();
					ThreadLocalCounter.get().abortCounter++;
				}
			}
			//System.out.println(Thread.currentThread().getName() + " commit:" + ThreadLocalCounter.get().commitCounter + " abort:" + ThreadLocalCounter.get().abortCounter);
		}

		return runResult;
	}
}
