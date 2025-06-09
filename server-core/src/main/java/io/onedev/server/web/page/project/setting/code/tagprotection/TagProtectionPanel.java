package io.onedev.server.web.page.project.setting.code.tagprotection;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.editable.BeanContext;

abstract class TagProtectionPanel extends Panel {

	private final TagProtection protection;
	
	public TagProtectionPanel(String id, TagProtection protection) {
		super(id);
		
		this.protection = protection;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("tags", protection.getTags()));
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				TagProtectionEditPanel editor = new TagProtectionEditPanel("protection", protection) {

					@Override
					protected void onSave(AjaxRequestTarget target, TagProtection protection) {
						TagProtectionPanel.this.onSave(target, protection);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						TagProtectionPanel.this.onCancel(target);
					}
					
				};
				TagProtectionPanel.this.replace(editor);
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
				target.add(TagProtectionPanel.this);
			}
			
		});
		
		add(BeanContext.view("protection", protection).setOutputMarkupId(true));
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onSave(AjaxRequestTarget target, TagProtection protection);

	protected abstract void onCancel(AjaxRequestTarget target);
	
}
