package io.onedev.server.web.component.issue.activities.activity;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.pullrequest.RequestStatusLabel;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.util.ReferenceTransformer;

@SuppressWarnings("serial")
public class IssuePullRequestPanel extends Panel {

	private final Long requestId;
	
	private final IModel<PullRequest> requestModel = new LoadableDetachableModel<PullRequest>() {

		@Override
		protected PullRequest load() {
			return OneDev.getInstance(PullRequestManager.class).load(requestId);
		}
		
	};
	
	public IssuePullRequestPanel(String id, Long requestId) {
		super(id);
		this.requestId = requestId;
	}
	
	private PullRequest getPullRequest() {
		return requestModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new RequestStatusLabel("state", requestModel));
		
		PullRequest request = getPullRequest();
		
		Project project = request.getTargetProject();
		
		String url = RequestCycle.get().urlFor(PullRequestActivitiesPage.class, 
				PullRequestActivitiesPage.paramsOf(request, null)).toString();
		ReferenceTransformer transformer = new ReferenceTransformer(project, url);
		String transformed = transformer.apply(request.getTitle());
		String title = String.format("<a href='%s'>#%d</a> %s", url, request.getNumber(), transformed);
		add(new Label("title", title).setEscapeModelStrings(false));
	}
	
}
