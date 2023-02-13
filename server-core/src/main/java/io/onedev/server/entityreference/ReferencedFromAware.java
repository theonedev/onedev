package io.onedev.server.entityreference;

import javax.annotation.Nullable;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;

public interface ReferencedFromAware<T extends AbstractEntity> {

	@Nullable
	T getReferencedFrom();
	
	public static boolean canDisplay(ReferencedFromAware<?> referencedFromAware) {
		AbstractEntity referencedFrom = referencedFromAware.getReferencedFrom();
		if (referencedFrom instanceof Issue) 
			return SecurityUtils.canAccess((Issue) referencedFrom);
		else if (referencedFrom instanceof PullRequest) 
			return SecurityUtils.canReadCode(((PullRequest) referencedFrom).getProject());
		else if (referencedFrom instanceof CodeComment) 
			return SecurityUtils.canReadCode(((CodeComment) referencedFrom).getProject());
		else 
			return referencedFrom != null; 
	}
}
