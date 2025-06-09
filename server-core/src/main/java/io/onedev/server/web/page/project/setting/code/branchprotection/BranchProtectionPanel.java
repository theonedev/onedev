package io.onedev.server.web.page.project.setting.code.branchprotection;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.editable.BeanContext;

abstract class BranchProtectionPanel extends Panel {

	private final BranchProtection protection;
	
	public BranchProtectionPanel(String id, BranchProtection protection) {
		super(id);
		
		this.protection = protection;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("branches", protection.getBranches()));
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				BranchProtectionEditPanel editor = new BranchProtectionEditPanel("protection", protection) {

					@Override
					protected void onSave(AjaxRequestTarget target, BranchProtection protection) {
						BranchProtectionPanel.this.onSave(target, protection);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						BranchProtectionPanel.this.onCancel(target);
					}
					
				};
				BranchProtectionPanel.this.replace(editor);
				target.add(editor);
			}
			
		});
		
		add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this protection?")));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		});
		
		add(new WebMarkupContainer("disabled") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!protection.isEnabled());
			}
			
		});
		
		add(new AjaxCheckBox("enable", Model.of(protection.isEnabled())) {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				protection.setEnabled(!protection.isEnabled());
				onSave(target, protection);
				target.add(BranchProtectionPanel.this);
			}
			
		});
		
		add(BeanContext.view("protection", protection).setOutputMarkupId(true));
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onSave(AjaxRequestTarget target, BranchProtection protection);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}
