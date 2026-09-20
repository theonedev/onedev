package io.onedev.server.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.SharedSessionContract;
import org.hibernate.Transaction;

import jakarta.transaction.Synchronization;

/** Persists all counter kinds in a single lock order, after entity flush and before commit. */
public class TransactionCounters {

	private record Key(String table, String keyColumn, Object key, String valueColumn) implements Comparable<Key> {
		@Override
		public int compareTo(Key other) {
			int order = table.compareTo(other.table);
			if (order == 0)
				order = keyColumn.compareTo(other.keyColumn);
			if (order == 0) {
				if (key instanceof Long && other.key instanceof Long)
					order = Long.compare((Long) key, (Long) other.key);
				else
					order = key.toString().compareTo(other.key.toString());
			}
			if (order == 0)
				order = valueColumn.compareTo(other.valueColumn);
			return order;
		}
	}

	private static final Map<Transaction, Map<Key, Long>> pendingTransactions = new ConcurrentHashMap<>();

	public static void record(SharedSessionContract session, String table, String keyColumn, Object key,
			String valueColumn, long value) {
		var transaction = session.getTransaction();
		if (!transaction.isActive())
			throw new IllegalStateException("Counters must be persisted in an active transaction");
		var pending = pendingTransactions.computeIfAbsent(transaction, tx -> {
			var updates = new TreeMap<Key, Long>();
			tx.registerSynchronization(new Synchronization() {
				@Override
				public void beforeCompletion() {
					// Use the entity transaction's connection: any failure rolls back both.
					session.doWork(conn -> update(conn, updates));
				}

				@Override
				public void afterCompletion(int status) {
					clear(tx);
				}
			});
			return updates;
		});
		pending.merge(new Key(table, keyColumn, key, valueColumn), value, Math::max);
	}

	private static void update(Connection conn, Map<Key, Long> updates) throws SQLException {
		for (var entry: updates.entrySet()) {
			var key = entry.getKey();
			try (var stmt = conn.prepareStatement(String.format("update %s set %s=? where %s=? and %s<?",
					key.table, key.valueColumn, key.keyColumn, key.valueColumn))) {
				stmt.setLong(1, entry.getValue());
				if (key.key instanceof Long)
					stmt.setLong(2, (Long) key.key);
				else
					stmt.setString(2, key.key.toString());
				stmt.setLong(3, entry.getValue());
				stmt.executeUpdate();
			}
		}
	}

	public static void clear(Transaction transaction) {
		pendingTransactions.remove(transaction);
	}

}
