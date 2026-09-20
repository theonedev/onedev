package io.onedev.server.persistence;

import com.hazelcast.map.IMap;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.data.DataService;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectNumberCounter;
import io.onedev.server.persistence.dao.Dao;

import java.util.Map;

public class SequenceGenerator {

	private final Class<? extends AbstractEntity> sequenceClass;

	private final String nextNumberProperty;

	private final ClusterService clusterService;

	private final Dao dao;

	private final DataService dataService;

	private IMap<Long, Long> nextSequences;

	public SequenceGenerator(Class<? extends AbstractEntity> sequenceClass, String nextNumberProperty,
			ClusterService clusterService, Dao dao, DataService dataService) {
		this.sequenceClass = sequenceClass;
		this.nextNumberProperty = nextNumberProperty;
		this.dao = dao;
		this.dataService = dataService;
		this.clusterService = clusterService;
	}

	private synchronized Map<Long, Long> getNextSequences() {
		if (nextSequences == null)
			nextSequences = clusterService.getHazelcastInstance().getMap("nextNumbers:" + sequenceClass.getName());
		return nextSequences;
	}

	public void removeNextSequence(Project sequenceScope) {
		getNextSequences().remove(sequenceScope.getId());
	}

	private void checkTransaction() {
		if (!dao.getSession().getTransaction().isActive())
			throw new IllegalStateException("Project numbers must be persisted in an active transaction");
	}

	private long loadNextSequence(Project sequenceScope) {
		// Query the column directly, bypassing potentially stale managed counters.
		return dao.getSession().createQuery("select " + nextNumberProperty + " from ProjectNumberCounter where project.id=:id", Long.class)
				.setParameter("id", sequenceScope.getId()).getSingleResult();
	}

	private void saveNextSequence(Project sequenceScope, long nextSequence) {
		TransactionCounters.record(dao.getSession(), dataService.getTableName(ProjectNumberCounter.class),
				dataService.getColumnName("project_id"), sequenceScope.getId(),
				dataService.getColumnName(nextNumberProperty), nextSequence);
	}

	public Long getNextSequence(Project sequenceScope) {
		checkTransaction();
		Long key = sequenceScope.getId();
		var sequences = getNextSequences();
		while (true) {
			Long nextSequence = sequences.get(key);
			if (nextSequence == null) {
				sequences.putIfAbsent(key, loadNextSequence(sequenceScope));
			} else {
				long followingSequence = Math.addExact(nextSequence, 1);
				if (sequences.replace(key, nextSequence, followingSequence)) {
					saveNextSequence(sequenceScope, followingSequence);
					return nextSequence;
				}
			}
		}
	}

	public void resetNextSequence(Project sequenceScope) {
		checkTransaction();
		// Importers can preserve supplied numbers. Advance past those numbers without
		// forgetting numbers already allocated for entities that have been deleted.
		var maxNumber = dao.getSession().createQuery("select max(number) from " + sequenceClass.getSimpleName()
				+ " where numberScope=:numberScope", Long.class)
				.setParameter("numberScope", sequenceScope).uniqueResult();
		long minimum = Math.max(loadNextSequence(sequenceScope), maxNumber != null ? Math.addExact(maxNumber, 1) : 1);
		var sequences = getNextSequences();
		Long key = sequenceScope.getId();
		while (true) {
			Long nextSequence = sequences.get(key);
			if (nextSequence == null) {
				if (sequences.putIfAbsent(key, minimum) == null)
					break;
			} else {
				minimum = Math.max(minimum, nextSequence);
				if (sequences.replace(key, nextSequence, minimum))
					break;
			}
		}
		saveNextSequence(sequenceScope, minimum);
	}

}
