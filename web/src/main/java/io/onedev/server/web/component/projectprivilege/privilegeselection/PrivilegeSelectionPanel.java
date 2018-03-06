package io.onedev.server.web.component.projectprivilege.privilegeselection;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.security.ProjectPrivilege;

@SuppressWarnings("serial")
public abstract class PrivilegeSelectionPanel extends Panel {

	private final ProjectPrivilege currentPrivilege;
	
	public PrivilegeSelectionPanel(String id, @Nullable ProjectPrivilege currentPrivilege) {
		super(id);
		this.currentPrivilege = currentPrivilege;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AjaxLink<Void>("read") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (currentPrivilege == ProjectPrivilege.READ)
					add(AttributeAppender.append("class", "active"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, ProjectPrivilege.READ);
			}
			
		});
		add(new AjaxLink<Void>("write") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (currentPrivilege == ProjectPrivilege.WRITE)
					add(AttributeAppender.append("class", "active"));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, ProjectPrivilege.WRITE);
			}
			
		});
		add(new AjaxLink<Void>("admin") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (currentPrivilege == ProjectPrivilege.ADMIN)
					add(AttributeAppender.append("class", "active"));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, ProjectPrivilege.ADMIN);
			}
			
		});
	}

	protected abstract void onSelect(AjaxRequestTarget target, ProjectPrivilege privilege);
	
}
