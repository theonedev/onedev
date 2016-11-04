package com.gitplex.server.web.component.createbranch;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.gitplex.commons.git.GitUtils;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.server.core.security.SecurityUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class CreateBranchPanel extends Panel {

	private final IModel<Depot> depotModel;
	
	private final String revision;
	
	private String branchName;
	
	public CreateBranchPanel(String id, IModel<Depot> depotModel, String revision) {
		super(id);
		this.depotModel = depotModel;
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
				} else if (depotModel.getObject().getObjectId(GitUtils.branch2ref(branchName), false) != null) {
					form.error("Branch '" + branchName + "' already exists, please choose a different name.");
					target.focusComponent(nameInput);
					target.add(form);
				} else {
					Depot depot = depotModel.getObject();
					ObjectId commitId = depot.getRevCommit(revision);
					Account user = Preconditions.checkNotNull(SecurityUtils.getAccount());
					GateCheckResult checkResult = depot.getGateKeeper().checkPush(user, 
							depot, Constants.R_HEADS + branchName, ObjectId.zeroId(), commitId);
					if (!checkResult.isPassedOrIgnored()) {
						form.error(Joiner.on(", ").join(checkResult.getReasons()));
						target.focusComponent(nameInput);
						target.add(form);
					} else {
						depot.createBranch(branchName, revision);
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
		depotModel.detach();
		
		super.onDetach();
	}

}
