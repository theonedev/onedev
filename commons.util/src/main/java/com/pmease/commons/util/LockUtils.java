package com.pmease.commons.util;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;

@SuppressWarnings("unchecked")
public class LockUtils {
	
    private final static Map<String, Lock> locks = 
    		new ReferenceMap(AbstractReferenceMap.HARD, AbstractReferenceMap.WEAK);
    
    private final static Map<String, ReadWriteLock> rwLocks = 
    		new ReferenceMap(AbstractReferenceMap.HARD, AbstractReferenceMap.WEAK);

    /**
     * Get named lock. 
     * 
     * @param name
     * 			name of the lock
     * @return
     * 			lock associated with specified name
     */
    public static Lock getLock(String name) {
    	Lock lock;
    	synchronized (locks) {
	        lock = locks.get(name);
	        if (lock == null) {
	        	lock = new ReentrantLock();
	        	locks.put(name, lock);
	        } 
    	}
    	return lock;
    }
    
    /**
     * Get named read write lock. 
     * 
     * @param name
     * 			name of the read write lock
     * @return
     * 			read write lock associated with specified name
     */
    public static ReadWriteLock getReadWriteLock(String name) {
    	ReadWriteLock lock;
    	synchronized (rwLocks) {
	        lock = rwLocks.get(name);
	        if (lock == null) {
	        	lock = new ReentrantReadWriteLock();
	        	rwLocks.put(name, lock);
	        } 
    	}
    	return lock;
    }

    /**
     * Execute specified callable in lock of specified name.
     * 
     * @param name
     * 			name of the lock to be acquired
     * @param callable
     * 			callable to be execute within the named lock
     * @return
     * 			return value of the callable
     */
    public static <T> T call(String name, Callable<T> callable) {
    	Lock lock = getLock(name);
    	try {
        	lock.lockInterruptibly();
    		return callable.call();
    	} catch (Exception e) {
    		throw new RuntimeException(e);
		} finally {
    		lock.unlock();
    	}
    }
    
    /**
     * Execute specified callable in read lock of specified name.
     * 
     * @param name
     * 			name of the lock to be acquired
     * @param callable
     * 			callable to be execute within the named read lock
     * @return
     * 			return value of the callable
     */
    public static <T> T read(String name, Callable<T> callable) {
    	Lock lock = getReadWriteLock(name).readLock();
    	try {
        	lock.lockInterruptibly();
    		return callable.call();
    	} catch (Exception e) {
    		throw new RuntimeException(e);
		} finally {
    		lock.unlock();
    	}
    }

    /**
     * Execute specified callable in write lock of specified name.
     * 
     * @param name
     * 			name of the lock to be acquired
     * @param callable
     * 			callable to be execute within the named write lock
     * @return
     * 			return value of the callable
     */
    public static <T> T write(String name, Callable<T> callable) {
    	Lock lock = getReadWriteLock(name).writeLock();
    	try {
        	lock.lockInterruptibly();
    		return callable.call();
    	} catch (Exception e) {
    		throw new RuntimeException(e);
		} finally {
    		lock.unlock();
    	}
    }

}