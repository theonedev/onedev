package com.pmease.gitop.web.page.project.api;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.io.IClusterable;

import com.pmease.gitop.model.Project;

public interface ProjectTabContribution extends IClusterable {
	
	ProjectTabGroup getGroup();
	
	String getName();
	
	Component newLink(String id, IModel<Project> project);
}
