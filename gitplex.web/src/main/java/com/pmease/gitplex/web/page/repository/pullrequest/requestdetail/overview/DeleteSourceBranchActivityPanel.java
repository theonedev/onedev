package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
public class DeleteSourceBranchActivityPanel extends AbstractActivityPanel {

	public DeleteSourceBranchActivityPanel(String id, RenderableActivity activity) {
		super(id, activity);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("user", userModel));
		add(new Label("age", DateUtils.formatAge(activity.getDate())));
		
		add(new Label("branch", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				PullRequest request = requestModel.getObject();
				if (request.getSourceRepo().equals(request.getTargetRepo()))
					return request.getSourceBranch();
				else
					return request.getSource().getFQN();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(requestModel.getObject().getSourceRepo() != null);
			}
			
		});
	}

}
