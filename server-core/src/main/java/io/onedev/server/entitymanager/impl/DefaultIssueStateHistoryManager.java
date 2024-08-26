package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueStateHistoryManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueStateHistory;
import io.onedev.server.model.support.TimeGroups;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.util.StatsGroup;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.model.AbstractEntity.PROP_ID;
import static io.onedev.server.model.IssueStateHistory.*;
import static java.util.Arrays.asList;

@Singleton
public class DefaultIssueStateHistoryManager extends BaseEntityManager<IssueStateHistory> implements IssueStateHistoryManager {
	
	private final IssueManager issueManager;
	
	@Inject
	public DefaultIssueStateHistoryManager(Dao dao, IssueManager issueManager) {
		super(dao);
		this.issueManager = issueManager;
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Issue && event.isNewEntity()) {
			var issue = (Issue) event.getEntity();
			dao.persist(create(issue, issue.getState()));
		} else if (event.getEntity() instanceof IssueChange) {
			var change = (IssueChange) event.getEntity();
			if (change.getData() instanceof IssueStateChangeData) {
				var changeData = (IssueStateChangeData) change.getData();
				if (!changeData.getOldState().equals(changeData.getNewState())) {
					var history = create(change.getIssue(), changeData.getNewState());
					updateLast(change.getIssue(), history.getDate());
					dao.persist(history);
				}
			} else if (change.getData() instanceof IssueBatchUpdateData) {
				var changeData = (IssueBatchUpdateData) change.getData();
				if (!changeData.getOldState().equals(changeData.getNewState())) {
					var history = create(change.getIssue(), changeData.getNewState());
					updateLast(change.getIssue(), history.getDate());
					dao.persist(history);
				}
			}
		}
	}
	
	private IssueStateHistory create(Issue issue, String state) {
		var history = new IssueStateHistory();
		history.setIssue(issue);
		history.setDate(new Date());
		history.setTimeGroups(TimeGroups.of(history.getDate()));
		history.setState(state);
		return history;
	}
	
	private void updateLast(Issue issue, Date date) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_ISSUE, issue));
		criteria.addOrder(Order.desc(PROP_ID));
		var lastHistory = find(criteria);
		if (lastHistory != null)
			lastHistory.setDuration(date.getTime() - lastHistory.getDate().getTime());
	}

	@Sessional
	@Override
	public Map<Integer, Map<String, Integer>> queryDurationStats(ProjectScope projectScope, 
																 @Nullable Criteria<Issue> criteria, StatsGroup group) {
		CriteriaBuilder builder = dao.getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<IssueStateHistory> root = criteriaQuery.from(IssueStateHistory.class);
		Join<Issue, Issue> issue = root.join(PROP_ISSUE, JoinType.INNER);

		var predicates = new ArrayList<Predicate>();
		predicates.addAll(asList(issueManager.buildPredicates(projectScope, criteria, criteriaQuery, builder, issue)));
		predicates.add(builder.isNotNull(root.get(PROP_DURATION)));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		
		var timePath = group.getPath(root.get(PROP_TIME_GROUPS));
		var statePath = root.get(PROP_STATE);
		criteriaQuery.groupBy(timePath, statePath);

		criteriaQuery.multiselect(newArrayList(
				timePath, statePath, builder.avg(root.get(PROP_DURATION))));

		Map<Integer, Map<String, Integer>> stats = new HashMap<>();
		for (var result: getSession().createQuery(criteriaQuery).getResultList()) {
			int time = (int)result[0];
			String state = (String)result[1];
			var duration = ((Double)result[2])/60000;
			stats.computeIfAbsent(time, it -> new HashMap<>()).put(state, (int)duration);
		}
		return stats;
	}

	@Sessional
	@Override
	public Map<Integer, Map<String, Integer>> queryFrequencyStats(ProjectScope projectScope,
																 @Nullable Criteria<Issue> criteria, StatsGroup group) {
		CriteriaBuilder builder = dao.getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<IssueStateHistory> root = criteriaQuery.from(IssueStateHistory.class);
		Join<Issue, Issue> issue = root.join(PROP_ISSUE, JoinType.INNER);

		criteriaQuery.where(issueManager.buildPredicates(projectScope, criteria, criteriaQuery, builder, issue));

		var timePath = group.getPath(root.get(PROP_TIME_GROUPS));
		var statePath = root.get(PROP_STATE);
		criteriaQuery.groupBy(timePath, statePath);

		criteriaQuery.multiselect(newArrayList(timePath, statePath, builder.count(root)));

		Map<Integer, Map<String, Integer>> stats = new HashMap<>();
		for (var result: getSession().createQuery(criteriaQuery).getResultList()) {
			int time = (int)result[0];
			String state = (String)result[1];
			var duration = ((Long)result[2]).intValue();
			stats.computeIfAbsent(time, it -> new HashMap<>()).put(state, duration);
		}
		return stats;
	}

}
