package io.onedev.server.web.component.issue.activities.activity;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.util.ReferenceTransformer;

@SuppressWarnings("serial")
public class IssuePullRequestPanel extends Panel {

	private final Long requestId;
	
	public IssuePullRequestPanel(String id, Long requestId) {
		super(id);
		this.requestId = requestId;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		PullRequest request = OneDev.getInstance(PullRequestManager.class).load(requestId);
		String url = RequestCycle.get().urlFor(PullRequestActivitiesPage.class, 
				PullRequestActivitiesPage.paramsOf(request, null)).toString();
		
		String label = "#" + request.getNumber();
		add(new Label("number", "<a href='" + url + "'>" + label + "</a>").setEscapeModelStrings(false));
		ReferenceTransformer transformer = new ReferenceTransformer(request.getTargetProject(), url);
		add(new Label("title", transformer.apply(request.getTitle())).setEscapeModelStrings(false));
	}
	
}
