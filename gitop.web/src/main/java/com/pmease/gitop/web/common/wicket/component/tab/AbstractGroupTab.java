package com.pmease.gitop.web.common.wicket.component.tab;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.model.IModel;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

@SuppressWarnings("serial")
public abstract class AbstractGroupTab extends AbstractTab implements ITabEx {

	public AbstractGroupTab(IModel<String> title) {
		super(title);
	}
	
	@Override
	public String getTabId() {
		String title = getTitle().getObject();
		Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "title");
		return StringUtils.replace(title.toLowerCase(), " ", "-");
	}
	
	@Override
	public boolean isSelected(ITabEx tab) {
		if (tab == null) {
			return false;
		}
		
		return Objects.equal(tab.getGroupName(), getGroupName()) &&
				Objects.equal(tab.getTitle().getObject(), this.getTitle().getObject());
	}
}
