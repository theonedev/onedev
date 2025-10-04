package io.onedev.server.web.component.issue.iteration;

import io.onedev.server.OneDev;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.editable.InplacePropertyEditLink;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.issues.iteration.IterationIssuesPage;
import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import java.io.Serializable;

import static java.util.stream.Collectors.toList;

public abstract class IterationCrumbPanel extends Panel {

	public IterationCrumbPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var editLink = new InplacePropertyEditLink("edit") {

			protected Component newContent(String id, FloatingPanel dropdown) {
				if (!SecurityUtils.canScheduleIssues(getIssue().getProject())) {
					return new Label(id, "<div class='px-3 py-2'><i>No permission to schedule issues</i></div>")
							.setEscapeModelStrings(false);
				} else {
					return super.newContent(id, dropdown);
				}
			}

			@Override
			protected Serializable getBean() {
				var bean = new IterationsBean();
				bean.setIterationNames(getIssue().getSchedules().stream()
						.map(it -> it.getIteration().getName()).collect(toList()));
				return bean;
			}

			@Override
			protected String getPropertyName() {
				return "iterationNames";
			}

			@Override
			protected void onUpdated(IPartialPageRequestHandler handler, Serializable bean, String propertyName) {
				var iterationsBean = (IterationsBean) bean;
				var issue = getIssue();
				var iterations = iterationsBean.getIterationNames().stream()
						.map(it -> issue.getProject().getHierarchyIteration(it))
						.collect(toList());
				var user = SecurityUtils.getUser();
				OneDev.getInstance(IssueChangeService.class).changeIterations(user, issue, iterations);
				((BasePage) getPage()).notifyObservablesChange(handler, issue.getChangeObservables(true));
			}

			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}
			
		};
		add(editLink);
	
		var iterationsView = new RepeatingView("iterations");
		for (var schedule: getIssue().getSchedules()) {
			var child = new BookmarkablePageLink<Void>(iterationsView.newChildId(),
					IterationIssuesPage.class,
					IterationIssuesPage.paramsOf(getIssue().getProject(), schedule.getIteration()));
			iterationsView.add(child);
			child.add(new Label("name", schedule.getIteration().getName()));
		}
		editLink.add(iterationsView);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!getIssue().getSchedules().isEmpty());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IterationCrumbCssResourceReference()));
	}

	protected abstract Issue getIssue();
}
