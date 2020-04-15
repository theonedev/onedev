package io.onedev.server.util.script;

import javax.annotation.Nullable;

import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GroovyScript;

/**
 * Use abstract class instead of interface here as otherwise groovy can not invoke static methods defined here
 * @author robin
 *
 */
@ExtensionPoint
public abstract class ScriptContribution {
	
	public abstract GroovyScript getScript();
	
	@Nullable
	public static String determineBuildFailureInvestigator() {
		Build build = Build.get();
		if (build != null) {
			RevCommit commit = Build.get().getProject().getRevCommit(build.getCommitId(), true);
			User user = OneDev.getInstance(UserManager.class).find(commit.getCommitterIdent());
			if (user != null)
				return user.getName();
			else
				return null;
		} else {
			return null;
		}
	}

	@Nullable
	public static Long getBuildNumber() {
		Build build = Build.get();
		if (build != null)
			return build.getNumber();
		else
			return null;
	}
	
}
