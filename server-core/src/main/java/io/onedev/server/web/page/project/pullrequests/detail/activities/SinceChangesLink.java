package io.onedev.server.web.page.project.pullrequests.detail.activities;

import java.util.Date;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
public class SinceChangesLink extends ViewStateAwarePageLink<Void> {

	private final IModel<PullRequest> requestModel;
	
	private final Date sinceDate;
	
	private final IModel<String> oldCommitModel = new LoadableDetachableModel<String>() {

		@Override
		protected String load() {
			PullRequest request = requestModel.getObject();
			String commit = request.getBaseCommitHash();
			for (PullRequestUpdate update: request.getSortedUpdates()) {
				if (update.getDate().before(sinceDate))
					commit = update.getHeadCommitHash();
			}
			return commit;
		}
		
	};
	
	public SinceChangesLink(String id, IModel<PullRequest> requestModel, Date sinceDate) {
		super(id, PullRequestChangesPage.class);
		this.requestModel = requestModel;
		this.sinceDate = sinceDate;
	}

	@Override
	public PageParameters getPageParameters() {
		PullRequest request = requestModel.getObject();
		PullRequestChangesPage.State state = new PullRequestChangesPage.State();
		state.oldCommit = oldCommitModel.getObject();
		state.newCommit = request.getHeadCommitHash();
		PullRequestDetailPage page = (PullRequestDetailPage) getPage();
		return PullRequestChangesPage.paramsOf(request, page.getPosition(), state);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PageDataChanged) {
			PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
			IPartialPageRequestHandler partialPageRequestHandler = pageDataChanged.getHandler();
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
