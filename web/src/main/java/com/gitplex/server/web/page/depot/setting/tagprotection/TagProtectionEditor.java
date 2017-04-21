package com.gitplex.server.web.page.depot.setting.tagprotection;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.server.model.Depot;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.model.support.tagcreator.DepotAdministrators;
import com.gitplex.server.model.support.tagcreator.DepotWriters;
import com.gitplex.server.model.support.tagcreator.SpecifiedUser;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.page.depot.DepotPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class TagProtectionEditor extends Panel {

	private final TagProtection protection;
	
	public TagProtectionEditor(String id, TagProtection protection) {
		super(id);
		
		this.protection = protection;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		TeamlessTagProtection teamlessProtection = new TeamlessTagProtection();
		teamlessProtection.setNoDeletion(protection.isNoDeletion());
		teamlessProtection.setNoUpdate(protection.isNoUpdate());
		teamlessProtection.setTag(protection.getTag());
		if (protection.getTagCreator() instanceof DepotAdministrators) {
			teamlessProtection.setTeamlessTagCreator(new TeamlessDepotAdministrators());
		} else if (protection.getTagCreator() instanceof SpecifiedUser) {
			SpecifiedUser specifiedUser = (SpecifiedUser) protection.getTagCreator(); 
			TeamlessSpecifiedUser teamlessSpecifiedUser = new TeamlessSpecifiedUser();
			teamlessSpecifiedUser.setUserName(specifiedUser.getUserName());
			teamlessProtection.setTeamlessTagCreator(teamlessSpecifiedUser);
		} else {
			teamlessProtection.setTeamlessTagCreator(new TeamlessDepotWriters());
		}
		
		Form<?> form = new Form<Void>("form");
		form.add(new NotificationPanel("feedback", form));

		Depot depot = ((DepotPage) getPage()).getDepot();
		if (depot.getAccount().isOrganization())
			form.add(BeanContext.editBean("editor", protection));
		else
			form.add(BeanContext.editBean("editor", teamlessProtection));
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				Depot depot = ((DepotPage) getPage()).getDepot();
				if (!depot.getAccount().isOrganization()) {
					protection.setNoDeletion(teamlessProtection.isNoDeletion());
					protection.setNoUpdate(teamlessProtection.isNoUpdate());
					protection.setTag(teamlessProtection.getTag());
					
					if (teamlessProtection.getTeamlessTagCreator() instanceof TeamlessDepotAdministrators) {
						protection.setTagCreator(new DepotAdministrators());
					} else if (teamlessProtection.getTeamlessTagCreator() instanceof TeamlessSpecifiedUser) {
						TeamlessSpecifiedUser teamlessSpecifiedUser = 
								(TeamlessSpecifiedUser) teamlessProtection.getTeamlessTagCreator(); 
						SpecifiedUser specifiedUser = new SpecifiedUser();
						specifiedUser.setUserName(teamlessSpecifiedUser.getUserName());
						protection.setTagCreator(specifiedUser);
					} else {
						protection.setTagCreator(new DepotWriters());
					}
				}
				
				onSave(target, protection);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
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

	protected abstract void onSave(AjaxRequestTarget target, TagProtection protection);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
