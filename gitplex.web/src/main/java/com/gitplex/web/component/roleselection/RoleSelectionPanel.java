package com.gitplex.web.component.roleselection;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public abstract class RoleSelectionPanel extends Panel {

	public static final String ROLE_ADMIN = "Admin";
	
	public static final String ROLE_MEMBER = "Member";
	
	private final String currentRole;
	
	public RoleSelectionPanel(String id, @Nullable String currentRole) {
		super(id);
		
		this.currentRole = currentRole;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AjaxLink<Void>("admin") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (ROLE_ADMIN.equals(currentRole))
					add(AttributeAppender.append("class", "active"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelectAdmin(target);
			}
			
		});
		add(new AjaxLink<Void>("member") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (ROLE_MEMBER.equals(currentRole))
					add(AttributeAppender.append("class", "active"));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelectOrdinary(target);
			}
			
		});
	}

	protected abstract void onSelectAdmin(AjaxRequestTarget target);
	
	protected abstract void onSelectOrdinary(AjaxRequestTarget target);
}
