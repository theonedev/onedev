package io.onedev.server.service.impl;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.model.AbstractEntity.PROP_ID;
import static io.onedev.server.model.IssueStateHistory.PROP_DATE;
import static io.onedev.server.model.IssueStateHistory.PROP_DURATION;
import static io.onedev.server.model.IssueStateHistory.PROP_ISSUE;
import static io.onedev.server.model.IssueStateHistory.PROP_STATE;
import static io.onedev.server.model.IssueStateHistory.PROP_TIME_GROUPS;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.shiro.subject.Subject;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jetbrains.annotations.Nullable;

import io.onedev.server.service.IssueService;
import io.onedev.server.service.IssueStateHistoryService;
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
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.util.StatsGroup;

@Singleton
public class DefaultIssueStateHistoryService extends BaseEntityService<IssueStateHistory> implements IssueStateHistoryService {

	@Inject
	private IssueService issueService;

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
					updateDuration(change.getIssue(), history.getDate());
					dao.persist(history);
				}
			} else if (change.getData() instanceof IssueBatchUpdateData) {
				var changeData = (IssueBatchUpdateData) change.getData();
				if (!changeData.getOldState().equals(changeData.getNewState())) {
					var history = create(change.getIssue(), changeData.getNewState());
					updateDuration(change.getIssue(), history.getDate());
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
	
	private void updateDuration(Issue issue, Date date) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_ISSUE, issue));
		criteria.addOrder(Order.desc(PROP_ID));
		var lastHistory = find(criteria);
		if (lastHistory != null)
			lastHistory.setDuration(date.getTime() - lastHistory.getDate().getTime());
	}

	@Sessional
	@Override
	public Map<Integer, Map<String, Integer>> queryDurationStats(Subject subject, ProjectScope projectScope, 
			@Nullable Criteria<Issue> issueCriteria, @Nullable Date startDate, @Nullable Date endDate, 
			StatsGroup statsGroup) {
		CriteriaBuilder builder = dao.getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<IssueStateHistory> root = criteriaQuery.from(IssueStateHistory.class);
		Join<Issue, Issue> issue = root.join(PROP_ISSUE, JoinType.INNER);

		var predicates = new ArrayList<Predicate>(asList(issueService.buildPredicates(subject, projectScope, issueCriteria, criteriaQuery, builder, issue)));
		if (startDate != null)
			predicates.add(builder.greaterThanOrEqualTo(root.get(PROP_DATE), startDate));
		if (endDate != null)
			predicates.add(builder.lessThanOrEqualTo(root.get(PROP_DATE), endDate));
		predicates.add(builder.isNotNull(root.get(PROP_DURATION)));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		
		var timePath = statsGroup.getPath(root.get(PROP_TIME_GROUPS));
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
	public Map<Integer, Map<String, Integer>> queryFrequencyStats(Subject subject, ProjectScope projectScope, 
			@Nullable Criteria<Issue> issueCriteria, @Nullable Date startDate, @Nullable Date endDate, 
			StatsGroup statsGroup) {
		CriteriaBuilder builder = dao.getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<IssueStateHistory> root = criteriaQuery.from(IssueStateHistory.class);
		Join<Issue, Issue> issue = root.join(PROP_ISSUE, JoinType.INNER);

		var predicates = new ArrayList<Predicate>(asList(issueService.buildPredicates(subject, projectScope, issueCriteria, criteriaQuery, builder, issue)));
		if (startDate != null)
			predicates.add(builder.greaterThanOrEqualTo(root.get(PROP_DATE), startDate));
		if (endDate != null)
			predicates.add(builder.lessThanOrEqualTo(root.get(PROP_DATE), endDate));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));

		var timePath = statsGroup.getPath(root.get(PROP_TIME_GROUPS));
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

	@Sessional
	@Override
	public Map<Integer, Map<String, Integer>> queryTrendStats(Subject subject, ProjectScope projectScope, 
			@Nullable Criteria<Issue> issueCriteria, @Nullable Date startDate, @Nullable Date endDate, 
			StatsGroup statsGroup) {
		Map<Long, String> issueStates = new HashMap<>();

		CriteriaBuilder builder = dao.getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<IssueStateHistory> root = criteriaQuery.from(IssueStateHistory.class);
		Join<Issue, Issue> issue = root.join(PROP_ISSUE, JoinType.INNER);
		var issuePredicates = Arrays.asList(issueService.buildPredicates(subject, projectScope, issueCriteria, criteriaQuery, builder, issue));
		var statePath = root.get(PROP_STATE);

		if (startDate != null) {
			criteriaQuery.multiselect(newArrayList(root.get(PROP_ISSUE).get(Issue.PROP_ID), statePath));

			Subquery<Date> subquery = criteriaQuery.subquery(Date.class);
			Root<IssueStateHistory> subRoot = subquery.from(IssueStateHistory.class);
			
			subquery.select(builder.max(subRoot.get(PROP_DATE)).as(Date.class));
			subquery.where(
				builder.equal(subRoot.get(PROP_ISSUE), root.get(PROP_ISSUE)), 
				builder.lessThan(subRoot.get(PROP_DATE), startDate));

			var predicates = new ArrayList<Predicate>(issuePredicates);
			predicates.add(builder.equal(root.get(PROP_DATE), subquery));
			criteriaQuery.where(predicates.toArray(new Predicate[0]));
			for (Object[] result: getSession().createQuery(criteriaQuery).getResultList()) {
				long issueId = ((Number) result[0]).longValue();	
				String state = (String) result[1];
				issueStates.put(issueId, state);
			}
		}

		criteriaQuery.multiselect(newArrayList(root.get(PROP_ISSUE).get(Issue.PROP_ID), statePath));
								
		var predicates = new ArrayList<Predicate>(issuePredicates);
		if (startDate != null)
			predicates.add(builder.greaterThanOrEqualTo(root.get(PROP_DATE), startDate));
		if (endDate != null)
			predicates.add(builder.lessThanOrEqualTo(root.get(PROP_DATE), endDate));

		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get(PROP_ID)));

		var timePath = statsGroup.getPath(root.get(PROP_TIME_GROUPS));

		criteriaQuery.multiselect(newArrayList(timePath, statePath, root.get(PROP_ISSUE).get(Issue.PROP_ID)));

		Map<Integer, Map<String, Integer>> stats = new HashMap<>();
		
		int lastTime = 0;
		for (Object[] result: getSession().createQuery(criteriaQuery).getResultList()) {
			int time = ((Number) result[0]).intValue();
			String state = (String) result[1];
			long issueId = ((Number) result[2]).longValue();
			issueStates.put(issueId, state);
			if (time != lastTime && lastTime != 0) 
				stats.put(lastTime, getStateCount(issueStates));
			lastTime = time;
		}
		if (lastTime != 0) 
			stats.put(lastTime, getStateCount(issueStates));
		
		return stats;
	}

	private Map<String, Integer> getStateCount(Map<Long, String> issueStates) {
		Map<String, Integer> stateCount = new HashMap<>();
		for (var entry: issueStates.entrySet()) {
			stateCount.compute(entry.getValue(), (k,v) -> v == null ? 1 : v+1);
		}
		return stateCount;
	}

}
