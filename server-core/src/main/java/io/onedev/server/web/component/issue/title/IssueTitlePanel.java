package io.onedev.server.web.component.issue.title;

import static io.onedev.server.entityreference.ReferenceUtils.transformReferences;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.entityreference.LinkTransformer;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.util.Cursor;

public abstract class IssueTitlePanel extends Panel {

	public IssueTitlePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		Issue issue = getIssue();
		
		var label = "(" + issue.getReference().toString(getCurrentProject()) + ")";
		WebMarkupContainer numberLink;
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
		
		String url = RequestCycle.get().urlFor(IssueActivitiesPage.class, 
				IssueActivitiesPage.paramsOf(issue)).toString();

		var transformed = transformReferences(issue.getTitle(), issue.getProject(), new LinkTransformer(url));
		transformed = Emojis.getInstance().apply(transformed);
		add(new Label("title", transformed) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				String script = String.format(""
						+ "$('#%s a:not(.embedded-reference)').click(function(e) {\n"
						+ "  if (!e.ctrlKey && !e.metaKey) {"
						+ "      $('#%s').click();\n"
						+ "    return false;\n"
						+ "  }\n"
						+ "});", 
						getMarkupId(), numberLink.getMarkupId());
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
