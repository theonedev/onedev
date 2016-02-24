package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.User;

@SuppressWarnings("serial")
class ReferencePullRequest extends AbstractRenderableActivity {

	private final Long referencedByRequestId;
	
	public ReferencePullRequest(PullRequest request, User user, Date date, PullRequest referencedByRequest) {
		super(request, user, date);
		this.referencedByRequestId = referencedByRequest.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new ReferenceActivityPanel(panelId, this, new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return GitPlex.getInstance(Dao.class).load(PullRequest.class, referencedByRequestId);
			}
			
		});
	}

}
