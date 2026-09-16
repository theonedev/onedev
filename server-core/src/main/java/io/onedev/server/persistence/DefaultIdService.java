package io.onedev.server.persistence;

import io.onedev.server.cluster.ClusterAtomicLong;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.data.DataService;
import io.onedev.server.model.AbstractEntity;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

@Singleton
public class DefaultIdService implements IdService {

	private final DataService dataService;
	
	private final ClusterService clusterService;
	
	private final SessionFactoryService sessionFactoryService;
	
	private final Map<Class<?>, ClusterAtomicLong> nextIds = new HashMap<>();
	
	@Inject
	public DefaultIdService(DataService dataService, ClusterService clusterService,
                            SessionFactoryService sessionFactoryService) {
		this.dataService = dataService;
		this.sessionFactoryService = sessionFactoryService;
		this.clusterService = clusterService;
	}

	@SuppressWarnings("unchecked")
	private long getMaxId(Connection conn, Class<?> entityClass) {
		try (Statement stmt = conn.createStatement()) {
			String query = String.format("select max(%s) from %s", 
					dataService.getColumnName(AbstractEntity.PROP_ID), 
					dataService.getTableName((Class<? extends AbstractEntity>) entityClass));
			try (ResultSet resultset = stmt.executeQuery(query)) {
				if (resultset.next()) 
					return Math.max(resultset.getLong(1), 0);
				else 
					return 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void init() {
		try (var conn = dataService.openConnection()) {
			callWithTransaction(conn, () -> {
				for (var persistenceClass: sessionFactoryService.getMetadata().getEntityBindings()) {
					Class<?> entityClass = persistenceClass.getMappedClass();
					var nextId = clusterService.getAtomicLong(entityClass.getName());
					clusterService.initWithLead(nextId, () -> getMaxId(conn, entityClass) + 1);
					nextIds.put(entityClass, nextId);
				}
				return null;
			});
		} catch (SQLException e) {
			throw new RuntimeException(e);
		};
	}

	@Override
	public long nextId(Class<?> entityClass) {
		return nextIds.get(entityClass).getAndIncrement();
	}

	@Override
	public void useId(Class<?> entityClass, long id) {
        nextIds.get(entityClass).ensureAtLeast(Math.addExact(id, 1));
	}

}
