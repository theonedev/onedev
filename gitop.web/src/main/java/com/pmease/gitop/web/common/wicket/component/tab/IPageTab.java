package com.pmease.gitop.web.common.wicket.component.tab;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public interface IPageTab extends ITabEx {
	
	boolean isSelected(Page page);
	
	public Component newTabLink(String id, final PageParameters params);
}
