package io.onedev.server.web.page.project.pullrequests.requestdetail.activities;

import java.util.Date;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.pullrequests.requestdetail.RequestDetailPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.changes.RequestChangesPage;
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
		RequestDetailPage page = (RequestDetailPage) getPage();
		return RequestChangesPage.paramsOf(request, page.getPosition(), state);
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
