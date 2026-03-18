package io.onedev.server.service.impl;

import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.in;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;

import io.onedev.server.ai.dispatch.AiDispatchAgent;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.AiDispatchRun;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.AiDispatchRunService;

@Singleton
public class DefaultAiDispatchRunService extends BaseEntityService<AiDispatchRun> implements AiDispatchRunService {

	@Transactional
	@Override
	public void create(AiDispatchRun run) {
		Preconditions.checkState(run.isNew());
		dao.persist(run);
	}

	@Transactional
	@Override
	public void update(AiDispatchRun run) {
		Preconditions.checkState(!run.isNew());
		dao.persist(run);
	}

	@Sessional
	@Override
	public List<AiDispatchRun> query(PullRequest request) {
		var criteria = EntityCriteria.of(AiDispatchRun.class);
		criteria.add(eq(AiDispatchRun.PROP_REQUEST, request));
		criteria.addOrder(Order.desc(AbstractEntity.PROP_ID));
		return query(criteria);
	}

	@Sessional
	@Override
	public List<AiDispatchRun> query(PullRequestComment comment) {
		var criteria = EntityCriteria.of(AiDispatchRun.class);
		criteria.add(eq(AiDispatchRun.PROP_COMMENT, comment));
		criteria.addOrder(Order.desc(AbstractEntity.PROP_ID));
		return query(criteria);
	}

	@Sessional
	@Override
	public List<AiDispatchRun> queryRecent() {
		return queryRecent(null, null);
	}

	@Sessional
	@Override
	public List<AiDispatchRun> queryRecent(@Nullable AiDispatchAgent agent,
										   @Nullable Collection<AiDispatchRun.State> states) {
		var criteria = EntityCriteria.of(AiDispatchRun.class);
		if (agent != null)
			criteria.add(eq(AiDispatchRun.PROP_AGENT, agent));
		if (states != null && !states.isEmpty())
			criteria.add(in(AiDispatchRun.PROP_STATE, states));
		criteria.addOrder(Order.desc(AbstractEntity.PROP_ID));
		return query(criteria);
	}

	@Sessional
	@Override
	public List<AiDispatchRun> queryUnfinished() {
		var criteria = EntityCriteria.of(AiDispatchRun.class);
		criteria.add(in(AiDispatchRun.PROP_STATE, List.of(AiDispatchRun.State.QUEUED, AiDispatchRun.State.RUNNING)));
		criteria.addOrder(Order.desc(AbstractEntity.PROP_ID));
		return query(criteria);
	}

	@Sessional
	@Override
	public int countActive() {
		return queryRecent(null, List.of(AiDispatchRun.State.QUEUED, AiDispatchRun.State.RUNNING)).size();
	}

	@Sessional
	@Override
	public Map<AiDispatchRun.State, Long> countByState(@Nullable AiDispatchAgent agent) {
		var counts = new EnumMap<AiDispatchRun.State, Long>(AiDispatchRun.State.class);
		for (var state: AiDispatchRun.State.values())
			counts.put(state, 0L);
		for (var run: queryRecent(agent, null))
			counts.compute(run.getState(), (key, value) -> value + 1);
		return counts;
	}

	@Sessional
	@Override
	public AiDispatchRun findLast(PullRequestComment comment) {
		var runs = query(comment);
		return runs.isEmpty()? null: runs.get(0);
	}

}
