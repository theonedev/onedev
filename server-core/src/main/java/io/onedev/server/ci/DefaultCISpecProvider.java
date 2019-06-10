package io.onedev.server.ci;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.model.Project;

/**
 * This extension point extends OneDev's ability to provide default CI spec for different 
 * kinds of projects 
 * <p>
 * OneDev has built-in CI support to build projects. The build instructions are defined in <i>onedev-ci.xml</i> 
 * in root of the project (do not worry about writing this xml, OneDev has visual editor to help you doing
 * that). For some typical projects, it is possible that some default build instructions can be deducted 
 * automatically without existence of <i>onedev-ci.xml</i>. Hence we introduced this extension point to make 
 * OneDev CI more easier to get started.
 * 
 * @author robin
 *
 */
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
	
	/**
	 * Get priority of this provider. If multiple providers can provide default CI specs for 
	 * specified project and commit, the provider with higher priority will win
	 * 
	 * @return
	 * 			a number indicating priority of the provider. The lower the value is, the 
	 *			higher the priority is
	 */
	int getPriority();
	
}
