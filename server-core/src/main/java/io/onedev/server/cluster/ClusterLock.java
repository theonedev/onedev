package io.onedev.server.cluster;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

/**
 * A named cluster wide lock backed by IMap key locking. It is reentrant and owned by the
 * locking thread. Like other IMap data, this has AP semantics, not the CP subsystem's
 * consensus guarantees.
 */
public final class ClusterLock implements Lock {

    private static final String MAP_NAME = "clusterLocks";

    private final IMap<String, Object> locks;
    private final String name;

    public ClusterLock(HazelcastInstance hazelcastInstance, String name) {
        this.locks = hazelcastInstance.getMap(MAP_NAME);
        this.name = name;
    }

    @Override
    public void lock() {
        locks.lock(name);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        // IMap has no interruptible blocking lock, so wait in bounded attempts
        boolean locked = false;
        while (!locked)
            locked = locks.tryLock(name, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean tryLock() {
        return locks.tryLock(name);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return locks.tryLock(name, time, unit);
    }

    @Override
    public void unlock() {
        locks.unlock(name);
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

}
