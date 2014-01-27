package com.pmease.gitop.web.page.project.pullrequest;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;

@SuppressWarnings("serial")
public class RequestDetailPage extends ProjectCategoryPage {

	private IModel<PullRequest> requestModel;
	
	public RequestDetailPage(final PageParameters params) {
		super(params);
		
		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return Gitop.getInstance(PullRequestManager.class).load(params.get(0).toLong());
			}
			
		};
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
	}

	public PullRequest getPullRequest() {
		return requestModel.getObject();
	}

	@Override
	public void onDetach() {
		requestModel.detach();
		super.onDetach();
	}
	
}