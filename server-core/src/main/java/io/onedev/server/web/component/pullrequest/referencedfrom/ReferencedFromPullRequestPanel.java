package io.onedev.server.web.component.pullrequest.referencedfrom;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.asset.titleandstatus.TitleAndStatusCssResourceReference;
import io.onedev.server.web.component.pullrequest.RequestStatusBadge;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.util.ReferenceTransformer;

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
		
		add(new RequestStatusBadge("state", getModel()));
		
		Project project = ((ProjectPage)getPage()).getProject();
		
		PullRequest request = getModelObject();

		if (SecurityUtils.canReadCode(request.getTargetProject())) {
			String url = RequestCycle.get().urlFor(PullRequestActivitiesPage.class, 
					PullRequestActivitiesPage.paramsOf(request)).toString();
			ReferenceTransformer transformer = new ReferenceTransformer(request.getTargetProject(), url);
			String transformed = transformer.apply(request.getTitle());
			String title;
			if (request.getTargetProject().equals(project)) { 
				title = String.format("<a href='%s'>#%d</a> %s", url, request.getNumber(), transformed);
			} else { 
				title = String.format("<a href='%s'>%s#%d</a> %s", 
						url, request.getTargetProject().getName(), request.getNumber(), transformed);
			}
			add(new Label("title", title).setEscapeModelStrings(false));
		} else {
			ReferenceTransformer transformer = new ReferenceTransformer(request.getTargetProject(), null);
			String transformed = transformer.apply(request.getTitle());
			String title;
			if (request.getTargetProject().equals(project)) 
				title = "#" + request.getNumber() + " " + transformed;
			else 
				title = request.getTargetProject().getName() + "#" + request.getNumber() + " " + transformed;
			add(new Label("title", title).setEscapeModelStrings(false));
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new TitleAndStatusCssResourceReference()));
	}

}
