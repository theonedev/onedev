package io.onedev.server.search.entity.pack;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Pack;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class FuzzyCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		return parse(value).getPredicate(query, from, builder);
	}

	@Override
	public boolean matches(Pack build) {
		return parse(value).matches(build);
	}
	
	private Criteria<Pack> parse(String value) {
		return new OrCriteria<Pack>(
				new TagCriteria("*" + value + "*", PackQueryLexer.Is),
				new GroupIdCriteria("*" + value + "*", PackQueryLexer.Is),
				new ArtiractIdCriteria("*" + value + "*", PackQueryLexer.Is),
				new VersionCriteria("*" + value + "*", PackQueryLexer.Is),
				new NameCriteria("*" + value + "*", PackQueryLexer.Is));
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
