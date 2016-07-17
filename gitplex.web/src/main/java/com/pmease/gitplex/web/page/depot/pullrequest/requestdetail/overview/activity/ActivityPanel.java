package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.ActivityRenderer;

@SuppressWarnings("serial")
abstract class ActivityPanel extends Panel {

	protected final ActivityRenderer renderer;
	
	protected final IModel<PullRequest> requestModel = new LoadableDetachableModel<PullRequest>() {

		@Override
		protected PullRequest load() {
			return renderer.getRequest();
		}
		
	};
	
	protected final IModel<Account> userModel = new LoadableDetachableModel<Account>() {

		@Override
		protected Account load() {
			return renderer.getUser();
		}
		
	};
	
	public ActivityPanel(String id, ActivityRenderer renderer) {
		super(id);
		this.renderer = renderer;
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		userModel.detach();
		super.onDetach();
	}

}
