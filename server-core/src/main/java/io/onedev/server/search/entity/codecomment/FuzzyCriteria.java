package io.onedev.server.search.entity.codecomment;

import com.google.common.base.Splitter;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.CodeComment;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;

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
		var criterias = new ArrayList<Criteria<CodeComment>>();
		for (var part: Splitter.on(' ').omitEmptyStrings().trimResults().split(value)) {
			criterias.add(new OrCriteria<>(newArrayList(
					new ContentCriteria(part),
					new ReplyCriteria(part),
					new PathCriteria("*" + part + "*")
			)));
		}
		return new AndCriteria<>(criterias);
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
