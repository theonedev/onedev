package io.onedev.server.web.util;

import org.apache.wicket.Component;
import org.apache.wicket.Page;

import com.google.common.base.Preconditions;

import io.onedev.server.model.Project;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.web.page.project.ProjectPage;

public class ComponentContext extends OneContext {

	private final Component component;
	
	public ComponentContext(Component component) {
		this.component = component;
	}

	@Override
	public Project getProject() {
		Page page = component.getPage();
		if (page instanceof ProjectPage) 
			return ((ProjectPage)page).getProject();
		else
			return null;
	}

	@Override
	public EditContext getEditContext(int level) {
		return WicketUtils.findParents(component, EditContext.class).get(level);
	}

	@Override
	public InputContext getInputContext() {
		return Preconditions.checkNotNull(WicketUtils.findInnermost(component, InputContext.class));
	}

	public Component getComponent() {
		return component;
	}
	
}
