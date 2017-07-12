package com.gitplex.server.web.component.createbranch;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

import com.gitplex.server.git.GitUtils;
import com.gitplex.server.model.Project;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class CreateBranchPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final String revision;
	
	private String branchName;
	
	public CreateBranchPanel(String id, IModel<Project> projectModel, String revision) {
		super(id);
		this.projectModel = projectModel;
		this.revision = revision;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		form.add(new NotificationPanel("feedback", form));
		
		final TextField<String> nameInput;
		form.add(nameInput = new TextField<String>("name", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return branchName;
			}

			@Override
			public void setObject(String object) {
				branchName = object;
			}
			
		}));
		nameInput.setOutputMarkupId(true);
		
		form.add(new AjaxButton("create") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (branchName == null) {
					form.error("Branch name is required.");
					target.focusComponent(nameInput);
					target.add(form);
				} else if (!Repository.isValidRefName(Constants.R_HEADS + branchName)) {
					form.error("Invalid branch name.");
					target.focusComponent(nameInput);
					target.add(form);
				} else if (projectModel.getObject().getObjectId(GitUtils.branch2ref(branchName), false) != null) {
					form.error("Branch '" + branchName + "' already exists, please choose a different name.");
					target.focusComponent(nameInput);
					target.add(form);
				} else {
					projectModel.getObject().createBranch(branchName, revision);
					onCreate(target, branchName);
				}
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		add(form);
	}
	
	protected abstract void onCreate(AjaxRequestTarget target, String branch);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	@Override
	protected void onDetach() {
		projectModel.detach();
		
		super.onDetach();
	}

}
