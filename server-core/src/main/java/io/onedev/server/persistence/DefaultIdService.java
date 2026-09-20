package io.onedev.server.persistence;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.SharedSessionContract;
import org.hibernate.Transaction;

import io.onedev.server.cluster.ClusterAtomicLong;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.data.DataService;
import io.onedev.server.model.EntityIdCounter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DefaultIdService implements IdService {

	private final DataService dataService;

	private final ClusterService clusterService;

	private final SessionFactoryService sessionFactoryService;

	private final Map<Class<?>, ClusterAtomicLong> nextIds = new HashMap<>();

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

	private void recordId(SharedSessionContract session, Class<?> entityClass, long id) {
		TransactionCounters.record(session, dataService.getTableName(EntityIdCounter.class),
				dataService.getColumnName(EntityIdCounter.PROP_ENTITY_NAME), entityClass.getName(),
				dataService.getColumnName(EntityIdCounter.PROP_MAX_ID), id);
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
		TransactionCounters.clear(transaction);
	}

}
