package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.component.pullrequest.requeststatus.RequestStatusPanel;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.ActivityRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.pmease.gitplex.web.util.DateUtils;

@SuppressWarnings("serial")
class ReferencedPanel extends ActivityPanel {

	private final IModel<PullRequest> referencedByRequestModel;
	
	public ReferencedPanel(String id, ActivityRenderer activity, 
			IModel<PullRequest> referencedByModel) {
		super(id, activity);
		
		this.referencedByRequestModel = referencedByModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AvatarLink("avatar", userModel.getObject(), null));
		add(new AccountLink("name", userModel.getObject()));
		add(new Label("age", DateUtils.formatAge(renderer.getDate())));
		
		BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", 
				RequestOverviewPage.class, RequestOverviewPage.paramsOf(referencedByRequestModel.getObject()));
		link.add(new Label("id", "#" + referencedByRequestModel.getObject().getId()));
		link.add(new Label("title", referencedByRequestModel.getObject().getTitle()));
		add(link);
		
		add(new RequestStatusPanel("status", referencedByRequestModel, false));
	}

	@Override
	protected void onDetach() {
		referencedByRequestModel.detach();
		super.onDetach();
	}

}
