package io.onedev.server.web.page.project.issues.milestones;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;

@SuppressWarnings("serial")
public class NewMilestonePage extends ProjectPage {

	public NewMilestonePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Milestone milestone = new Milestone();
		
		BeanEditor editor = BeanContext.edit("editor", milestone);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
				Milestone milestoneWithSameName = milestoneManager.findInHierarchy(getProject(), milestone.getName());
				if (milestoneWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another milestone in the project hierarchy");
				} 
				if (editor.isValid()){
					milestone.setProject(getProject());
					milestoneManager.save(milestone);
					Session.get().success("New milestone created");
					setResponsePage(MilestoneIssuesPage.class, MilestoneIssuesPage.paramsOf(getProject(), milestone, null));
				}
				
			}
			
		};
		form.add(editor);
		add(form);
	}

	public static PageParameters paramsOf(Project project) {
		return ProjectPage.paramsOf(project);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManageIssues(getProject());
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Create Milestone");
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, MilestoneListPage.class, MilestoneListPage.paramsOf(project, false, null));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}
