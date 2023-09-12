package io.onedev.server.search.entity.agent;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Agent;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

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
		return new OrCriteria<>(
				new NameCriteria("*" + value + "*"),
				new IpAddressCriteria("*" + value + "*"),
				new OsCriteria("*" + value + "*"),
				new OsVersionCriteria("*" + value + "*"),
				new OsArchCriteria("*" + value + "*"));
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
