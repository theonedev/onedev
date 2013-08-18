package com.pmease.gitop.core.model.gatekeeper;

import java.util.Collection;

import org.apache.shiro.SecurityUtils;

import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.permission.ObjectPermission;

public class AllowAuthorizedUsers extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest mergeRequest) {
		Collection<String> touchedFiles = mergeRequest.getTouchedFiles();
		if (!touchedFiles.isEmpty()) {
			for (String filePath: touchedFiles) {
				ObjectPermission requiredPermission = ObjectPermission.ofBranchWrite(
						mergeRequest.getTargetBranch().getRepository(), 
						mergeRequest.getTargetBranch().getName(),
						filePath);
				if (!SecurityUtils.getSubject().isPermitted(requiredPermission))
					return CheckResult.REJECT;
			}
			
			return CheckResult.ACCEPT;
		} else {
			ObjectPermission requiredPermission = ObjectPermission.ofBranchWrite(
					mergeRequest.getTargetBranch().getRepository(), mergeRequest.getTargetBranch().getName(), null);
			if (SecurityUtils.getSubject().isPermitted(requiredPermission))
				return CheckResult.ACCEPT;
			else
				return CheckResult.REJECT;
		}
	}

}
