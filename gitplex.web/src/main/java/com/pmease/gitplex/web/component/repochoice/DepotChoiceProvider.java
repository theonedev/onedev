package com.pmease.gitplex.web.component.repochoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.component.select2.ResponseFiller;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public class DepotChoiceProvider extends ChoiceProvider<Depot> {

	private final IModel<Account> userModel;
	
	private final IModel<List<Depot>> repositoriesModel;
	
	public DepotChoiceProvider(final @Nullable IModel<Account> userModel) {
		this.userModel = userModel;
		
		repositoriesModel = new LoadableDetachableModel<List<Depot>>() {

			@Override
			protected List<Depot> load() {
				EntityCriteria<Depot> criteria = EntityCriteria.of(Depot.class);
				if (getUser() != null) 
					criteria.add(Restrictions.eq("owner", getUser()));
				
				List<Depot> repositories = GitPlex.getInstance(Dao.class).query(criteria);

				for (Iterator<Depot> it = repositories.iterator(); it.hasNext();) {
					if (!SecurityUtils.canPull(it.next()))
						it.remove();
				}
				
				Collections.sort(repositories, new Comparator<Depot>() {

					@Override
					public int compare(Depot repo1, Depot repo2) {
						if (repo1.getOwner().getName().compareTo(repo2.getOwner().getName()) < 0)
							return -1;
						else if (repo1.getOwner().getName().compareTo(repo2.getOwner().getName()) > 0)
							return 1;
						else
							return repo1.getName().compareTo(repo2.getName());
					}
					
				});
				
				return repositories;
			}

		};
	}
	
	@Override
	public void toJson(Depot choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId());
		writer.key("name");
		
		if (getUser() != null)
			writer.value(StringEscapeUtils.escapeHtml4(choice.getName()));
		else
			writer.value(StringEscapeUtils.escapeHtml4(choice.getOwner().getName() + "/" + choice.getName()));
	}
	
	@Nullable
	private Account getUser() {
		if (userModel != null)
			return userModel.getObject();
		else
			return null;
	}

	@Override
	public Collection<Depot> toChoices(Collection<String> ids) {
		List<Depot> list = Lists.newArrayList();
		Dao dao = GitPlex.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			list.add(dao.load(Depot.class, id));
		}
		
		return list;
	}

	public void detach() {
		if (userModel != null)
			userModel.detach();
		repositoriesModel.detach();
		
		super.detach();
	}

	@Override
	public void query(String term, int page, Response<Depot> response) {
		term = term.toLowerCase();
		String userName;
		String depotName;
		if (term.indexOf('/') != -1) {
			userName = StringUtils.substringBefore(term, "/");
			depotName = StringUtils.substringAfter(term, "/");
		} else {
			userName = term;
			depotName = null;
		}
		List<Depot> depots = new ArrayList<>();
		for (Depot depot: repositoriesModel.getObject()) {
			if (depotName != null) {
				if (depot.getOwner().getName().toLowerCase().startsWith(userName) 
						&& depot.getName().toLowerCase().startsWith(depotName)) {
					depots.add(depot);
				}
			} else if (depot.getOwner().getName().toLowerCase().startsWith(userName)) {
				depots.add(depot);
			}
		}
		new ResponseFiller<Depot>(response).fill(depots, page, Constants.DEFAULT_PAGE_SIZE);
	}
	
}
