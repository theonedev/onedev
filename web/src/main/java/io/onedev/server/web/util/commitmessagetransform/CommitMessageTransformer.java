package io.onedev.server.web.util.commitmessagetransform;

import io.onedev.launcher.loader.ExtensionPoint;
import io.onedev.server.model.Project;

@ExtensionPoint
public interface CommitMessageTransformer {
	
	String transform(Project project, String commitMessage);
	
}
