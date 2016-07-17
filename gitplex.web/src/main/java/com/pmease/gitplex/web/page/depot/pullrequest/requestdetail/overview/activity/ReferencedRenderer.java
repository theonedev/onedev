package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

@SuppressWarnings("serial")
public class ReferencedRenderer extends AbstractRenderer {

	private final Long referencedByRequestId;
	
	public ReferencedRenderer(PullRequest request, Account user, Date date, PullRequest referencedByRequest) {
		super(request, user, date);
		this.referencedByRequestId = referencedByRequest.getId();
	}
	
	@Override
	public ActivityPanel render(String panelId) {
		return new ReferencedPanel(panelId, this, new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return GitPlex.getInstance(Dao.class).load(PullRequest.class, referencedByRequestId);
			}
			
		});
	}

}
