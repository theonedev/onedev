package io.onedev.server.cluster;

import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.IMap;

/**
 * A named counter backed by atomic partition operations and synchronous IMap backups.
 * Like other IMap data, this has AP semantics, not the CP subsystem's consensus guarantees.
 */
public final class ClusterAtomicLong {
    private static final String MAP_NAME = "clusterAtomicLongs";

    private final IMap<String, Long> values;
    private final String name;

    public ClusterAtomicLong(HazelcastInstance hazelcastInstance, String name) {
        this.values = hazelcastInstance.getMap(MAP_NAME);
        this.name = name;
    }

    public long get() {
        Long value = values.get(name);
        return value != null ? value : 0;
    }

    public void set(long value) {
        values.set(name, value);
    }

    public long getAndIncrement() {
        return values.executeOnKey(name, new Update(Operation.INCREMENT, 0, 0));
    }

    public boolean compareAndSet(long expected, long value) {
        return values.executeOnKey(name, new Update(Operation.COMPARE_AND_SET, expected, value)) == expected;
    }

    public void ensureAtLeast(long value) {
        values.executeOnKey(name, new Update(Operation.MAX, 0, value));
    }

    private enum Operation { INCREMENT, COMPARE_AND_SET, MAX }

    private static final class Update implements EntryProcessor<String, Long, Long> {
        private static final long serialVersionUID = 1L;
        private final Operation operation;
        private final long expected;
        private final long value;
        private Long updated;

        private Update(Operation operation, long expected, long value) {
            this.operation = operation;
            this.expected = expected;
            this.value = value;
        }

        @Override
        public Long process(Map.Entry<String, Long> entry) {
            long current = entry.getValue() != null ? entry.getValue() : 0;
            updated = switch (operation) {
                case INCREMENT -> Math.addExact(current, 1);
                case COMPARE_AND_SET -> current == expected ? value : null;
                case MAX -> Math.max(current, value);
            };
            if (updated != null)
                entry.setValue(updated);
            return current;
        }

        @Override
        public EntryProcessor<String, Long, Long> getBackupProcessor() {
            // Apply the primary's result, even when a backup has no previous entry.
            return updated != null ? new SetBackupValue(updated) : null;
        }
    }

    private static final class SetBackupValue implements EntryProcessor<String, Long, Long> {
        private static final long serialVersionUID = 1L;
        private final long value;

        private SetBackupValue(long value) {
            this.value = value;
        }

        @Override
        public Long process(Map.Entry<String, Long> entry) {
            entry.setValue(value);
            return null;
        }

        @Override
        public EntryProcessor<String, Long, Long> getBackupProcessor() {
            return null;
        }
    }
}
