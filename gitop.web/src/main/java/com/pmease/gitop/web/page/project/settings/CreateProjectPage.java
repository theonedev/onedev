package com.pmease.gitop.web.page.project.settings;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.GitopHelper;
import com.pmease.gitop.web.common.form.FeedbackPanel;
import com.pmease.gitop.web.common.form.select.DropDownChoiceElement;
import com.pmease.gitop.web.common.form.textfield.TextFieldElement;
import com.pmease.gitop.web.model.ProjectModel;
import com.pmease.gitop.web.page.AbstractLayoutPage;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.source.ProjectHomePage;

@SuppressWarnings("serial")
@RequiresUser
public class CreateProjectPage extends AbstractLayoutPage {

	private String owner;
	
	public CreateProjectPage(PageParameters params) {
		StringValue sv = params.get("user");
		if (!sv.isEmpty() && !sv.isNull()) {
			owner = sv.toString();
		}
	}
	
	@Override
	protected String getPageTitle() {
		return "Create a new project";
	}

	@Override
	public boolean isPermitted() {
		return Gitop.getInstance(UserManager.class).getCurrent() != null;
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		if (Strings.isNullOrEmpty(owner)) {
			this.owner = Gitop.getInstance(UserManager.class).getCurrent().getName();
		}
		
		final IModel<Project> projectModel = new ProjectModel(new Project());
		Form<Project> form = new Form<Project>("form", projectModel);
		add(form);
		
		form.add(new FeedbackPanel("feedback"));
		form.add(new DropDownChoiceElement<String>("owner", "Project Owner",
				new PropertyModel<String>(this, "owner"),
				new AbstractReadOnlyModel<List<? extends String>>() {

					@Override
					public List<String> getObject() {
						List<User> users = 
								GitopHelper.getInstance()
									.getManagableAccounts(Gitop.getInstance(UserManager.class).getCurrent());
						
						List<String> names = Lists.newArrayList();
						for (User each : users) {
							names.add(each.getName());
						}
						
						return names;
					}
		}));
		
		form.add(new TextFieldElement<String>("name", "Project Name", 
				new PropertyModel<String>(projectModel, "name"))
				.add(new PropertyValidator<String>())
				.add(new IValidator<String>() {

					@Override
					public void validate(IValidatable<String> validatable) {
						String name = validatable.getValue();
						User o = Gitop.getInstance(UserManager.class).find(owner);
						
						for (Project each : o.getProjects()) {
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
		
		form.add(new AjaxButton("submit", form) {
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Project project = projectModel.getObject();
				User o = Gitop.getInstance(UserManager.class).find(owner);
				Preconditions.checkNotNull(o);
				project.setOwner(o);
				Gitop.getInstance(ProjectManager.class).save(project);
				setResponsePage(ProjectHomePage.class, PageSpec.forProject(project));
			}
		});
	}
}
