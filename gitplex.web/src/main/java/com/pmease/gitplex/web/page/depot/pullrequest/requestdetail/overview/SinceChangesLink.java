package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;
import com.pmease.gitplex.web.websocket.PullRequestChanged;

@SuppressWarnings("serial")
public class SinceChangesLink extends BookmarkablePageLink<Void> {

	private final IModel<PullRequest> requestModel;
	
	private final Date sinceDate;
	
	private final IModel<String> oldCommitModel = new LoadableDetachableModel<String>() {

		@Override
		protected String load() {
			PullRequest request = requestModel.getObject();
			String commit = request.getBaseCommitHash();
			for (PullRequestUpdate update: request.getSortedUpdates()) {
				if (update.getDate().getTime()<sinceDate.getTime()) {
					commit = update.getHeadCommitHash();
				}
			}
			return commit;
		}
		
	};
	
	public SinceChangesLink(String id, IModel<PullRequest> requestModel, Date sinceDate) {
		super(id, RequestChangesPage.class);
		this.requestModel = requestModel;
		this.sinceDate = sinceDate;
	}

	@Override
	public PageParameters getPageParameters() {
		PullRequest request = requestModel.getObject();
		RequestChangesPage.State state = new RequestChangesPage.State();
		state.oldCommit = oldCommitModel.getObject();
		state.newCommit = request.getHeadCommitHash();
		return RequestChangesPage.paramsOf(request, state);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PullRequestChanged) {
			PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
			IPartialPageRequestHandler partialPageRequestHandler = pullRequestChanged.getPartialPageRequestHandler();
			partialPageRequestHandler.add(this);
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupPlaceholderTag(true);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!oldCommitModel.getObject().equals(requestModel.getObject().getHeadCommitHash()));
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		oldCommitModel.detach();
		
		super.onDetach();
	}

}
