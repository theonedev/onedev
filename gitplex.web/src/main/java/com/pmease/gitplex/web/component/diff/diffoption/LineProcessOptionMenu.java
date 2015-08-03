package com.pmease.gitplex.web.component.diff.diffoption;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.LineProcessor;
import com.pmease.commons.wicket.behavior.menu.CheckItem;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;

@SuppressWarnings("serial")
public class LineProcessOptionMenu extends MenuPanel {

	public LineProcessOptionMenu(String id, IModel<LineProcessor> model) {
		super(id, model);
	}

	@Override
	protected List<MenuItem> getMenuItems() {
		List<MenuItem> menuItems = new ArrayList<>();
		
		for (final LineProcessOption option: LineProcessOption.values()) {
			menuItems.add(new CheckItem() {

				@Override
				protected String getLabel() {
					return option.toString();
				}

				@Override
				protected boolean isChecked() {
					return getDefaultModelObject() == option;
				}

				@Override
				protected void onClick(AjaxRequestTarget target) {
					setDefaultModelObject(option);
					hide(target);
				}
				
			});
		}

		return menuItems;
	}	
}

