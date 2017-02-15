package com.gitplex.server.web.component.createtag;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.security.SecurityUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class CreateTagPanel extends Panel {

	private final IModel<Depot> depotModel;
	
	private final String revision;
	
	private String tagName;
	
	private String tagMessage;
	
	public CreateTagPanel(String id, IModel<Depot> depotModel, String revision) {
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
				return tagName;
			}

			@Override
			public void setObject(String object) {
				tagName = object;
			}
			
		}));
		nameInput.setOutputMarkupId(true);
		
		form.add(new TextArea<String>("message", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return tagMessage;
			}

			@Override
			public void setObject(String object) {
				tagMessage = object;
			}
			
		}));
		form.add(new AjaxButton("create") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (tagName == null) {
					form.error("Tag name is required.");
					target.focusComponent(nameInput);
					target.add(form);
				} else if (!Repository.isValidRefName(Constants.R_TAGS + tagName)) {
					form.error("Tag name is not valid.");
					target.focusComponent(nameInput);
					target.add(form);
				} else if (depotModel.getObject().getObjectId(GitUtils.tag2ref(tagName), false) != null) {
					form.error("Tag '" + tagName + "' already exists, please choose a different name.");
					target.focusComponent(nameInput);
					target.add(form);
				} else {
					Depot depot = depotModel.getObject();
					ObjectId commitId = depot.getRevCommit(revision);
					Account user = Preconditions.checkNotNull(SecurityUtils.getAccount());
					GateCheckResult checkResult = depot.getGateKeeper().checkPush(user, 
							depot, Constants.R_TAGS + tagName, ObjectId.zeroId(), commitId);
					if (!checkResult.isPassedOrIgnored()) {
						form.error(Joiner.on(", ").join(checkResult.getReasons()));
						target.focusComponent(nameInput);
						target.add(form);
					} else {
						depot.tag(tagName, revision, user.asPerson(), tagMessage);
						onCreate(target, tagName);
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
	
	protected abstract void onCreate(AjaxRequestTarget target, String tag);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	@Override
	protected void onDetach() {
		depotModel.detach();
		
		super.onDetach();
	}

}
