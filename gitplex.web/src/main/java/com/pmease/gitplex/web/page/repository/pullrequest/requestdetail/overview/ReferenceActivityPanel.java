package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.pullrequest.requeststatus.RequestStatusPanel;
import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
public class ReferenceActivityPanel extends Panel {

	private final IModel<PullRequest> requestModel;
	
	private final IModel<PullRequest> referencedByRequestModel;
	
	private final IModel<User> userModel;
	
	private final Date date;

	public ReferenceActivityPanel(String id, IModel<PullRequest> requestModel, 
			IModel<PullRequest> referencedByModel, IModel<User> userModel, Date date) {
		super(id);
		
		this.requestModel = requestModel;
		this.referencedByRequestModel = referencedByModel;
		this.userModel = userModel;
		this.date = date;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new UserLink("user", userModel));
		add(new Label("age", DateUtils.formatAge(date)));
		
		BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", 
				RequestOverviewPage.class, RequestOverviewPage.paramsOf(referencedByRequestModel.getObject()));
		link.add(new Label("id", "#" + referencedByRequestModel.getObject().getId()));
		link.add(new Label("title", referencedByRequestModel.getObject().getTitle()));
		add(link);
		
		add(new RequestStatusPanel("status", referencedByRequestModel));
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		referencedByRequestModel.detach();
		userModel.detach();
		super.onDetach();
	}

}
