package com.pmease.gitplex.web.component.sidebar;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;

@SuppressWarnings("serial")
public class SidebarBorder extends Border {

	private final List<PageTab> tabs;
	
	public SidebarBorder(String id, List<PageTab> tabs) {
		super(id);
		
		this.tabs = tabs;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		addToBorder(new Tabbable("tabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(SidebarBorder.class, "sidebar.css")));
	}

}
