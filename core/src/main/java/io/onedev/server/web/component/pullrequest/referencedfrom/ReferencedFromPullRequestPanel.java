package io.onedev.server.web.component.pullrequest.referencedfrom;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.pullrequest.RequestStatusLabel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

@SuppressWarnings("serial")
public class ReferencedFromPullRequestPanel extends GenericPanel<PullRequest> {

	public ReferencedFromPullRequestPanel(String id, Long requestId) {
		super(id, new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return OneDev.getInstance(PullRequestManager.class).load(requestId);
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new RequestStatusLabel("state", getModel()));
		
		Project project = ((ProjectPage)getPage()).getProject();
		
		PullRequest request = getModelObject();
		
		Link<Void> link = new BookmarkablePageLink<Void>("link", PullRequestActivitiesPage.class, 
				PullRequestActivitiesPage.paramsOf(request, null));
		add(link);
		if (request.getTargetProject().equals(project)) {
			link.add(new Label("label", "#" + request.getNumber() + " - " + request.getTitle()));
		} else {
			link.add(new Label("label", request.getTargetProject().getName() + "#" + request.getNumber() + " - " + request.getTitle()));
		}
	}

}
