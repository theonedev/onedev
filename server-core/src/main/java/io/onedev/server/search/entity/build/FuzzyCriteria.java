package io.onedev.server.search.entity.build;

import com.google.common.base.Splitter;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;

public class FuzzyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		return parse(value).getPredicate(query, from, builder);
	}

	@Override
	public boolean matches(Build build) {
		return parse(value).matches(build);
	}
	
	private Criteria<Build> parse(String value) {
		var criterias = new ArrayList<Criteria<Build>>();
		for (var part: Splitter.on(' ').omitEmptyStrings().trimResults().split(value)) {
			criterias.add(new OrCriteria<>(newArrayList(
					new VersionCriteria("*" + part + "*"),
					new JobCriteria("*" + part + "*")
			)));
		}
		return new AndCriteria<>(criterias);
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
