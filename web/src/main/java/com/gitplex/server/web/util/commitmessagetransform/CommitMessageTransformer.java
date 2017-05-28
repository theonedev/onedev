package com.gitplex.server.web.util.commitmessagetransform;

import com.gitplex.launcher.loader.ExtensionPoint;
import com.gitplex.server.model.Project;

@ExtensionPoint
public interface CommitMessageTransformer {
	
	String transform(Project project, String commitMessage);
	
}
