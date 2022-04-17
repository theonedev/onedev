package io.onedev.server.util;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.Project;

public interface ProjectScope {

	Project getProject();
	
	boolean isRecursive();
	
	@Nullable
	RecursiveConfigurable getRecursiveConfigurable();

	public static interface RecursiveConfigurable {
		
		public void setRecursive(AjaxRequestTarget target, boolean recursive);
		
	}
	
}
