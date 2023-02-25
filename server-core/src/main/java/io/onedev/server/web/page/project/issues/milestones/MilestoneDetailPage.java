package io.onedev.server.web.page.project.issues.milestones;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.milestone.MilestoneDateLabel;
import io.onedev.server.web.component.milestone.MilestoneStatusLabel;
import io.onedev.server.web.component.milestone.actions.MilestoneActionsPanel;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public abstract class MilestoneDetailPage extends ProjectPage {

	public static final String PARAM_MILESTONE = "milestone";
	
	protected final IModel<Milestone> milestoneModel;
	
	public MilestoneDetailPage(PageParameters params) {
		super(params);
		
		String idString = params.get(PARAM_MILESTONE).toString();
		if (StringUtils.isBlank(idString))
			throw new RestartResponseException(MilestoneListPage.class, MilestoneListPage.paramsOf(getProject(), false, null));
		
		Long milestoneId = Long.valueOf(idString);
		milestoneModel = new LoadableDetachableModel<Milestone>() {

			@Override
			protected Milestone load() {
				return OneDev.getInstance(MilestoneManager.class).load(milestoneId);
			}
			
		};
		
	}

	protected Milestone getMilestone() {
		return milestoneModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("name", getMilestone().getName()));
		add(new MilestoneStatusLabel("status", milestoneModel));
		add(new MilestoneDateLabel("dates", milestoneModel));
		add(new MilestoneActionsPanel("actions", milestoneModel) {

			@Override
			protected void onDeleted(AjaxRequestTarget target) {
				String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Milestone.class);
				if (redirectUrlAfterDelete != null)
					throw new RedirectToUrlException(redirectUrlAfterDelete);
				else
					setResponsePage(MilestoneListPage.class, MilestoneListPage.paramsOf(getProject()));
			}

			@Override
			protected void onUpdated(AjaxRequestTarget target) {
				setResponsePage(getPageClass(), getPageParameters());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManageIssues(getProject()) && getProject().equals(getMilestone().getProject()));
			}

		});
		add(new BookmarkablePageLink<Void>("create", NewMilestonePage.class, 
				NewMilestonePage.paramsOf(getProject())));
		add(new WebMarkupContainer("inherited") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getProject().equals(getMilestone().getProject()));
			}
			
		});
		add(new MultilineLabel("description", getMilestone().getDescription()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getMilestone().getDescription() != null);
			}
			
		});

		List<Tab> tabs = new ArrayList<>();
		tabs.add(new MilestoneTab("Issues", MilestoneIssuesPage.class));
		tabs.add(new MilestoneTab("Burndown", MilestoneBurndownPage.class));
		
		add(new Tabbable("milestoneTabs", tabs).setOutputMarkupId(true));
	}
	
	@Override
	protected void onDetach() {
		milestoneModel.detach();
		super.onDetach();
	}
	
	public static PageParameters paramsOf(Project project, Milestone milestone) {
		PageParameters params = paramsOf(project);
		params.add(PARAM_MILESTONE, milestone.getId());
		return params;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MilestoneCssResourceReference()));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("milestones", MilestoneListPage.class, 
				MilestoneListPage.paramsOf(getProject())));
		fragment.add(new Label("milestoneName", getMilestone().getName()));
		return fragment;
	}
	
	@Override
	protected String getPageTitle() {
		return "Milestone " +  getMilestone().getName() + " - " + getProject().getPath();
	}
	
	private class MilestoneTab extends PageTab {

		public MilestoneTab(String title, Class<? extends Page> pageClass) {
			super(Model.of(title), pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			return new PageTabHead(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					return new ViewStateAwarePageLink<Void>(linkId, pageClass, paramsOf(getProject(), getMilestone()));
				}
				
			};
		}
		
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, MilestoneListPage.class, MilestoneListPage.paramsOf(project, false, null));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}
