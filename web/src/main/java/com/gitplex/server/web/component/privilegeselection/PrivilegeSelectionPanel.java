package com.gitplex.server.web.component.privilegeselection;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.server.core.security.privilege.DepotPrivilege;

@SuppressWarnings("serial")
public abstract class PrivilegeSelectionPanel extends Panel {

	private final boolean withAdmin;
	
	private final DepotPrivilege currentPrivilege;
	
	public PrivilegeSelectionPanel(String id, boolean withAdmin, @Nullable DepotPrivilege currentPrivilege) {
		super(id);
		this.withAdmin = withAdmin;
		this.currentPrivilege = currentPrivilege;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AjaxLink<Void>("read") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (currentPrivilege == DepotPrivilege.READ)
					add(AttributeAppender.append("class", "active"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, DepotPrivilege.READ);
			}
			
		});
		add(new AjaxLink<Void>("write") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (currentPrivilege == DepotPrivilege.WRITE)
					add(AttributeAppender.append("class", "active"));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, DepotPrivilege.WRITE);
			}
			
		});
		add(new AjaxLink<Void>("admin") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (currentPrivilege == DepotPrivilege.ADMIN)
					add(AttributeAppender.append("class", "active"));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(withAdmin);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, DepotPrivilege.ADMIN);
			}
			
		});
	}

	protected abstract void onSelect(AjaxRequestTarget target, DepotPrivilege privilege);
	
}
