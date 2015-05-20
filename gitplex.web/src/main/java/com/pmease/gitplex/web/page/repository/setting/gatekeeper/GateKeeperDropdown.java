package com.pmease.gitplex.web.page.repository.setting.gatekeeper;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;

@SuppressWarnings("serial")
public abstract class GateKeeperDropdown extends DropdownPanel {

	public GateKeeperDropdown(String id) {
		super(id);
	}

	@Override
	protected Component newContent(String id) {
		return new GateKeeperSelector(id) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
				hide(target);
				GateKeeperDropdown.this.onSelect(target, gateKeeperClass);
			}
		};
	}

	protected abstract void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass);
}
