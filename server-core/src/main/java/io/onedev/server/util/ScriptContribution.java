package io.onedev.server.util;

import javax.annotation.Nullable;

import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.EmailAddress;
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
			EmailAddressManager emailAddressManager = OneDev.getInstance(EmailAddressManager.class);
			EmailAddress emailAddress = emailAddressManager.findByPersonIdent(commit.getCommitterIdent());
			if (emailAddress != null && emailAddress.isVerified())
				return emailAddress.getOwner().getName();
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
