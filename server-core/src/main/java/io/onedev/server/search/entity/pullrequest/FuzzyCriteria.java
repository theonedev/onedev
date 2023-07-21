package io.onedev.server.search.entity.pullrequest;

import com.google.common.base.Splitter;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;

public class FuzzyCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		return parse(value).getPredicate(query, from, builder);
	}

	@Override
	public boolean matches(PullRequest request) {
		return parse(value).matches(request);
	}
	
	private Criteria<PullRequest> parse(String value) {
		var criterias = new ArrayList<Criteria<PullRequest>>();
		for (var part: Splitter.on(' ').omitEmptyStrings().trimResults().split(value)) {
			criterias.add(new OrCriteria<>(newArrayList(
					new TitleCriteria(part),
					new DescriptionCriteria(part),
					new CommentCriteria(part)
			)));
		}
		return new AndCriteria<>(criterias);
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
