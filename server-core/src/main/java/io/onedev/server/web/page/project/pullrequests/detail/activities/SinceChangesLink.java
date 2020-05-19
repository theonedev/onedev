package io.onedev.server.web.page.project.pullrequests.detail.activities;

import java.util.Collection;
import java.util.Date;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;

@SuppressWarnings("serial")
public class SinceChangesLink extends ViewStateAwarePageLink<Void> {

	private final IModel<PullRequest> requestModel;
	
	private final Date sinceDate;
	
	private final IModel<String> oldCommitModel = new LoadableDetachableModel<String>() {

		@Override
		protected String load() {
			PullRequest request = getPullRequest();
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
		PullRequest request = getPullRequest();
		PullRequestChangesPage.State state = new PullRequestChangesPage.State();
		state.oldCommitHash = oldCommitModel.getObject();
		state.newCommitHash = request.getLatestUpdate().getHeadCommitHash();
		return PullRequestChangesPage.paramsOf(request, state);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WebSocketObserver() {

			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(PullRequest.getWebSocketObservable(getPullRequest().getId()));
			}

			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(component);
			}
			
		});
		
		setOutputMarkupPlaceholderTag(true);
	}
	
	private PullRequest getPullRequest() {
		return requestModel.getObject();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!oldCommitModel.getObject().equals(getPullRequest().getLatestUpdate().getHeadCommitHash()));
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		oldCommitModel.detach();
		
		super.onDetach();
	}

}
