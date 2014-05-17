package com.pmease.gitop.web.component.choice;

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
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public class ComparableRepositoryChoiceProvider extends ChoiceProvider<Repository> {

	private static final int PAGE_SIZE = 25;
	
	private IModel<Repository> currentRepositoryModel;
	
	private IModel<List<Repository>> comparableRepositoriesModel;

	public ComparableRepositoryChoiceProvider(IModel<Repository> currentRepositoryModel) {
		this.currentRepositoryModel = currentRepositoryModel;
		
		comparableRepositoriesModel = new LoadableDetachableModel<List<Repository>>() {

			@Override
			protected List<Repository> load() {
				List<Repository> comparableRepositories = getCurrentRepository().findComparables();
				comparableRepositories.remove(getCurrentRepository());
				if (getCurrentRepository().getForkedFrom() != null)
					comparableRepositories.remove(getCurrentRepository().getForkedFrom());
				Collections.sort(comparableRepositories, new Comparator<Repository>() {

					@Override
					public int compare(Repository repository1, Repository repository2) {
						return repository1.getUser().getName().compareTo(repository2.getUser().getName());
					}
					
				});
				if (getCurrentRepository().getForkedFrom() != null)
					comparableRepositories.add(0, getCurrentRepository().getForkedFrom());
				comparableRepositories.add(0, getCurrentRepository());
				for (Iterator<Repository> it = comparableRepositories.iterator(); it.hasNext();) {
					if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(it.next())))
						it.remove();
				}
				return comparableRepositories;
			}
			
		};
	}
	
	@Override
	public void query(String term, int page, Response<Repository> response) {
		List<Repository> repositories = new ArrayList<>();
		for (Repository repository: getComparableRepositories()) {
			if (repository.getOwner().getName().contains(term))
				repositories.add(repository);
		}
		int first = page * PAGE_SIZE;
		if (first + PAGE_SIZE < repositories.size()) {
			response.addAll(repositories.subList(first, first + PAGE_SIZE));
			response.setHasMore(true);
		} else if (first + PAGE_SIZE == repositories.size()) {
			response.addAll(repositories.subList(first, first + PAGE_SIZE));
			response.setHasMore(false);
		} else {
			response.addAll(repositories.subList(first, repositories.size()));
			response.setHasMore(false);
		}
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
		Dao dao = Gitop.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			repositories.add(dao.load(Repository.class, id));
		}

		return repositories;
	}

	private Repository getCurrentRepository() {
		return currentRepositoryModel.getObject();
	}
	
	private List<Repository> getComparableRepositories() {
		return comparableRepositoriesModel.getObject();
	}

	@Override
	public void detach() {
		super.detach();
		currentRepositoryModel.detach();
		comparableRepositoriesModel.detach();
	}
	
}