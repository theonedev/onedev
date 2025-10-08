package io.onedev.server.git.hook;

import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import org.eclipse.jgit.lib.ObjectId;

import org.jspecify.annotations.Nullable;

@ExtensionPoint
public interface GitPreReceiveChecker {

	/**
	 * Perform pre-receive check against specified ref update
	 * @param submitter user to push code
	 * @param oldObjectId ObjectId.zeroId() when create ref
	 * @param newObjectId ObjectId.zeroId() when delete ref
	 * @return found error, or null if no errors found
	 */
	@Nullable
	String check(Project project, User submitter, String refName, ObjectId oldObjectId, ObjectId newObjectId);
			
}
