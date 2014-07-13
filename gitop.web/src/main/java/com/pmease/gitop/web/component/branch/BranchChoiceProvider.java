package com.pmease.gitop.web.component.branch;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.model.IModel;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Branch;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class BranchChoiceProvider extends ChoiceProvider<Branch> {

	private static final long serialVersionUID = 1L;

	IModel<EntityCriteria<Branch>> criteria;

	public BranchChoiceProvider(IModel<EntityCriteria<Branch>> criteria) {
		this.criteria = criteria;
	}

	@Override
	public void query(String term, int page, Response<Branch> response) {
		EntityCriteria<Branch> crit = criteria == null ? null : criteria.getObject();
		if (crit == null) {
			crit = EntityCriteria.of(Branch.class);
		}

		crit.add(Restrictions.ilike("name", term, MatchMode.START));
		crit.addOrder(Order.asc("name"));
		int first = page * 10;

		List<Branch> branches = Gitop.getInstance(Dao.class).query(crit, first, 10);
		
		response.addAll(branches);
	}

	@Override
	public void toJson(Branch choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(StringEscapeUtils.escapeHtml4(choice.getName()));
	}

	@Override
	public Collection<Branch> toChoices(Collection<String> ids) {
		List<Branch> branches = Lists.newArrayList();
		Dao dao = Gitop.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			branches.add(dao.load(Branch.class, id));
		}

		return branches;
	}

	@Override
	public void detach() {
		super.detach();

		if (criteria != null) {
			criteria.detach();
		}
	}
}