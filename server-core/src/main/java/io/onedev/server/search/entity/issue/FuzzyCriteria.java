package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.model.Issue;
import io.onedev.server.search.entitytext.IssueTextService;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class FuzzyCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private static final int MAX_TEXT_QUERY_COUNT = 1000;

	private final String value;

	private transient List<Long> issueIds;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		if (issueIds == null) {
			if (value.length() == 0)
				return builder.conjunction();
			issueIds = OneDev.getInstance(IssueTextService.class).query(projectScope, value, MAX_TEXT_QUERY_COUNT);
		}
		if (issueIds.isEmpty())
			return builder.disjunction();
		else
			return builder.in(from.get(Issue.PROP_ID)).value(issueIds);
	}

	@Override
	public boolean matches(Issue issue) {
		if (value.length() == 0)
			return true;
		else
			return OneDev.getInstance(IssueTextService.class).matches(issue, value);
	}

	@Override
	public List<Order> getPreferOrders(CriteriaBuilder builder, From<Issue, Issue> from) {
		if (issueIds != null && !issueIds.isEmpty()) {
			var orders = new ArrayList<Order>();
			var orderCase = builder.selectCase();
			for (int i = 0; i < issueIds.size(); i++) 
				orderCase.when(builder.equal(from.get(Issue.PROP_ID), issueIds.get(i)), i);
			orders.add(builder.asc(orderCase.otherwise(issueIds.size())));
			return orders;
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
