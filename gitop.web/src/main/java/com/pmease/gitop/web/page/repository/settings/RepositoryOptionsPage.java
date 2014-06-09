package com.pmease.gitop.web.page.repository.settings;

import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Git;
import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.common.quantity.Data;
import com.pmease.gitop.web.common.wicket.form.BaseForm;
import com.pmease.gitop.web.common.wicket.form.checkbox.CheckBoxElement;
import com.pmease.gitop.web.common.wicket.form.select.DropDownChoiceElement;
import com.pmease.gitop.web.common.wicket.form.textfield.TextFieldElement;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.home.AccountHomePage;

@SuppressWarnings("serial")
public class RepositoryOptionsPage extends AbstractRepositorySettingPage {

	public RepositoryOptionsPage(PageParameters params) {
		super(params);
	}

	private String repositoryName;
	private String defaultBranchName;
	
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
		
		Form<?> form = new BaseForm<Void>("form");
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
						
						RepositoryManager pm = Gitop.getInstance(RepositoryManager.class);
						if (pm.findBy(repository.getOwner(), name) != null) {
							validatable.error(new ValidationError().setMessage("Repository name is already exist"));
						}
					}
				}));
		form.add(new TextFieldElement<String>("description", "Description", 
				new PropertyModel<String>(repositoryModel, "description"))
				.add(new PropertyValidator<String>())
				.setRequired(false));
		
		form.add(new CheckBoxElement("forkable", "Allow Forks",
				new PropertyModel<Boolean>(repositoryModel, "forkable"),
				Model.of("Enable/Disable whether this repository can be forked by others")));
		
		// Default branch is recorded in HEAD ref of the repository, since no any branches exist in 
		// repository when it is created, it might be more appropriate to assign default branch directly 
		// via branches page.
		IModel<List<? extends String>> branchesModel = new AbstractReadOnlyModel<List<? extends String>>() {

			@Override
			public List<String> getObject() {
				Repository repository = getRepository();
				Git git = repository.git();
				if (git.hasCommits()) {
					return Lists.newArrayList(git.listBranches());
				} else {
					return Collections.emptyList();
				}
			}
		};
		
		defaultBranchName = GitUtils.getDefaultBranch(getRepository().git());
		form.add(new DropDownChoiceElement<String>(
				"defaultBranch", 
				"Default Branch",
				new PropertyModel<String>(this, "defaultBranchName"),
				branchesModel)
				.setHelp("Set default branch for browsing. By default, it is \"master\"." ));
		
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
//				repository.setDefaultBranchName(defaultBranch);
				
				if (!Strings.isNullOrEmpty(defaultBranchName)) {
					repository.git().updateDefaultBranch(defaultBranchName);
				}
				
				Gitop.getInstance(RepositoryManager.class).save(repository);
				
				if (nameChanged) {
					setResponsePage(RepositoryOptionsPage.class, PageSpec.forRepository(repository));
				} else {
					form.success("Repository " + repository + " has been updated.");
					target.add(form);
				}
			}
		});
		
		add(new AjaxLink<Void>("deletelink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Gitop.getInstance(RepositoryManager.class).delete(getRepository());
				setResponsePage(AccountHomePage.class, PageSpec.forUser(getAccount()));
			}
		}.add(new ConfirmBehavior("<p>Are you sure you want to delete repository: " + getRepository() 
				+ "?</p><b>NOTE:</b> Once you delete this repository, there is no going back")));
	}

	@Override
	protected String getPageTitle() {
		return "Options - " + getRepository();
	}
}
