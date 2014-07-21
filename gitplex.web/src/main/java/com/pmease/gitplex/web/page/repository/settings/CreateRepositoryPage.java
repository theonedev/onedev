package com.pmease.gitplex.web.page.repository.settings;

import java.util.List;

import com.pmease.gitplex.core.GitPlex;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.wicket.AttributeModifier;
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
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.common.wicket.form.select.DropDownChoiceElement;
import com.pmease.gitplex.web.common.wicket.form.textfield.TextFieldElement;
import com.pmease.gitplex.web.model.RepositoryModel;
import com.pmease.gitplex.web.page.layout.LayoutPage;
import com.pmease.gitplex.web.page.repository.source.RepositoryHomePage;

@SuppressWarnings("serial")
@RequiresUser
public class CreateRepositoryPage extends LayoutPage {

	private String owner;
	
	public CreateRepositoryPage(PageParameters params) {
		StringValue sv = params.get("user");
		if (!sv.isEmpty() && !sv.isNull()) {
			owner = sv.toString();
		}
	}
	
	@Override
	protected String getPageTitle() {
		return "Create a new repository";
	}

	@Override
	public boolean isPermitted() {
		return GitPlex.getInstance(UserManager.class).getCurrent() != null;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (Strings.isNullOrEmpty(owner)) {
			this.owner = GitPlex.getInstance(UserManager.class).getCurrent().getName();
		}
		
		final IModel<Repository> repositoryModel = new RepositoryModel(new Repository());
		Form<Repository> form = new Form<Repository>("form", repositoryModel);
		add(form);
		
		form.add(new FeedbackPanel("feedback", form));
		DropDownChoiceElement<?> e = new DropDownChoiceElement<String>("owner", "Repository Owner",
				new PropertyModel<String>(this, "owner"),
				new AbstractReadOnlyModel<List<? extends String>>() {

					@Override
					public List<String> getObject() {
						UserManager um = GitPlex.getInstance(UserManager.class);
						List<User> users = um.getManagableAccounts(um.getCurrent());
						List<String> names = Lists.newArrayList();
						for (User each : users) {
							names.add(each.getName());
						}
						
						return names;
					}
		});
		
		form.add(e.addFormComponentBehavior(AttributeModifier.replace("data-ays-ignore", "true")));
		
		form.add(new TextFieldElement<String>("name", "Repository Name", 
				new PropertyModel<String>(repositoryModel, "name"))
				.add(new PropertyValidator<String>())
				.add(new IValidator<String>() {

					@Override
					public void validate(IValidatable<String> validatable) {
						String name = validatable.getValue();
						User o = GitPlex.getInstance(UserManager.class).findByName(owner);
						
						for (Repository each : o.getRepositories()) {
							if (each.getName().equalsIgnoreCase(name)) {
								validatable.error(new ValidationError().setMessage("This repository already exists"));
								return;
							}
						}
					}
					
				}));
		
		form.add(new TextFieldElement<String>("description", "Description",
				new PropertyModel<String>(repositoryModel, "description"))
				.add(new PropertyValidator<String>())
				.setRequired(false));
		
		form.add(new AjaxButton("submit", form) {
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Repository repository = repositoryModel.getObject();
				User o = GitPlex.getInstance(UserManager.class).findByName(owner);
				Preconditions.checkNotNull(o);
				repository.setOwner(o);
				GitPlex.getInstance(RepositoryManager.class).save(repository);
				setResponsePage(RepositoryHomePage.class, RepositoryHomePage.paramsOf(repository));
			}
		});
	}
}
