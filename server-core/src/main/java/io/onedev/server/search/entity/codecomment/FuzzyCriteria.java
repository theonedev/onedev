package io.onedev.server.search.entity.codecomment;

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
import io.onedev.server.model.CodeComment;
import io.onedev.server.search.entitytext.CodeCommentTextService;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class FuzzyCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private static final int MAX_TEXT_QUERY_COUNT = 1000;

	private final String value;

	private transient List<Long> commentIds;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		if (commentIds == null) {
			if (value.length() == 0)
				return builder.conjunction();
			var project = projectScope!=null? projectScope.getProject(): null;
			commentIds = OneDev.getInstance(CodeCommentTextService.class).query(project, value, MAX_TEXT_QUERY_COUNT);
		}
		if (commentIds.isEmpty())
			return builder.disjunction();
		else
			return builder.in(from.get(CodeComment.PROP_ID)).value(commentIds);
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (value.length() == 0)
			return true;
		else
			return OneDev.getInstance(CodeCommentTextService.class).matches(comment, value);
	}

	@Override
	public List<Order> getPreferOrders(CriteriaBuilder builder, From<CodeComment, CodeComment> from) {
		if (commentIds != null && !commentIds.isEmpty()) {
			var orders = new ArrayList<Order>();
			var orderCase = builder.selectCase();
			for (int i = 0; i < commentIds.size(); i++) 
				orderCase.when(builder.equal(from.get(CodeComment.PROP_ID), commentIds.get(i)), i);
			orders.add(builder.asc(orderCase.otherwise(commentIds.size())));
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
