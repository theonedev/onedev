package com.pmease.gitplex.web.page.repository.setting.gatekeeper;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;

import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.floating.Alignment;
import com.pmease.commons.wicket.component.floating.FloatingPanel;
import com.pmease.commons.wicket.dropdown.AlignDropdownWithMouse;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;

@SuppressWarnings("serial")
public abstract class GateKeeperLink extends DropdownLink<Void> {

	public GateKeeperLink(String id) {
		super(id, new AlignDropdownWithMouse(), new Alignment(50, 50, 50, 50));
	}

	@Override
	protected void onInitialize(FloatingPanel dropdown) {
		super.onInitialize(dropdown);
		dropdown.add(AttributeAppender.append("class", "gate-keeper-selector"));
	}

	@Override
	protected Component newContent(String id) {
		return new GateKeeperSelector(id) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
				close(target);
				GateKeeperLink.this.onSelect(target, gateKeeperClass);
			}
		};
	}

	protected abstract void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass);
}
