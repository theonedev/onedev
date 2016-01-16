package com.pmease.gitplex.web.component.repochoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.component.select2.ListChoiceProvider;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.model.AffinalRepositoriesModel;

@SuppressWarnings("serial")
public class AffinalRepositoryChoiceProvider extends ListChoiceProvider<Repository> {

	private final Long repoId;
	
	private IModel<List<Repository>> affinalReposModel;

	public AffinalRepositoryChoiceProvider(Long repoId) {
		super(Constants.DEFAULT_PAGE_SIZE);
	
		this.repoId = repoId;
		affinalReposModel = new AffinalRepositoriesModel(repoId);
	}
	
	@Override
	public void toJson(Repository choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId());
		writer.key("name");
		String value;
		if (getRepository().equals(choice))
			value = "<i>Current</i>";
		else if (choice.equals(getRepository().getForkedFrom()))
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

	private Repository getRepository() {
		return GitPlex.getInstance(Dao.class).load(Repository.class, repoId);
	}
	
	private List<Repository> getAffinalRepositories() {
		return affinalReposModel.getObject();
	}

	@Override
	public void detach() {
		affinalReposModel.detach();
		
		super.detach();
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