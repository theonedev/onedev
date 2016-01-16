package com.pmease.gitplex.web.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.permission.ObjectPermission;

@SuppressWarnings("serial")
public class AffinalRepositoriesModel extends LoadableDetachableModel<List<Repository>> {

	private final Long repoId;
	
	public AffinalRepositoriesModel(Long repoId) {
		this.repoId = repoId;
	}
	
	@Override
	protected List<Repository> load() {
		Repository repository = GitPlex.getInstance(Dao.class).load(Repository.class, repoId);;
		List<Repository> affinals = repository.findAffinals();
		affinals.remove(repository);
		if (repository.getForkedFrom() != null)
			affinals.remove(repository.getForkedFrom());
		Collections.sort(affinals, new Comparator<Repository>() {

			@Override
			public int compare(Repository repo1, Repository repo2) {
				if (repo1.getUser().equals(repo2.getUser()))
					return repo1.getName().compareTo(repo2.getName());
				else
					return repo1.getUser().getName().compareTo(repo2.getUser().getName());
			}
			
		});
		if (repository.getForkedFrom() != null)
			affinals.add(0, repository.getForkedFrom());
		affinals.add(0, repository);
		for (Iterator<Repository> it = affinals.iterator(); it.hasNext();) {
			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepoPull(it.next())))
				it.remove();
		}
		return affinals;
	}

}
