package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
public class DeleteSourceBranchActivityPanel extends Panel {

	private final IModel<PullRequest> requestModel;
	
	private final IModel<User> userModel;
	
	private final Date date;
	
	public DeleteSourceBranchActivityPanel(String id, IModel<PullRequest> requestModel, 
			IModel<User> userModel, Date date) {
		super(id);
		
		this.requestModel = requestModel;
		this.userModel = userModel;
		this.date = date;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("user", userModel));
		add(new Label("age", DateUtils.formatAge(date)));
		
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

	@Override
	protected void onDetach() {
		requestModel.detach();
		userModel.detach();
		
		super.onDetach();
	}

}
