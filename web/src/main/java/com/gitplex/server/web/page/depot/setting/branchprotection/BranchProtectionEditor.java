package com.gitplex.server.web.page.depot.setting.branchprotection;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.FileProtection;
import com.gitplex.server.util.reviewappointment.ReviewAppointment;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.page.depot.DepotPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
abstract class BranchProtectionEditor extends Panel {

	private final BranchProtection protection;
	
	public BranchProtectionEditor(String id, BranchProtection protection) {
		super(id);
		
		this.protection = protection;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		Form<?> form = new Form<Void>("form");
		form.add(new NotificationPanel("feedback", form));

		TeamlessBranchProtection teamlessProtection = new TeamlessBranchProtection();
		teamlessProtection.setBranch(protection.getBranch());
		teamlessProtection.setFileProtections(protection.getFileProtections());
		teamlessProtection.setNoDeletion(protection.isNoDeletion());
		teamlessProtection.setNoForcedPush(protection.isNoForcedPush());
		
		Depot depot = ((DepotPage) getPage()).getDepot();
		
		List<String> reviewerNames = new ArrayList<>();
		ReviewAppointment reviewAppointment = protection.getReviewAppointment(depot);
		if (reviewAppointment != null) {
			for (Account user: reviewAppointment.getUsers())
				reviewerNames.add(user.getName());
		}
		teamlessProtection.setReviewerNames(reviewerNames);
		
		for (FileProtection fileProtection: teamlessProtection.getFileProtections()) {
			TeamlessFileProtection teamlessFileProtection = new TeamlessFileProtection();
			teamlessFileProtection.setPath(fileProtection.getPath());
			
			reviewerNames = new ArrayList<>();
			reviewAppointment = fileProtection.getReviewAppointment(depot);
			if (reviewAppointment != null) {
				for (Account user: reviewAppointment.getUsers())
					reviewerNames.add(user.getName());
			}
			teamlessFileProtection.setReviewerNames(reviewerNames);
			
			teamlessProtection.getTeamlessFileProtections().add(teamlessFileProtection);
		}
		
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
					protection.setBranch(teamlessProtection.getBranch());
					protection.setFileProtections(teamlessProtection.getFileProtections());
					protection.setNoDeletion(teamlessProtection.isNoDeletion());
					protection.setNoForcedPush(teamlessProtection.isNoForcedPush());
					
					StringBuilder builder = new StringBuilder();
					for (String userName: teamlessProtection.getReviewerNames())
						builder.append("user(").append(userName).append(") ");
					if (builder.length() != 0)
						protection.setReviewAppointmentExpr(builder.toString().trim());
					else
						protection.setReviewAppointmentExpr(null);
					
					protection.getFileProtections().clear();
					for (TeamlessFileProtection teamlessFileProtection: teamlessProtection.getTeamlessFileProtections()) {
						FileProtection fileProtection = new FileProtection();
						fileProtection.setPath(teamlessFileProtection.getPath());
						
						builder = new StringBuilder();
						for (String userName: teamlessFileProtection.getReviewerNames())
							builder.append("user(").append(userName).append(") ");
						if (builder.length() != 0)
							fileProtection.setReviewAppointmentExpr(builder.toString().trim());
						
						protection.getFileProtections().add(fileProtection);
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

	protected abstract void onSave(AjaxRequestTarget target, BranchProtection protection);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
