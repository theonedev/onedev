package com.pmease.gitplex.web.page.repository.admin;

import org.apache.commons.io.FileUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.google.common.base.Objects;
import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.permission.Permission;
import com.pmease.gitplex.web.common.quantity.Data;
import com.pmease.gitplex.web.common.wicket.form.checkbox.CheckBoxElement;
import com.pmease.gitplex.web.common.wicket.form.textfield.TextFieldElement;
import com.pmease.gitplex.web.page.account.AccountHomePage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class GeneralSettingPage extends RepositoryPage {

	public GeneralSettingPage(PageParameters params) {
		super(params);
	}

	private String repositoryName;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("location", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getRepository().git().repoDir().toString();
			}
			
		}));
		
		add(new Label("size", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				long size = FileUtils.sizeOf(getRepository().git().repoDir());
				return Data.formatBytes(size);
			}
			
		}));
		
		repositoryName = getRepository().getName();
		
		Form<?> form = new Form<Void>("form");
		add(form);
		form.add(new FeedbackPanel("feedback",
									   new ComponentFeedbackMessageFilter(form))
					.hideAfter(Duration.seconds(15)));

		form.add(new TextFieldElement<String>("name", "Repository Name", 
				new PropertyModel<String>(this, "repositoryName"))
				.add(new PropertyValidator<String>())
				.add(new IValidator<String>() {

					@Override
					public void validate(IValidatable<String> validatable) {
						Repository repository = getRepository();
						
						String name = validatable.getValue();
						if (Objects.equal(name, repository.getName())) {
							return;
						}
						
						RepositoryManager pm = GitPlex.getInstance(RepositoryManager.class);
						if (pm.findBy(repository.getOwner(), name) != null) {
							validatable.error(new ValidationError().setMessage("Repository name is already exist"));
						}
					}
				}));
		form.add(new TextFieldElement<String>("description", "Description", 
				new PropertyModel<String>(repoModel, "description"))
				.add(new PropertyValidator<String>())
				.setRequired(false));
		
		form.add(new CheckBoxElement("forkable", "Allow Forks",
				new PropertyModel<Boolean>(repoModel, "forkable"),
				Model.of("Enable/Disable whether this repository can be forked by others")));
		
		form.add(new AjaxButton("submit", form) {
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Repository repository = getRepository();
				boolean nameChanged = !Objects.equal(repository.getName(), repositoryName);
				if (nameChanged) {
					repository.setName(repositoryName);
				}
				
				GitPlex.getInstance(RepositoryManager.class).save(repository);
				
				if (nameChanged) {
					setResponsePage(GeneralSettingPage.class, GeneralSettingPage.paramsOf(repository));
				} else {
					form.success("Repository " + repository + " has been updated.");
					target.add(form);
				}
			}
		});
		
		add(new AjaxLink<Void>("deletelink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				GitPlex.getInstance(RepositoryManager.class).delete(getRepository());
				setResponsePage(AccountHomePage.class, AccountHomePage.paramsOf(getAccount()));
			}
		}.add(new ConfirmBehavior("<p>Are you sure you want to delete repository: " + getRepository() 
				+ "?</p><b>NOTE:</b> Once you delete this repository, there is no going back")));
	}

	@Override
	protected String getPageTitle() {
		return "General Setting - " + getRepository();
	}

	@Override
	protected boolean isPermitted() {
		return super.isPermitted() 
				&& SecurityUtils.getSubject().isPermitted(Permission.ofRepositoryAdmin(getRepository()));
	}

}
