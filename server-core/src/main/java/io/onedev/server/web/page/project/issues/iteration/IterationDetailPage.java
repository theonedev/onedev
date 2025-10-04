package io.onedev.server.web.page.project.issues.iteration;

import io.onedev.server.OneDev;
import io.onedev.server.service.IterationService;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.iteration.IterationDateLabel;
import io.onedev.server.web.component.iteration.IterationStatusLabel;
import io.onedev.server.web.component.iteration.actions.IterationActionsPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

public abstract class IterationDetailPage extends ProjectPage {

	public static final String PARAM_ITERATION = "iteration";
	
	protected final IModel<Iteration> iterationModel;
	
	public IterationDetailPage(PageParameters params) {
		super(params);
		
		String idString = params.get(PARAM_ITERATION).toString();
		if (StringUtils.isBlank(idString))
			throw new RestartResponseException(IterationListPage.class, IterationListPage.paramsOf(getProject(), false, null));
		
		Long iterationId = Long.valueOf(idString);
		iterationModel = new LoadableDetachableModel<Iteration>() {

			@Override
			protected Iteration load() {
				return OneDev.getInstance(IterationService.class).load(iterationId);
			}
			
		};
		
	}

	protected Iteration getIteration() {
		return iterationModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("name", getIteration().getName()));
		add(new IterationStatusLabel("status", iterationModel));
		add(new IterationDateLabel("dates", iterationModel));
		add(new IterationActionsPanel("actions", iterationModel) {

			@Override
			protected void onDeleted(AjaxRequestTarget target) {
				String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Iteration.class);
				if (redirectUrlAfterDelete != null)
					throw new RedirectToUrlException(redirectUrlAfterDelete);
				else
					setResponsePage(IterationListPage.class, IterationListPage.paramsOf(getProject()));
			}

			@Override
			protected void onUpdated(AjaxRequestTarget target) {
				setResponsePage(getPageClass(), getPageParameters());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManageIssues(getProject()) && getProject().equals(getIteration().getProject()));
			}

		});
		add(new BookmarkablePageLink<Void>("create", NewIterationPage.class, 
				NewIterationPage.paramsOf(getProject())));
		add(new WebMarkupContainer("inherited") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getProject().equals(getIteration().getProject()));
			}
			
		});
		add(new MultilineLabel("description", getIteration().getDescription()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getIteration().getDescription() != null);
			}
			
		});

		List<Tab> tabs = new ArrayList<>();
		var params = paramsOf(getProject(), getIteration());
		tabs.add(new PageTab(Model.of(_T("Issues")), IterationIssuesPage.class, params));
		tabs.add(new PageTab(Model.of(_T("Burndown")), IterationBurndownPage.class, params));
		
		add(new Tabbable("iterationTabs", tabs).setOutputMarkupId(true));
	}
	
	@Override
	protected void onDetach() {
		iterationModel.detach();
		super.onDetach();
	}
	
	public static PageParameters paramsOf(Project project, Iteration iteration) {
		PageParameters params = paramsOf(project);
		params.add(PARAM_ITERATION, iteration.getId());
		return params;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IterationCssResourceReference()));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("iterations", IterationListPage.class, 
				IterationListPage.paramsOf(getProject())));
		fragment.add(new Label("iterationName", getIteration().getName()));
		return fragment;
	}
	
	@Override
	protected String getPageTitle() {
		return _T("Iteration") + " " +  getIteration().getName() + " - " + getProject().getPath();
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, IterationListPage.class, IterationListPage.paramsOf(project, false, null));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project));
	}
	
}
