package com.pmease.gitop.web.page.project.pullrequest;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.web.page.project.RepositoryTabPage;

@SuppressWarnings("serial")
public class RequestDetailPage extends RepositoryTabPage {

	private IModel<PullRequest> model;
	
	public RequestDetailPage(final PageParameters params) {
		super(params);
		
		model = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return Gitop.getInstance(PullRequestManager.class).load(params.get(0).toLong());
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		add(new RequestDetailPanel("content", model));
	}
	
	public PullRequest getPullRequest() {
		return model.getObject();
	}

	@Override
	public void onDetach() {
		model.detach();
		super.onDetach();
	}
	
}