package io.onedev.server.model.support.issue.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.support.issue.query.IssueSort.Direction;

public class IssueQuery implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final IssueCriteria criteria;
	
	private final List<IssueSort> sorts;
	
	public IssueQuery(@Nullable IssueCriteria criteria, List<IssueSort> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public CriteriaQuery<Issue> buildCriteriaQuery(Session session) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Issue> query = builder.createQuery(Issue.class);
		Root<Issue> root = query.from(Issue.class);

		if (criteria != null) 
			query.where(criteria.getPredicate(new QueryBuildContext(root, builder)));

		List<Order> orders = new ArrayList<>();
		for (IssueSort sort: sorts) {
			if (Issue.BUILTIN_FIELDS.containsKey(sort.getField())) {
				String fieldName = Issue.BUILTIN_FIELDS.get(sort.getField());
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(root.get(fieldName)));
				else
					orders.add(builder.desc(root.get(fieldName)));
			} else {
				Join<Issue, IssueField> join = root.join("fields", JoinType.LEFT);
				join.on(builder.equal(join.get("name"), sort.getField()));
				if (sort.getDirection() == Direction.ASCENDING)
					orders.add(builder.asc(join.get("ordinal")));
				else
					orders.add(builder.desc(join.get("ordinal")));
			}
		}

		if (orders.isEmpty())
			orders.add(builder.desc(root.get("id")));
		query.orderBy(orders);
		
		return query;
	}

	public IssueCriteria getCriteria() {
		return criteria;
	}

	public List<IssueSort> getSorts() {
		return sorts;
	}
	
}
