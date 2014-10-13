package com.pmease.gitplex.web.component.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.component.select2.ListChoiceProvider;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.Constants;

@SuppressWarnings("serial")
public class RepositoryChoiceProvider extends ListChoiceProvider<Repository> {

	private final IModel<User> userModel;
	
	private final IModel<List<Repository>> repositoriesModel;
	
	public RepositoryChoiceProvider(final @Nullable IModel<User> userModel) {
		super(Constants.DEFAULT_PAGE_SIZE);
		
		this.userModel = userModel;
		
		repositoriesModel = new LoadableDetachableModel<List<Repository>>() {

			@Override
			protected List<Repository> load() {
				EntityCriteria<Repository> criteria = EntityCriteria.of(Repository.class);
				if (getUser() != null) 
					criteria.add(Restrictions.eq("owner", getUser()));
				
				List<Repository> repositories = GitPlex.getInstance(Dao.class).query(criteria);

				for (Iterator<Repository> it = repositories.iterator(); it.hasNext();) {
					if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(it.next())))
						it.remove();
				}
				
				Collections.sort(repositories, new Comparator<Repository>() {

					@Override
					public int compare(Repository repo1, Repository repo2) {
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
	public void toJson(Repository choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId());
		writer.key("name");
		if (getUser() != null)
			writer.value(StringEscapeUtils.escapeHtml4(choice.getName()));
		else
			writer.value(StringEscapeUtils.escapeHtml4(choice.getOwner().getName() + "/" + choice.getName()));
	}
	
	@Nullable
	private User getUser() {
		if (userModel != null)
			return userModel.getObject();
		else
			return null;
	}

	@Override
	public Collection<Repository> toChoices(Collection<String> ids) {
		List<Repository> list = Lists.newArrayList();
		Dao dao = GitPlex.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			list.add(dao.load(Repository.class, id));
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
	protected List<Repository> filterList(String term) {
		term = term.toLowerCase();
		String userName;
		String repoName;
		if (term.indexOf('/') != -1) {
			userName = StringUtils.substringBefore(term, "/");
			repoName = StringUtils.substringAfter(term, "/");
		} else {
			userName = term;
			repoName = null;
		}
		List<Repository> repositories = new ArrayList<>();
		for (Repository repository: repositoriesModel.getObject()) {
			if (repoName != null) {
				if (repository.getOwner().getName().toLowerCase().equals(userName) 
						&& repository.getName().toLowerCase().startsWith(repoName)) {
					repositories.add(repository);
				}
			} else if (repository.getOwner().getName().toLowerCase().startsWith(userName)) {
				repositories.add(repository);
			}
		}
		return repositories;
	}
	
}
