package io.onedev.server.web.component.pullrequest.referencedfrom;

import io.onedev.server.OneDev;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.pullrequest.RequestStatusBadge;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;

import static org.unbescape.html.HtmlEscape.escapeHtml5;

public class ReferencedFromPullRequestPanel extends GenericPanel<PullRequest> {

	public ReferencedFromPullRequestPanel(String id, Long requestId) {
		super(id, new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return OneDev.getInstance(PullRequestService.class).load(requestId);
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new RequestStatusBadge("state", getModel()));
		
		Project project = ((ProjectPage)getPage()).getProject();
		
		PullRequest request = getModelObject();

		if (SecurityUtils.canReadCode(request.getTargetProject())) {
			String url = RequestCycle.get().urlFor(PullRequestActivitiesPage.class, 
					PullRequestActivitiesPage.paramsOf(request)).toString();
			String summary = String.format("<a href='%s'>%s</a>", 
					url, escapeHtml5(request.getSummary(project)));
			add(new Label("summary", summary).setEscapeModelStrings(false));
		} else {
			add(new Label("summary", request.getSummary(project)));
		}
	}

}
