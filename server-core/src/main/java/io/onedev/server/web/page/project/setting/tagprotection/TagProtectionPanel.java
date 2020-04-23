package io.onedev.server.web.page.project.setting.tagprotection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.model.support.TagProtection;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.editable.BeanContext;

@SuppressWarnings("serial")
abstract class TagProtectionPanel extends Panel {

	private final TagProtection protection;
	
	public TagProtectionPanel(String id, TagProtection protection) {
		super(id);
		
		this.protection = protection;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				TagProtectionEditPanel editor = new TagProtectionEditPanel("protection", protection) {

					@Override
					protected void onSave(AjaxRequestTarget target, TagProtection protection) {
						TagProtectionPanel.this.onSave(target, protection);
						Component protectionViewer = BeanContext.view("protection", protection).setOutputMarkupId(true);
						TagProtectionPanel.this.replace(protectionViewer);
						target.add(protectionViewer);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						Component protectionViewer = BeanContext.view("protection", protection).setOutputMarkupId(true);
						TagProtectionPanel.this.replace(protectionViewer);
						target.add(protectionViewer);
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
				attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this protection?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
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
		
		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return !protection.isEnabled()? "disabled": "";
			}
			
		}));
		
		add(BeanContext.view("protection", protection).setOutputMarkupId(true));
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onSave(AjaxRequestTarget target, TagProtection protection);
	
}
