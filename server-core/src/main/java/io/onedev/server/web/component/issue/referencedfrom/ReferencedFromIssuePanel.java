package io.onedev.server.web.component.issue.referencedfrom;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.asset.titleandstatus.TitleAndStatusCssResourceReference;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.util.ReferenceTransformer;

@SuppressWarnings("serial")
public class ReferencedFromIssuePanel extends GenericPanel<Issue> {

	public ReferencedFromIssuePanel(String id, Long issueId) {
		super(id, new LoadableDetachableModel<Issue>() {

			@Override
			protected Issue load() {
				return OneDev.getInstance(IssueManager.class).load(issueId);
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new IssueStateBadge("state", getModel()));
		
		Project project = ((ProjectPage)getPage()).getProject();
		
		Issue issue = getModelObject();
		
		if (SecurityUtils.canAccess(issue.getProject())) {
			String url = RequestCycle.get().urlFor(IssueActivitiesPage.class, 
					IssueActivitiesPage.paramsOf(issue)).toString();
			ReferenceTransformer transformer = new ReferenceTransformer(issue.getProject(), url);
			String transformed = transformer.apply(issue.getTitle());
			String title;
			if (issue.getProject().equals(project)) { 
				title = String.format("<a href='%s'>#%d</a> %s", url, issue.getNumber(), transformed);
			} else { 
				title = String.format("<a href='%s'>%s#%d</a> %s", 
						url, issue.getProject().getName(), issue.getNumber(), transformed);
			}
			add(new Label("title", title).setEscapeModelStrings(false));
		} else {
			ReferenceTransformer transformer = new ReferenceTransformer(issue.getProject(), null);
			String transformed = transformer.apply(issue.getTitle());
			String title;
			if (issue.getProject().equals(project)) 
				title = "#" + issue.getNumber() + " " + transformed;
			else 
				title = issue.getProject().getName() + "#" + issue.getNumber() + " " + transformed;
			add(new Label("title", title).setEscapeModelStrings(false));
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new TitleAndStatusCssResourceReference()));
	}

}
