package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.component.UserLink;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.component.pullrequest.requeststatus.RequestStatusPanel;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
class ReferenceActivityPanel extends AbstractActivityPanel {

	private final IModel<PullRequest> referencedByRequestModel;
	
	public ReferenceActivityPanel(String id, RenderableActivity activity, 
			IModel<PullRequest> referencedByModel) {
		super(id, activity);
		
		this.referencedByRequestModel = referencedByModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AvatarLink("avatar", userModel.getObject(), null));
		add(new UserLink("name", userModel.getObject()));
		add(new Label("age", DateUtils.formatAge(activity.getDate())));
		
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
