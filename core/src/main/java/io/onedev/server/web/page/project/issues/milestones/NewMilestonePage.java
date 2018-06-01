package io.onedev.server.web.page.project.issues.milestones;

import java.util.Date;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.IssuesPage;

@SuppressWarnings("serial")
public class NewMilestonePage extends IssuesPage {

	public NewMilestonePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Milestone milestone = new Milestone();
		milestone.setUpdateDate(new Date());
		
		BeanEditor editor = BeanContext.editBean("editor", milestone);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
				Milestone milestoneWithSameName = milestoneManager.find(getProject(), milestone.getName());
				if (milestoneWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another milestone in the project");
				} 
				if (!editor.hasErrors(true)){
					milestone.setProject(getProject());
					milestoneManager.save(milestone);
					Session.get().success("New milestone created");
					setResponsePage(MilestoneListPage.class, MilestoneListPage.paramsOf(getProject(), false, null));
				}
				
			}
			
		};
		form.add(editor);
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MilestonesResourceReference()));
	}

	public static PageParameters paramsOf(Project project) {
		return ProjectPage.paramsOf(project);
	}
	
}
