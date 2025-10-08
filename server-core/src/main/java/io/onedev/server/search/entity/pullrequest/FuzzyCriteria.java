package io.onedev.server.search.entity.pullrequest;

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
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entitytext.PullRequestTextService;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class FuzzyCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private static final int MAX_TEXT_QUERY_COUNT = 1000;

	private final String value;

	private transient List<Long> requestIds;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		if (requestIds == null) {
			if (value.length() == 0)
				return builder.conjunction();
			var project = projectScope!=null? projectScope.getProject(): null;
			requestIds = OneDev.getInstance(PullRequestTextService.class).query(project, value, MAX_TEXT_QUERY_COUNT);
		}
		if (requestIds.isEmpty())
			return builder.disjunction();
		else
			return builder.in(from.get(PullRequest.PROP_ID)).value(requestIds);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (value.length() == 0)
			return true;
		else
			return OneDev.getInstance(PullRequestTextService.class).matches(request, value);
	}

	@Override
	public List<Order> getPreferOrders(CriteriaBuilder builder, From<PullRequest, PullRequest> from) {
		if (requestIds != null && !requestIds.isEmpty()) {
			var orders = new ArrayList<Order>();
			var orderCase = builder.selectCase();
			for (int i = 0; i < requestIds.size(); i++) 
				orderCase.when(builder.equal(from.get(PullRequest.PROP_ID), requestIds.get(i)), i);
			orders.add(builder.asc(orderCase.otherwise(requestIds.size())));
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
