package com.turbodev.server.web.util.commitmessagetransform;

import com.turbodev.launcher.loader.ExtensionPoint;
import com.turbodev.server.model.Project;

@ExtensionPoint
public interface CommitMessageTransformer {
	
	String transform(Project project, String commitMessage);
	
}
