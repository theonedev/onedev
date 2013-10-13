package com.pmease.gitop.web.page.account.setting.permission;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.ImmutableList;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.web.common.component.messenger.Messenger;
import com.pmease.gitop.web.common.form.FeedbackPanel;

@SuppressWarnings("serial")
public class TeamEditor extends Panel {

	public TeamEditor(String id, IModel<Team> model) {
		super(id, model);
	}

	private Team getTeam() {
		return (Team) getDefaultModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(createInfoForm());
	}
	
	private Form<?> createInfoForm() {
		@SuppressWarnings("unchecked")
		IModel<Team> teamModel = (IModel<Team>) getDefaultModel();
		Form<Team> infoForm = new Form<Team>("infoForm", teamModel);
		add(infoForm);
		infoForm.add(new FeedbackPanel("feedback"));
		infoForm.add(new TextField<String>("name", new PropertyModel<String>(teamModel, "name")).setRequired(true));
		WebMarkupContainer permissionContainer = 
				new WebMarkupContainer("permissionContainer");
		infoForm.add(permissionContainer);
		permissionContainer.add(
				new ListView<GeneralOperation>("permissions", 
						ImmutableList.<GeneralOperation>of(
							GeneralOperation.READ,
							GeneralOperation.WRITE,
							GeneralOperation.ADMIN)) {

				@Override
				protected void populateItem(ListItem<GeneralOperation> item) {
					GeneralOperation permission = item.getModelObject();
					AjaxLink<?> link = new PermissionLink("link", permission);
					link.add(new Label("name", permission.toString()));
					item.add(link);
				}
			
		});
		
		infoForm.add(new AjaxButton("submit", infoForm) {
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Team team = getTeam();
				boolean isNew = team.isNew();
				Gitop.getInstance(TeamManager.class).save(team);
				Messenger
					.success(String.format("Team has been %s successfully.", 
											isNew ? "created" : "updated"))
					.run(target);
			}
		});
		
		return infoForm;
	}
	
	class PermissionLink extends AjaxLink<Void> {
		final GeneralOperation permssion;
		
		PermissionLink(String id, final GeneralOperation permission) {
			super(id);
			this.permssion = permission;
			
			add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					return getTeam().getAuthorizedOperation() == permission ?
							"active" : "";
				}
			}));
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			Team team = getTeam();
			if (team.getAuthorizedOperation() == permssion) {
				return;
			}

			team.setAuthorizedOperation(permssion);
			Gitop.getInstance(TeamManager.class).save(team);
			
			target.add(TeamEditor.this.get("infoForm"));
		}
	}
}
