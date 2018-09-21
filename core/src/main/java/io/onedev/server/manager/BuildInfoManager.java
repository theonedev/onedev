package io.onedev.server.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;

public interface BuildInfoManager {

	/**
	 * Get commit heads of previous builds
	 * 
	 * @param project
	 * @param buildId
	 * 
	 * @return
	 * 			<tt>null</tt> if commit head information is not collected yet for specified build
	 */
	@Nullable
	Collection<ObjectId> getPrevCommits(Project project, Long buildId);
	
	void delete(Project project, Long buildId);
	
}
