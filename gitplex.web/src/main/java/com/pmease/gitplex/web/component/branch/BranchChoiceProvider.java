package com.pmease.gitplex.web.component.branch;

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
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.Constants;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public class BranchChoiceProvider extends ChoiceProvider<Branch> {

	private IModel<Repository> repoModel;

	public BranchChoiceProvider(IModel<Repository> repoModel) {
		this.repoModel = repoModel;
	}

	@Override
	public void query(String term, int page, Response<Branch> response) {
		EntityCriteria<Branch> criteria = EntityCriteria.of(Branch.class);
		criteria.add(Restrictions.eq("repository", repoModel.getObject()));
		
		criteria.add(Restrictions.ilike("name", term, MatchMode.START));
		criteria.addOrder(Order.asc("name"));
		int first = page * Constants.DEFAULT_PAGE_SIZE;

		List<Branch> branches = GitPlex.getInstance(Dao.class).query(criteria, first, Constants.DEFAULT_PAGE_SIZE+1);
		if (branches.size() > Constants.DEFAULT_PAGE_SIZE) {
			branches.remove(branches.size()-1);
			response.addAll(branches);
			response.setHasMore(true);
		} else {
			response.addAll(branches);
			response.setHasMore(false);
		}
	}

	@Override
	public void toJson(Branch choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(StringEscapeUtils.escapeHtml4(choice.getName()));
	}

	@Override
	public Collection<Branch> toChoices(Collection<String> ids) {
		List<Branch> branches = Lists.newArrayList();
		Dao dao = GitPlex.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			branches.add(dao.load(Branch.class, id));
		}

		return branches;
	}

	@Override
	public void detach() {
		repoModel.detach();
		
		super.detach();
	}
}