package com.gitplex.server.web.page.depot.setting.gatekeeper;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;

import com.gitplex.server.gatekeeper.GateKeeper;
import com.gitplex.server.web.component.floating.AlignPlacement;
import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.component.link.DropdownLink;

@SuppressWarnings("serial")
abstract class GateKeeperLink extends DropdownLink {

	public GateKeeperLink(String id) {
		super(id, true, new AlignPlacement(50, 50, 50, 50));
	}

	@Override
	protected void onInitialize(FloatingPanel dropdown) {
		super.onInitialize(dropdown);
		dropdown.add(AttributeAppender.append("class", "gate-keeper-selector"));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new GateKeeperDropBehavior() {

			@Override
			protected List<Integer> getPosition() {
				return GateKeeperLink.this.getPosition();
			}

		});
	}

	@Override
	protected Component newContent(String id) {
		return new GateKeeperSelector(id) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
				closeDropdown();
				GateKeeperLink.this.onSelect(target, gateKeeperClass);
			}
		};
	}

	protected abstract void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass);
	
	protected abstract List<Integer> getPosition();
}
