package io.onedev.server.web.component.project.privilege.selection;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.security.permission.ProjectPrivilege;

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
		add(new AjaxLink<Void>("issueRead") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (currentPrivilege == ProjectPrivilege.ISSUE_READ)
					add(AttributeAppender.append("class", "active"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, ProjectPrivilege.ISSUE_READ);
			}
			
		});
		add(new AjaxLink<Void>("codeRead") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (currentPrivilege == ProjectPrivilege.CODE_READ)
					add(AttributeAppender.append("class", "active"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, ProjectPrivilege.CODE_READ);
			}
			
		});
		add(new AjaxLink<Void>("codeWrite") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (currentPrivilege == ProjectPrivilege.CODE_WRITE)
					add(AttributeAppender.append("class", "active"));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, ProjectPrivilege.CODE_WRITE);
			}
			
		});
		add(new AjaxLink<Void>("administration") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (currentPrivilege == ProjectPrivilege.ADMINISTRATION)
					add(AttributeAppender.append("class", "active"));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, ProjectPrivilege.ADMINISTRATION);
			}
			
		});
	}

	protected abstract void onSelect(AjaxRequestTarget target, ProjectPrivilege privilege);
	
}
