package com.pmease.gitop.web.page.project.settings;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.web.page.project.settings.gatekeeper.GateKeeperDropdown;

@SuppressWarnings("serial")
public class GateKeeperSettingPage extends AbstractProjectSettingPage {

	public GateKeeperSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		GateKeeperDropdown gateKeeperDropdown = new GateKeeperDropdown("gateKeeperDropdown") {

			@Override
			protected void onSelect(Class<? extends GateKeeper> gateKeeperClass) {
				
			}
			
		};
		add(gateKeeperDropdown);
		DropdownBehavior behavior = new DropdownBehavior(gateKeeperDropdown);
		behavior.alignWithCursor(10, 10);
		add(new WebMarkupContainer("gateKeeperDropdownTrigger").add(behavior));
	}

	@Override
	protected Category getCategory() {
		return Category.GATE_KEEPER;
	}

}
