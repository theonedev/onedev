package com.turbodev.server.web.page.project.blob.search;

import java.util.List;

import com.turbodev.server.web.component.floating.FloatingPanel;
import com.turbodev.server.web.component.menu.MenuItem;

public interface SearchMenuContributor {
	
	List<MenuItem> getMenuItems(FloatingPanel dropdown);
	
}
