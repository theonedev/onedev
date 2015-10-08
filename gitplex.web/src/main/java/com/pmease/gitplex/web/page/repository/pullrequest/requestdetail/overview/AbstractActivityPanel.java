package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
abstract class AbstractActivityPanel extends Panel {

	protected final RenderableActivity activity;
	
	protected final IModel<PullRequest> requestModel = new LoadableDetachableModel<PullRequest>() {

		@Override
		protected PullRequest load() {
			return activity.getRequest();
		}
		
	};
	
	protected final IModel<User> userModel = new LoadableDetachableModel<User>() {

		@Override
		protected User load() {
			return activity.getUser();
		}
		
	};
	
	public AbstractActivityPanel(String id, final RenderableActivity activity) {
		super(id);
		
		this.activity = activity;
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		userModel.detach();
		super.onDetach();
	}

}
