package io.onedev.server.buildspec.job.gitcredential;

import java.io.Serializable;

import io.onedev.k8shelper.CloneInfo;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface GitCredential extends Serializable {
	
	CloneInfo newCloneInfo(Build build, String jobToken);
	
}
