package io.onedev.server.web.page.project.issues.milestones;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
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
				Milestone milestoneWithSameName = milestoneManager.find(getProject(), milestone.getName());
				if (milestoneWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another milestone in the project");
				} 
				if (editor.isValid()){
					milestone.setProject(getProject());
					milestoneManager.save(milestone);
					Session.get().success("New milestone created");
					setResponsePage(MilestoneDetailPage.class, MilestoneDetailPage.paramsOf(milestone, null));
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

}
