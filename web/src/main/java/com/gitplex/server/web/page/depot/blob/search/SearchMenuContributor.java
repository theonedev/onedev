package com.gitplex.server.web.page.depot.blob.search;

import java.util.List;

import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.component.menu.MenuItem;

public interface SearchMenuContributor {
	
	List<MenuItem> getMenuItems(FloatingPanel dropdown);
	
}
