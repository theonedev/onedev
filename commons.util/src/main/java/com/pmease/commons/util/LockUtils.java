package com.pmease.commons.util;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;

@SuppressWarnings("unchecked")
public class LockUtils {
	
    private final static Map<String, Lock> locks = new ReferenceMap(
    		AbstractReferenceMap.HARD, AbstractReferenceMap.WEAK);
    
    /**
     * Lock specified name.
     * 
     * @param name
     * 			name to be locked
     * @return
     * 			acquired lock against specified name
     */
    public static Lock lock(String name) {
    	return lock(name, false);
    }
    
    /**
     * Lock with specified name. 
     * 
     * @param name
     * 			name to be locked
     * @param tryMode
     * 			check if the name can be locked if this value is <tt>true</tt>
     * @return
     * 			acquired lock against specified name, or <tt>null</tt> if specified 
     * 			name can not be locked and <tt>tryMode</tt> is <tt>true</tt>
     */
    public static @Nullable Lock lock(String name, boolean tryMode) {
    	Lock lock;
    	synchronized (locks) {
	        lock = locks.get(name);
	        if (lock == null) {
	        	lock = new ReentrantLock();
	        	locks.put(name, lock);
	        } 
    	}
    	if (tryMode) {
    		if (lock.tryLock())
    			return lock;
    		else
    			return null;
    	} else {
			try {
				lock.lockInterruptibly();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
	    	return lock;
    	}
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
    	Lock lock = lock(name);
    	try {
    		return callable.call();
    	} catch (Exception e) {
    		throw new RuntimeException(e);
		} finally {
    		lock.unlock();
    	}
    }
    
}