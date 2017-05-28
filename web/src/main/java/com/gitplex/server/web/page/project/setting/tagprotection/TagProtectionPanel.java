package com.gitplex.server.web.page.project.setting.tagprotection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.util.ajaxlistener.ConfirmListener;

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
				TagProtectionEditor editor = new TagProtectionEditor("protection", protection) {

					@Override
					protected void onSave(AjaxRequestTarget target, TagProtection protection) {
						TagProtectionPanel.this.onSave(target, protection);
						Component protectionViewer = BeanContext.viewBean("protection", protection).setOutputMarkupId(true);
						TagProtectionPanel.this.replace(protectionViewer);
						target.add(protectionViewer);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						Component protectionViewer = BeanContext.viewBean("protection", protection).setOutputMarkupId(true);
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
				attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this protection?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		});
		
		add(BeanContext.viewBean("protection", protection).setOutputMarkupId(true));
	}
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onSave(AjaxRequestTarget target, TagProtection protection);
	
}
