package com.pmease.gitplex.web.component.addbranch;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.model.Repository;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class AddBranchPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String revision;
	
	private String branchName;
	
	public AddBranchPanel(String id, IModel<Repository> repoModel, String revision) {
		super(id);
		this.repoModel = repoModel;
		this.revision = revision;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		form.add(new NotificationPanel("feedback", form));
		form.add(new TextField<String>("name", new IModel<String>() {

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
			
		}).setOutputMarkupId(true));

		form.add(new AjaxButton("create") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (branchName == null) {
					form.error("Branch name is required.");
					target.focusComponent(form.get("name"));
					target.add(form);
				} else {
					String branchRef = GitUtils.branch2ref(branchName);
					Repository repo = repoModel.getObject();
					if (repo.getObjectId(branchRef, false) != null) {
						form.error("Branch '" + branchName + "' already exists, please choose a different name.");
						target.add(form);
					} else {
						repo.git().createBranch(branchName, repoModel.getObject().getRevCommit(revision).name());
						onCreate(target, branchName);
					}
				}
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

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
		repoModel.detach();
		
		super.onDetach();
	}

}
