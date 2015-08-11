package com.pmease.gitplex.web.component.diff.revision;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;

import com.pmease.commons.wicket.ajaxlistener.IndicateLoadingListener;
import com.pmease.commons.wicket.behavior.menu.CheckItem;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;

@SuppressWarnings("serial")
abstract class LineProcessOptionMenu extends MenuPanel {

	private LineProcessOption option = LineProcessOption.IGNORE_NOTHING;
	
	public LineProcessOptionMenu(String id) {
		super(id);
	}

	protected abstract void onOptionChange(AjaxRequestTarget target);
	
	public LineProcessOption getOption() {
		return option;
	}
	
	@Override
	protected List<MenuItem> getMenuItems() {
		List<MenuItem> menuItems = new ArrayList<>();
		
		for (final LineProcessOption option: LineProcessOption.values()) {
			menuItems.add(new CheckItem() {

				@Override
				protected String getLabel() {
					return option.getName();
				}

				@Override
				protected boolean isChecked() {
					return getOption() == option;
				}

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new IndicateLoadingListener());
				}

				@Override
				protected void onClick(AjaxRequestTarget target) {
					LineProcessOptionMenu.this.option = option;
					onOptionChange(target);
				}
				
			});
		}

		return menuItems;
	}	
}

