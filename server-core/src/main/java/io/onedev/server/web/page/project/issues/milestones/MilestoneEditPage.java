package io.onedev.server.web.page.project.issues.milestones;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.editablebean.MilestoneEditBean;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class MilestoneEditPage extends ProjectPage {

	private static final String PARAM_MILESTONE = "milestone";
	
	private final IModel<Milestone> milestoneModel;
	
	public MilestoneEditPage(PageParameters params) {
		super(params);
		
		Long milestoneId = params.get(PARAM_MILESTONE).toLong();
		milestoneModel = new LoadableDetachableModel<Milestone>() {

			@Override
			protected Milestone load() {
				return OneDev.getInstance(MilestoneManager.class).load(milestoneId);
			}
			
		};
	}

	private Milestone getMilestone() {
		return milestoneModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		var bean = new MilestoneEditBean();
		bean.readFrom(getMilestone());
		BeanEditor editor = BeanContext.edit("editor", bean);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				bean.writeTo(getMilestone());
				OneDev.getInstance(MilestoneManager.class).update(getMilestone());
				Session.get().success("Milestone saved");
				setResponsePage(MilestoneIssuesPage.class, 
						MilestoneIssuesPage.paramsOf(getMilestone().getProject(), getMilestone(), null));
				
			}
			
		};
		form.add(editor);
		add(form);
	}
	
	@Override
	protected void onDetach() {
		milestoneModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Milestone milestone) {
		PageParameters params = paramsOf(milestone.getProject());
		params.add(PARAM_MILESTONE, milestone.getId());
		return params;
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManageIssues(getProject());
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("milestones", MilestoneListPage.class, 
				MilestoneListPage.paramsOf(getProject())));
		Link<Void> link = new BookmarkablePageLink<Void>("milestone", MilestoneIssuesPage.class, 
				MilestoneIssuesPage.paramsOf(getMilestone().getProject(), getMilestone(), null));
		link.add(new Label("name", getMilestone().getName()));
		fragment.add(link);
		return fragment;
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, MilestoneListPage.class, MilestoneListPage.paramsOf(project, false, null));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}
