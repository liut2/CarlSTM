# CarlSTM
## Brief Introduction
Software Transactional Memory (STM) is a mechanism for concurrency control in shared-memory programs, the idea for which originated in the database community. The basic concept is that critical sections of code in your program can be labeled as atomic blocks, without the actual use of lock like "synchronized" keyword in java in your program.

In this project, we used a technique called "Lazy buffering" that won't carry out the real write/update operation until a transaction is committed. In the case when the transaction is aborted, we undo all the changes that'are made by the transaction and retry the transaction until it succeeds in commit.

## Use Case

Here we performed a comparison among different implementation of a HashSet:
* coarse grained, which just put a lock on the whole HashSet whenever there is a read/write operation
* fine grained, which only locks a specific slot in the HashSet based on its hash code, thus reduces the block when multiple threads are trying to update the HashSet
* STM based, which regards each read/write operation as a transaction in STM. Note here in order to optimize the performance of the HashSet, we used read/write lock to differentiate different operations. 
