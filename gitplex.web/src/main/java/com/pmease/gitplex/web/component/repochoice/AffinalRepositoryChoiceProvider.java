package com.pmease.gitplex.web.component.repochoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.component.select2.ListChoiceProvider;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.Constants;

@SuppressWarnings("serial")
public class AffinalRepositoryChoiceProvider extends ListChoiceProvider<Repository> {

	private IModel<Repository> currentRepositoryModel;
	
	private IModel<List<Repository>> affinalRepositoriesModel;

	public AffinalRepositoryChoiceProvider(IModel<Repository> currentRepositoryModel) {
		super(Constants.DEFAULT_PAGE_SIZE);
		
		this.currentRepositoryModel = currentRepositoryModel;
		
		affinalRepositoriesModel = new LoadableDetachableModel<List<Repository>>() {

			@Override
			protected List<Repository> load() {
				List<Repository> affinalRepositories = getCurrentRepository().findAffinals();
				affinalRepositories.remove(getCurrentRepository());
				if (getCurrentRepository().getForkedFrom() != null)
					affinalRepositories.remove(getCurrentRepository().getForkedFrom());
				Collections.sort(affinalRepositories, new Comparator<Repository>() {

					@Override
					public int compare(Repository repository1, Repository repository2) {
						return repository1.getUser().getName().compareTo(repository2.getUser().getName());
					}
					
				});
				if (getCurrentRepository().getForkedFrom() != null)
					affinalRepositories.add(0, getCurrentRepository().getForkedFrom());
				affinalRepositories.add(0, getCurrentRepository());
				for (Iterator<Repository> it = affinalRepositories.iterator(); it.hasNext();) {
					if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepoPull(it.next())))
						it.remove();
				}
				return affinalRepositories;
			}
			
		};
	}
	
	@Override
	public void toJson(Repository choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId());
		writer.key("name");
		String value;
		if (getCurrentRepository().equals(choice))
			value = "<i>Current</i>";
		else if (choice.equals(getCurrentRepository().getForkedFrom()))
			value = "<i>UpStream</i>";
		else
			value = StringEscapeUtils.escapeHtml4(choice.getOwner().getName() + "/" + choice.getName());
		writer.value(value);
	}

	@Override
	public Collection<Repository> toChoices(Collection<String> ids) {
		List<Repository> repositories = Lists.newArrayList();
		Dao dao = GitPlex.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			repositories.add(dao.load(Repository.class, id));
		}

		return repositories;
	}

	private Repository getCurrentRepository() {
		return currentRepositoryModel.getObject();
	}
	
	private List<Repository> getAffinalRepositories() {
		return affinalRepositoriesModel.getObject();
	}

	@Override
	public void detach() {
		super.detach();
		currentRepositoryModel.detach();
		affinalRepositoriesModel.detach();
	}

	@Override
	protected List<Repository> filterList(String term) {
		term = term.toLowerCase();
		List<Repository> repositories = new ArrayList<>();
		for (Repository repository: getAffinalRepositories()) {
			if (repository.getOwner().getName().toLowerCase().startsWith(term))
				repositories.add(repository);
		}
		return repositories;
	}
	
}