package io.onedev.server.web.component.issue.title;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.ReferenceTransformer;

@SuppressWarnings("serial")
public abstract class IssueTitlePanel extends Panel {

	public IssueTitlePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		String label;
		
		Project currentProject = getCurrentProject();
		Issue issue = getIssue();
		if (currentProject == null)
			label = issue.getProject() + "#" + issue.getNumber();
		else if (currentProject.equals(issue.getProject()))
			label = "#" + issue.getNumber();
		else 
			label = issue.getProject() + "#" + issue.getNumber();
		
		WebMarkupContainer numberLink;
		if (getCursor() != null) {
			add(numberLink = new ActionablePageLink("number", 
					IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue)) {

				@Override
				public IModel<?> getBody() {
					return Model.of(label);
				}

				@Override
				protected void doBeforeNav(AjaxRequestTarget target) {
					WebSession.get().setIssueCursor(getCursor());
					String redirectUrlAfterDelete = RequestCycle.get().urlFor(
							getPage().getClass(), getPage().getPageParameters()).toString();
					WebSession.get().setRedirectUrlAfterDelete(Issue.class, redirectUrlAfterDelete);
				}
				
			});
		} else {
			add(numberLink = new BookmarkablePageLink<Void>("number", 
					IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue)) {
				
				@Override
				public IModel<?> getBody() {
					return Model.of(label);
				}
				
			});
		}
		
		String url = RequestCycle.get().urlFor(IssueActivitiesPage.class, 
				IssueActivitiesPage.paramsOf(issue)).toString();

		String transformed = Emojis.getInstance().apply(new ReferenceTransformer(issue.getProject(), url).apply(issue.getTitle()));
		add(new Label("title", transformed) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				String script = String.format(""
						+ "$('#%s a:not(.embedded-reference)').click(function(e) {\n"
						+ "  if (!e.ctrlKey && !e.metaKey) {"
						+ "    if (%b)\n"
						+ "      $('#%s').click();\n"
						+ "    else\n"
						+ "      window.location.href=$(this).attr('href');"
						+ "    return false;\n"
						+ "  }\n"
						+ "});", 
						getMarkupId(), getCursor()!=null, numberLink.getMarkupId());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		}.setEscapeModelStrings(false).setOutputMarkupId(true));
		
		add(new WebMarkupContainer("confidential") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getIssue().isConfidential());
			}
			
		});
	}

	protected abstract Issue getIssue();
	
	@Nullable
	protected abstract Project getCurrentProject();
	
	@Nullable
	protected abstract Cursor getCursor();
	
}
