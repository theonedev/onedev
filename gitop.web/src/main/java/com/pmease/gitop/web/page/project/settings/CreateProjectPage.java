package com.pmease.gitop.web.page.project.settings;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.common.form.FeedbackPanel;
import com.pmease.gitop.web.common.form.flatcheckbox.FlatCheckBoxElement;
import com.pmease.gitop.web.common.form.textfield.TextFieldElement;
import com.pmease.gitop.web.model.ProjectModel;
import com.pmease.gitop.web.page.AbstractLayoutPage;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.source.ProjectHomePage;

@SuppressWarnings("serial")
@RequiresUser
public class CreateProjectPage extends AbstractLayoutPage {

	@Override
	protected String getPageTitle() {
		return "Create a new project";
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		final IModel<Project> projectModel = new ProjectModel(new Project());
		Form<Project> form = new Form<Project>("form", projectModel);
		add(form);
		
		form.add(new FeedbackPanel("feedback"));
		form.add(new TextFieldElement<String>("name", "Project Name", 
				new PropertyModel<String>(projectModel, "name"))
				.add(new PropertyValidator<String>())
				.add(new IValidator<String>() {

					@Override
					public void validate(IValidatable<String> validatable) {
						String name = validatable.getValue();
						User owner = User.getCurrent();
						
						for (Project each : owner.getProjects()) {
							if (each.getName().equalsIgnoreCase(name)) {
								validatable.error(new ValidationError().setMessage("This project already exists"));
								return;
							}
						}
					}
					
				}));
		
		form.add(new TextFieldElement<String>("description", "Description",
				new PropertyModel<String>(projectModel, "description"))
				.add(new PropertyValidator<String>())
				.setRequired(false));
		
		form.add(new FlatCheckBoxElement("public", "Public Accessible", 
				new PropertyModel<Boolean>(projectModel, "publiclyAccessible"),
				Model.of("Anyone can browse and pull this repository")));
		
		form.add(new AjaxButton("submit", form) {
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Project project = projectModel.getObject();
				project.setOwner(User.getCurrent());
				Gitop.getInstance(ProjectManager.class).save(project);
				setResponsePage(ProjectHomePage.class, PageSpec.forProject(project));
			}
		});
	}
}
