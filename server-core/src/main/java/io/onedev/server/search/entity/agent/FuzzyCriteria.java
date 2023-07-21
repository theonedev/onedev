package io.onedev.server.search.entity.agent;

import com.google.common.base.Splitter;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Agent;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;

public class FuzzyCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		return parse(value).getPredicate(query, from, builder);
	}

	@Override
	public boolean matches(Agent agent) {
		return parse(value).matches(agent);
	}
	
	private Criteria<Agent> parse(String value) {
		var criterias = new ArrayList<Criteria<Agent>>();
		for (var part: Splitter.on(' ').omitEmptyStrings().trimResults().split(value)) {
			criterias.add(new OrCriteria<>(newArrayList(
					new NameCriteria("*" + part + "*"),
					new IpAddressCriteria("*" + part + "*"),
					new OsCriteria("*" + part + "*"),
					new OsVersionCriteria("*" + part + "*"),
					new OsArchCriteria("*" + part + "*")
			)));
		}
		return new AndCriteria<>(criterias);
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
