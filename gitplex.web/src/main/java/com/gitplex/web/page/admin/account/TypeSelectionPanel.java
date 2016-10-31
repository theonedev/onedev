package com.gitplex.web.page.admin.account;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
abstract class TypeSelectionPanel extends Panel {

	public static final String TYPE_USER = "User";
	
	public static final String TYPE_ORGANIZATIOIN = "Organization";
	
	private final String currentType;
	
	public TypeSelectionPanel(String id, @Nullable String currentType) {
		super(id);
		
		this.currentType = currentType;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AjaxLink<Void>("user") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (TYPE_USER.equals(currentType))
					add(AttributeAppender.append("class", "active"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelectUser(target);
			}
			
		});
		add(new AjaxLink<Void>("organization") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (TYPE_ORGANIZATIOIN.equals(currentType))
					add(AttributeAppender.append("class", "active"));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelectOrganization(target);
			}
			
		});
	}

	protected abstract void onSelectUser(AjaxRequestTarget target);
	
	protected abstract void onSelectOrganization(AjaxRequestTarget target);
}
