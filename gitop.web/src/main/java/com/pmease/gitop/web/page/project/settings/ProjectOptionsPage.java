package com.pmease.gitop.web.page.project.settings;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.web.common.component.messenger.Messenger;
import com.pmease.gitop.web.common.component.vex.AjaxConfirmLink;
import com.pmease.gitop.web.common.form.FeedbackPanel;
import com.pmease.gitop.web.common.form.flatcheckbox.FlatCheckBoxElement;
import com.pmease.gitop.web.common.form.select.DropDownChoiceElement;
import com.pmease.gitop.web.common.form.textfield.TextFieldElement;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.home.AccountHomePage;

@SuppressWarnings("serial")
public class ProjectOptionsPage extends AbstractProjectSettingPage {

	public ProjectOptionsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.OPTIONS;
	}
	
	private boolean forkable = true;
	private String defaultBranch;
	private String projectName;
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		projectName = getProject().getName();
		
		Form<?> form = new Form<Void>("form");
		add(form);
		form.add(new FeedbackPanel("feedback", form));
		form.add(new TextFieldElement<String>("name", "Project Name", 
				new PropertyModel<String>(this, "projectName"))
				.add(new PropertyValidator<String>())
				.add(new IValidator<String>() {

					@Override
					public void validate(IValidatable<String> validatable) {
						Project project = getProject();
						
						String name = validatable.getValue();
						if (Objects.equal(name, project.getName())) {
							return;
						}
						
						ProjectManager pm = Gitop.getInstance(ProjectManager.class);
						if (pm.find(project.getOwner(), name) != null) {
							validatable.error(new ValidationError().setMessage("Project name is already exist"));
						}
					}
				}));
		form.add(new TextFieldElement<String>("description", "Description", 
				new PropertyModel<String>(projectModel, "description"))
				.add(new PropertyValidator<String>())
				.setRequired(false));
		
		form.add(new FlatCheckBoxElement("forkable", "Allow Forks",
				new PropertyModel<Boolean>(this, "forkable"),
				Model.of("Enable/Disable whether this repository can be forked by others")));
		
		IModel<List<? extends String>> branchesModel = new AbstractReadOnlyModel<List<? extends String>>() {

			@Override
			public List<String> getObject() {
				return ImmutableList.<String>of();
			}
		};
		
		form.add(new DropDownChoiceElement<String>(
				"defaultBranch", 
				"Default Branch",
				new PropertyModel<String>(this, "defaultBranch"),
				branchesModel));
		
		form.add(new AjaxButton("submit", form) {
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Project project = getProject();
				boolean nameChanged = !Objects.equal(project.getName(), projectName);
				if (nameChanged) {
					project.setName(projectName);
				}
				Gitop.getInstance(ProjectManager.class).save(project);
				
				if (nameChanged) {
					setResponsePage(ProjectOptionsPage.class, PageSpec.forProject(project));
				} else {
					target.add(form);
					Messenger.success("Project " + project + " has been updated.")
							 .run(target);
				}
			}
		});
		
		add(new AjaxConfirmLink<Void>("deletelink", Model.of(
				"<p>Are you sure you want to delete project: " 
						+ getProject() 
						+ "?</p><b>NOTE:</b> Once you delete this project, there is no going back")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Gitop.getInstance(ProjectManager.class).delete(getProject());
				setResponsePage(AccountHomePage.class, PageSpec.forUser(getAccount()));
			}
		});
	}

	public boolean isForkable() {
		return forkable;
	}

	public void setForkable(boolean forkable) {
		this.forkable = forkable;
	}

	public String getDefaultBranch() {
		return defaultBranch;
	}

	public void setDefaultBranch(String defaultBranch) {
		this.defaultBranch = defaultBranch;
	}
}
