package io.onedev.server.persistence;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.SharedSessionContract;
import org.hibernate.Transaction;

import io.onedev.server.cluster.ClusterAtomicLong;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.data.DataService;
import io.onedev.server.model.EntityIdCounter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Synchronization;

@Singleton
public class DefaultIdService implements IdService {

	private final DataService dataService;

	private final ClusterService clusterService;

	private final SessionFactoryService sessionFactoryService;

	private final Map<Class<?>, ClusterAtomicLong> nextIds = new HashMap<>();

	private final Map<Transaction, Map<String, Long>> transactionIds = new ConcurrentHashMap<>();

	private volatile boolean initialized;

	@Inject
	public DefaultIdService(DataService dataService, ClusterService clusterService,
			SessionFactoryService sessionFactoryService) {
		this.dataService = dataService;
		this.sessionFactoryService = sessionFactoryService;
		this.clusterService = clusterService;
	}

	@Override
	public void init() {
		try (var conn = dataService.openConnection()) {
			var maxIds = callWithTransaction(conn, () -> {
				var saved = new HashMap<String, Long>();
				try (var stmt = conn.createStatement();
						var rows = stmt.executeQuery(String.format("select %s, %s from %s",
								dataService.getColumnName(EntityIdCounter.PROP_ENTITY_NAME),
								dataService.getColumnName(EntityIdCounter.PROP_MAX_ID),
								dataService.getTableName(EntityIdCounter.class)))) {
					while (rows.next())
						saved.put(rows.getString(1), rows.getLong(2));
				}
				return saved;
			});
			for (var binding: sessionFactoryService.getMetadata().getEntityBindings()) {
				Class<?> entityClass = binding.getMappedClass();
				var maxId = maxIds.get(entityClass.getName());
				if (maxId == null)
					throw new IllegalStateException("Missing entity ID counter: " + entityClass.getName());
				var nextId = clusterService.getAtomicLong(entityClass.getName());
				clusterService.initWithLead(nextId, () -> Math.addExact(maxId, 1));
				nextIds.put(entityClass, nextId);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		initialized = true;
	}

	private void updateCounters(Connection conn, Map<String, Long> maxIds) throws SQLException {
		var maxIdColumn = dataService.getColumnName(EntityIdCounter.PROP_MAX_ID);
		try (var stmt = conn.prepareStatement(String.format("update %s set %s=? where %s=? and %s<?",
				dataService.getTableName(EntityIdCounter.class), maxIdColumn,
				dataService.getColumnName(EntityIdCounter.PROP_ENTITY_NAME), maxIdColumn))) {
			for (var entry: maxIds.entrySet()) {
				stmt.setLong(1, entry.getValue());
				stmt.setString(2, entry.getKey());
				stmt.setLong(3, entry.getValue());
				stmt.executeUpdate();
			}
		}
	}

	private void recordId(SharedSessionContract session, Class<?> entityClass, long id) {
		var transaction = session.getTransaction();
		if (!transaction.isActive())
			throw new IllegalStateException("Entity IDs must be persisted in an active transaction");
		var maxIds = transactionIds.computeIfAbsent(transaction, tx -> {
			// All transactions acquire counter row locks in entity-name order, once per
			// entity type, after Hibernate flushes inserts and before the JDBC commit.
			var pending = new TreeMap<String, Long>();
			tx.registerSynchronization(new Synchronization() {
				@Override
				public void beforeCompletion() {
					// Use this transaction's connection. Failure must roll back its inserts too.
					session.doWork(conn -> updateCounters(conn, pending));
				}

				@Override
				public void afterCompletion(int status) {
					clearTransaction(tx);
				}
			});
			return pending;
		});
		maxIds.merge(entityClass.getName(), id, Math::max);
	}

	@Override
	public long nextId(SharedSessionContract session, Class<?> entityClass) {
		if (!initialized)
			throw new IllegalStateException("Entity ID service is not initialized");
		long id = nextIds.get(entityClass).getAndIncrement();
		recordId(session, entityClass, id);
		return id;
	}

	@Override
	public void useId(SharedSessionContract session, Class<?> entityClass, long id) {
		// Offline imports preserve supplied IDs and counter rows before this service
		// starts. Reserved negative IDs do not advance positive-ID counters.
		if (initialized && id > 0) {
			nextIds.get(entityClass).ensureAtLeast(Math.addExact(id, 1));
			recordId(session, entityClass, id);
		}
	}

	@Override
	public void clearTransaction(Transaction transaction) {
		transactionIds.remove(transaction);
	}

}
