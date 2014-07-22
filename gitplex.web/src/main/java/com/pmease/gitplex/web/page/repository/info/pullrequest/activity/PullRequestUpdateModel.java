package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequestUpdate;

@SuppressWarnings("serial")
public class PullRequestUpdateModel extends LoadableDetachableModel<PullRequestUpdate> {

	private final Long updateId;
	
	public PullRequestUpdateModel(Long updateId) {
		this.updateId = updateId;
	}
	
	@Override
	protected PullRequestUpdate load() {
		return GitPlex.getInstance(Dao.class).load(PullRequestUpdate.class, updateId);
	}

}
