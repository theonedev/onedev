package io.onedev.server.ci;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.model.Project;

@ExtensionPoint
public interface DefaultCISpecProvider {
	
	int DEFAULT_PRIORITY = 100;
	
	/**
	 * Get default CI spec for specified project and commit
	 * @param project
	 * @param commitId
	 * @return
	 * 			default CI spec, or <tt>null</tt> if no default CI spec can be provided
	 */
	@Nullable
	CISpec getDefaultCISpec(Project project, ObjectId commitId);
	
	int getPriority();
	
}
