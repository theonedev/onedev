package io.onedev.server.persistence;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.SharedSessionContract;
import org.hibernate.Transaction;

import io.onedev.server.cluster.ClusterAtomicLong;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.data.DataService;
import io.onedev.server.model.AbstractEntity;
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
			// Initialize new model counters before publishing the counter table's own
			// next ID. Only the leader writes, and all rows commit together.
			clusterService.initWithLead(clusterService.getAtomicLong(EntityIdCounter.class.getName()),
					() -> callWithTransaction(conn, () -> initializeMissingCounters(conn)));
			var maxIds = callWithTransaction(conn, () -> loadMaxIds(conn));
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

	private Map<String, Long> loadMaxIds(Connection conn) throws SQLException {
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
	}

	private long initializeMissingCounters(Connection conn) throws SQLException {
		var maxIds = loadMaxIds(conn);
		var savedCounterId = maxIds.get(EntityIdCounter.class.getName());
		if (savedCounterId == null)
			throw new IllegalStateException("Missing entity ID counter: " + EntityIdCounter.class.getName());
		long counterId = savedCounterId;
		var table = dataService.getTableName(EntityIdCounter.class);
		var idColumn = dataService.getColumnName(AbstractEntity.PROP_ID);
		var nameColumn = dataService.getColumnName(EntityIdCounter.PROP_ENTITY_NAME);
		var maxIdColumn = dataService.getColumnName(EntityIdCounter.PROP_MAX_ID);
		for (var binding: sessionFactoryService.getMetadata().getEntityBindings()) {
			var entityClass = binding.getMappedClass().asSubclass(AbstractEntity.class);
			if (!maxIds.containsKey(entityClass.getName())) {
				// A migration may have populated the new table. Never rescan existing
				// counters, whose high-water marks also account for deleted entities.
				long maxId;
				try (var stmt = conn.createStatement();
						var rows = stmt.executeQuery(String.format("select max(%s) from %s",
								idColumn, dataService.getTableName(entityClass)))) {
					rows.next();
					maxId = Math.max(0, rows.getLong(1));
				}
				try (var stmt = conn.prepareStatement(String.format("insert into %s (%s, %s, %s) values (?, ?, ?)",
						table, idColumn, nameColumn, maxIdColumn))) {
					counterId = Math.addExact(counterId, 1);
					stmt.setLong(1, counterId);
					stmt.setString(2, entityClass.getName());
					stmt.setLong(3, maxId);
					stmt.executeUpdate();
				}
			}
		}
		if (counterId != savedCounterId) {
			try (var stmt = conn.prepareStatement(String.format("update %s set %s=? where %s=?",
					table, maxIdColumn, nameColumn))) {
				stmt.setLong(1, counterId);
				stmt.setString(2, EntityIdCounter.class.getName());
				stmt.executeUpdate();
			}
		}
		return Math.addExact(counterId, 1);
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
