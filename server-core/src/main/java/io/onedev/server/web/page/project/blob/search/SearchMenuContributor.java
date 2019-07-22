package io.onedev.server.web.page.project.blob.search;

import java.util.List;

import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.menu.MenuItem;

public interface SearchMenuContributor {
	
	List<MenuItem> getMenuItems(FloatingPanel dropdown);
	
}
