package io.onedev.server.search.entity.codecomment;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.CodeComment;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class FuzzyCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		return parse(value).getPredicate(query, from, builder);
	}

	@Override
	public boolean matches(CodeComment comment) {
		return parse(value).matches(comment);
	}
	
	private Criteria<CodeComment> parse(String value) {
		return new OrCriteria<>(
				new ContentCriteria(value), 
				new ReplyCriteria(value), 
				new PathCriteria("*" + value + "*"));
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
